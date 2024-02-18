/*
 * Copyright 2020 Xiaomi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.xiaomi.hera.trace.etl.es.consumer;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.google.common.base.Joiner;
import com.xiaomi.hera.trace.etl.api.service.DataSourceService;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.hera.trace.etl.domain.metrics.ThriftUtil;
import com.xiaomi.hera.trace.etl.es.domain.Const;
import com.xiaomi.hera.trace.etl.es.domain.FilterResult;
import com.xiaomi.hera.trace.etl.es.domain.FutureRequest;
import com.xiaomi.hera.trace.etl.es.domain.LocalStorages;
import com.xiaomi.hera.trace.etl.es.filter.FilterService;
import com.xiaomi.hera.trace.etl.es.queue.impl.RocksdbStoreServiceImpl;
import com.xiaomi.hera.trace.etl.es.queue.impl.TeSnowFlake;
import com.xiaomi.hera.trace.etl.es.service.bloomfilter.BloomFilterService;
import com.xiaomi.hera.trace.etl.es.service.bloomfilter.LocalBloomFilterService;
import com.xiaomi.hera.trace.etl.es.service.bloomfilter.RedisBloomFilterService;
import com.xiaomi.hera.trace.etl.es.service.pool.ConsumerPool;
import com.xiaomi.hera.trace.etl.es.service.redis.RedisService;
import com.xiaomi.hera.trace.etl.util.ExecutorUtil;
import com.xiaomi.hera.trace.etl.util.MessageUtil;
import com.xiaomi.hera.tspandata.TAttributeKey;
import com.xiaomi.hera.tspandata.TAttributes;
import com.xiaomi.hera.tspandata.TSpanData;
import com.xiaomi.hera.tspandata.TValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * @author dingtao
 * @Description
 * @date 2021/9/29 2:47 pm
 */
@Service
@Slf4j
public class ConsumerService {

    @Value("${rocks.first.gap}")
    private long firstGap;
    @Value("${rocks.second.gap}")
    private long secondGap;
    @Value("${rocks.first.path}")
    private String firstRocksPath;
    @Value("${rocks.second.path}")
    private String secondRocksPath;

    @NacosValue(value = "${trace.es.filter.isopen}", autoRefreshed = true)
    private boolean filterIsOpen;

    @Value("${bloom.filter.type}")
    private String bloomFilterType;

    @Autowired
    private BloomFilterService bloomFilterService;
    @Autowired
    private FilterService filterService;
    @Autowired
    private DataSourceService dataSourceService;
    @Autowired
    private TeSnowFlake snowFlake;
    @Autowired
    private RedisService redisService;

    private RocksdbStoreServiceImpl firstRocksdbStoreService;
    private RocksdbStoreServiceImpl secondRocksdbStoreService;

    /**
     * Control the number of rocksDB messages stored in each batch
     * to prevent memory overflow caused by too many single key messages
     */
    private AtomicInteger firstCount = new AtomicInteger();
    private AtomicInteger secondCount = new AtomicInteger();

    private CopyOnWriteArrayList<String> firstList = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<String> secondList = new CopyOnWriteArrayList<>();

    /**
     * The first lock is isolated from the second lock
     */
    private ReentrantLock firstLock = new ReentrantLock();
    private ReentrantLock secondLock = new ReentrantLock();

    /**
     * The maximum time interval for requesting Redis, in milliseconds.
     * When the time elapsed since the last Redis request exceeds this interval,
     * a new request to Redis will be made.
     */
    private static final int GAP_TIME = 1000;

    private static final int BATCH_ROCKSDB_COUNT = 20;

    @PostConstruct
    public void init() {
        if (filterIsOpen) {
            firstRocksdbStoreService = new RocksdbStoreServiceImpl(firstRocksPath, TeSnowFlake.FIRST_TIMESTAMP_REDIS_PREFIX);
            secondRocksdbStoreService = new RocksdbStoreServiceImpl(secondRocksPath, TeSnowFlake.SECOND_TIMESTAMP_REDIS_PREFIX);
            // Initialize the rocksdb task for the first time
            initFirstRocksTask();
            // Initializes the second read rocksdb task
            initSecondRocksTask();
            if(Const.BLOOM_FILTER_TYPE_REDIS.equals(bloomFilterType)) {
                // Batch execute Redis BFMEXIST tasks.
                redisExistBatch();
            }
        }
    }

    private void dealMessage(String order, String message) {
        if (StringUtils.isEmpty(message)) {
            return;
        }
        // The body of the message stored in Rocksdb is: traceId ### serviceName ### spanName ### TSpanData(String) #### ......
        String[] messages = message.split(MessageUtil.ROCKS_SPLIT);
        for (String oneMessage : messages) {
            String[] split = oneMessage.split(MessageUtil.SPLIT);
            TSpanData tSpanData = deserializeFromString(split[3]);
            if (tSpanData != null) {
                if (bloomFilterService.isExistLocal(split[0], split[1], split[2], Const.ROCKS_TYPE, order, tSpanData)) {
                    // write into data source
                    dataSourceService.insertHeraSpan(tSpanData, split[1], split[2]);
                } else if (Const.FIRST_ORDER.equals(order)) {
                    if(Const.BLOOM_FILTER_TYPE_LOCAL.equals(bloomFilterType)) {
                        insertRocks(split[0], split[1], split[2], tSpanData, Const.SECOND_ORDER);
                    }
                }
            }
        }
    }

    public void consumer(TSpanData tSpanData) {
        try {
            if (tSpanData == null) {
                log.error("tSpanData is null");
                return;
            }

            String status = tSpanData.getStatus().name();
            String heraContext = "";
            TAttributes attributes = tSpanData.getAttributes();
            List<TAttributeKey> tagsKeys = attributes.getKeys();
            List<TValue> tagsValues = attributes.getValues();
            if (tagsKeys != null && tagsValues != null && tagsKeys.size() > 0 && tagsKeys.size() != tagsValues.size()) {
                for (int i = 0; i < tagsKeys.size(); i++) {
                    String key = tagsKeys.get(i).getValue();
                    String value = ThriftUtil.valueToString(tagsKeys.get(i), tagsValues.get(i));
                    if (filterIsOpen) {
                        if ("span.hera_context".equals(key)) {
                            heraContext = value;
                        }
                    }
                }
            }
            String serviceName = "unknow-service";
            if (tSpanData.getExtra() != null && StringUtils.isNotEmpty(tSpanData.getExtra().getServiceName())) {
                serviceName = tSpanData.getExtra().getServiceName();
            }
            // filter
            String traceId = tSpanData.getTraceId();
            String spanName = tSpanData.getName();
            Long duration = tSpanData.getEndEpochNanos() - tSpanData.getStartEpochNanos();
            FilterResult filter = filterService.filterBefore(status, traceId, spanName, heraContext, serviceName, duration, new SpanHolder(tSpanData));
            if (filter.isDiscard()) {
                return;
            }
            if (filter.isResult()) {
                if (filter.isAddBloom()) {
                    // inert bloom filter
                    bloomFilterService.addBatch(traceId);
                }
                // write into data source
                dataSourceService.insertHeraSpan(tSpanData, serviceName, spanName);
            } else {
                insertRocks(traceId, serviceName, spanName, tSpanData, Const.FIRST_ORDER);
            }
        } catch (Throwable e) {
            log.error("message parse error, message : " + tSpanData.toString(), e);
        }
    }

    private void insertRocks(String traceId, String serviceName, String spanName, TSpanData tSpanData, String order) {
        if (filterIsOpen) {
            if (Const.FIRST_ORDER.equals(order)) {
                internatInset(traceId, serviceName, spanName, tSpanData, order);
            } else if (Const.SECOND_ORDER.equals(order)) {
                internatInset(traceId, serviceName, spanName, tSpanData, order);
            }
        }
    }

    //Use optimistic locking to try to improve performance a bit.
    private void internatInset(String traceId, String serviceName, String spanName, TSpanData tSpanData, String order) {
        String m = buildRocksDBMessage(traceId, serviceName, spanName, tSpanData, order);
        if (StringUtils.isEmpty(m)) {
            return;
        }
        // Check the second level match
        long currSeconds = System.currentTimeMillis() / 1000;
        if (Const.FIRST_ORDER.equals(order)) {
            int j = firstCount.getAndUpdate(i -> {
                if (i >= BATCH_ROCKSDB_COUNT) {
                    return 0;
                }
                return i;
            });

            MutableObject<String> mo = new MutableObject<>();
            firstLock.lock();
            try{
                firstList.add(m);
                if (j >= BATCH_ROCKSDB_COUNT) {
                    String msg = Joiner.on("").join(firstList);
                    mo.setValue(msg);
                    firstList.clear();
                }
            }finally {
                firstLock.unlock();
            }

            if (j >= BATCH_ROCKSDB_COUNT) {
                String key = firstRocksdbStoreService.getKey(currSeconds, LocalStorages.firstRocksKeySuffix.addAndGet(1));
                firstRocksdbStoreService.put(key, mo.getValue().getBytes(StandardCharsets.UTF_8));
            }

        } else if (Const.SECOND_ORDER.equals(order)) {
            int j = secondCount.getAndUpdate(i -> {
                if (i >= BATCH_ROCKSDB_COUNT) {
                    return 0;
                }
                return i;
            });

            MutableObject<String> mo = new MutableObject<>();
            secondLock.lock();
            try{
                secondList.add(m);
                if (j >= BATCH_ROCKSDB_COUNT) {
                    String msg = Joiner.on("").join(secondList);
                    mo.setValue(msg);
                    secondList.clear();
                }
            }finally {
                secondLock.unlock();
            }

            if (j >= BATCH_ROCKSDB_COUNT) {
                String key = secondRocksdbStoreService.getKey(currSeconds, LocalStorages.secondRocksKeySuffix.addAndGet(1));
                secondRocksdbStoreService.put(key, mo.getValue().getBytes(StandardCharsets.UTF_8));
            }
        }

    }

    private String buildRocksDBMessage(String traceId, String serviceName, String spanName, TSpanData tSpanData, String order) {
        String serialize = serializeToString(tSpanData);
        if (serialize != null) {
            if (Const.FIRST_ORDER.equals(order)) {
                StringBuilder sb = new StringBuilder();
                sb.append(traceId).append(MessageUtil.SPLIT)
                        .append(serviceName).append(MessageUtil.SPLIT)
                        .append(spanName).append(MessageUtil.SPLIT)
                        .append(serialize).append(MessageUtil.ROCKS_SPLIT);
                firstCount.incrementAndGet();
                return sb.toString();
            } else if (Const.SECOND_ORDER.equals(order)) {
                StringBuilder sb = new StringBuilder();
                sb.append(traceId).append(MessageUtil.SPLIT)
                        .append(serviceName).append(MessageUtil.SPLIT)
                        .append(spanName).append(MessageUtil.SPLIT)
                        .append(serialize).append(MessageUtil.ROCKS_SPLIT);
                secondCount.incrementAndGet();
                return sb.toString();
            }
        }
        return "";
    }

    private void initFirstRocksTask() {
        // Gets the timestamp of the last message read
        String firstKey = snowFlake.recoverLastTimestamp(TeSnowFlake.FIRST_TIMESTAMP_REDIS_PREFIX);
        final String firstLastRocksKey = firstKey == null ?
                System.currentTimeMillis() + "_" + LocalStorages.firstRocksKeySuffix.get() : firstKey;
        // The local message thread is read for the first time
        ExecutorUtil.submitRocksDBRead(() -> {
            try {
                firstRocksdbStoreService.delayTake(firstLastRocksKey, firstGap, new Consumer<byte[]>() {
                    @Override
                    public void accept(byte[] bytes) {
                        ExecutorUtil.submitDelayMessage(() -> {
                            try {
                                String firstRocksMes = new String(bytes);
                                dealMessage(Const.FIRST_ORDER, firstRocksMes);
                            } catch (Throwable t) {
                                log.error("deal first rocksdb message error : ", t);
                            }
                        });
                    }
                }, snowFlake);
            } catch (Throwable e) {
                log.error("first get Rocks message error : ", e);
            }
        });
    }

    private void initSecondRocksTask() {
        // Gets the timestamp of the last message read
        String secondKey = snowFlake.recoverLastTimestamp(TeSnowFlake.SECOND_TIMESTAMP_REDIS_PREFIX);
        final String secondLastRocksKey = secondKey == null ?
                System.currentTimeMillis() + "_" + LocalStorages.secondRocksKeySuffix.get() : secondKey;
        // The local message thread is read for the sencond time
        ExecutorUtil.submitRocksDBRead(() -> {
            try {
                secondRocksdbStoreService.delayTake(secondLastRocksKey, secondGap, new Consumer<byte[]>() {
                    @Override
                    public void accept(byte[] bytes) {
                        ExecutorUtil.submitDelayMessage(() -> {
                            try {
                                String firstRocksMes = new String(bytes);
                                dealMessage(Const.SECOND_ORDER, firstRocksMes);
                            } catch (Throwable t) {
                                log.error("deal second rocksdb message error : ", t);
                            }
                        });
                    }
                }, snowFlake);
            } catch (Throwable e) {
                log.error("second get Rocks message error : ", e);
            }
        });
    }

    private void redisExistBatch() {
        RedisBloomFilterService redisBloomFilterService = (RedisBloomFilterService) bloomFilterService;
        // consumer redis exist batch
        for (int i = 0; i < ConsumerPool.CONSUMER_BATCH_REDIS_KEY_SIZE; i++) {
            final int index = i;
            BlockingQueue<FutureRequest> futureRequests = ConsumerPool.CONSUMER_BATCH_REDIS_QUEUE.get(index);
            Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setDaemon(false);
                    thread.setName("redis-exist-batch-" + index);
                    return thread;
                }
            }).submit(() -> {
                long lastTime = System.currentTimeMillis();
                while (true) {
                    try {
                        int futureRequestSize = futureRequests.size();
                        if (futureRequestSize >= ConsumerPool.REDIS_EXIST_BATCH || (futureRequestSize > 0 && System.currentTimeMillis() - lastTime > GAP_TIME)) {
                            List<FutureRequest> requests = new ArrayList<>();
                            futureRequests.drainTo(requests);
                            List<String> traceIds = new ArrayList<>();
                            for (FutureRequest request : requests) {
                                traceIds.add(request.getTraceId());
                            }
                            String redisKey = redisBloomFilterService.getRedisKey(index);
                            List<Boolean> booleans = redisService.bfMExist(redisKey, traceIds.toArray(new String[traceIds.size()]));
                            if (booleans != null) {
                                for (int j = 0; j < booleans.size(); j++) {
                                    FutureRequest futureRequest = requests.get(j);
                                    if (booleans.get(j)) {
                                        insertEsOrRocks(futureRequest, true);
                                    } else {
                                        insertEsOrRocks(futureRequest, false);
                                    }
                                }
                            } else {
                                // redis 异常，默认返回true
                                for (FutureRequest request : requests) {
                                    insertEsOrRocks(request, true);
                                }
                            }
                            lastTime = System.currentTimeMillis();
                        }
                        Thread.sleep(1);
                    } catch (Throwable t) {
                        log.error("redis exist batch error : ", t);
                    }
                }
            });
        }
        // rocks redis exist batch
        for (int i = 0; i < ConsumerPool.CONSUMER_BATCH_REDIS_KEY_SIZE; i++) {
            final int index = i;
            BlockingQueue<FutureRequest> futureRequests = ConsumerPool.ROCKS_BATCH_REDIS_QUEUE.get(index);
            Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setDaemon(false);
                    thread.setName("rocksdb-redis-exist-batch-" + index);
                    return thread;
                }
            }).submit(() -> {
                long lastTime = System.currentTimeMillis();
                while (true) {
                    try {
                        int futureRequestSize = futureRequests.size();
                        if (futureRequestSize >= ConsumerPool.REDIS_EXIST_BATCH || (futureRequestSize > 0 && System.currentTimeMillis() - lastTime > GAP_TIME)) {
                            List<FutureRequest> requests = new ArrayList<>();
                            futureRequests.drainTo(requests);
                            List<String> traceIds = new ArrayList<>();
                            for (FutureRequest request : requests) {
                                traceIds.add(request.getTraceId());
                            }
                            String redisKey = redisBloomFilterService.getRedisKey(index);
                            List<Boolean> booleans = redisService.bfMExist(redisKey, traceIds.toArray(new String[traceIds.size()]));
                            if (booleans != null) {
                                for (int j = 0; j < booleans.size(); j++) {
                                    FutureRequest futureRequest = requests.get(j);
                                    if (booleans.get(j)) {
                                        dataSourceService.insertHeraSpan(futureRequest.gettSpanData(), futureRequest.getServiceName(), futureRequest.getSpanName());
                                    } else {
                                        if (Const.FIRST_ORDER.equals(futureRequest.getOrder())) {
                                            insertRocks(futureRequest.getTraceId(), futureRequest.getServiceName(), futureRequest.getSpanName(), futureRequest.gettSpanData(), Const.SECOND_ORDER);
                                        }
                                    }
                                }
                            } else {
                                // redis 异常，默认返回true
                                for (FutureRequest request : requests) {
                                    insertEsOrRocks(request, true);
                                }
                            }
                            lastTime = System.currentTimeMillis();
                        }
                        Thread.sleep(1);
                    } catch (Throwable t) {
                        log.error("redis exist batch error : ", t);
                    }
                }
            });
        }
        // redis add batch
        for (int i = 0; i < ConsumerPool.CONSUMER_BATCH_REDIS_KEY_SIZE; i++) {
            final int index = i;
            BlockingQueue<String> traceIds = ConsumerPool.BATCH_REDIS_ADD_QUEUE.get(index);
            Executors.newSingleThreadExecutor(new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setDaemon(false);
                    thread.setName("redis-add-batch-" + index);
                    return thread;
                }
            }).submit(() -> {
                long lastTime = System.currentTimeMillis();
                while (true) {
                    try {
                        int traceIdSize = traceIds.size();
                        if (traceIdSize >= ConsumerPool.REDIS_ADD_BATCH || (traceIdSize > 0 && System.currentTimeMillis() - lastTime > GAP_TIME)) {
                            List<String> requests = new ArrayList<>();
                            traceIds.drainTo(requests);
                            String redisKey = redisBloomFilterService.getRedisKey(index);
                            redisService.bfInsert(redisKey, requests.toArray(new String[requests.size()]));
                            lastTime = System.currentTimeMillis();
                        }
                        Thread.sleep(1);
                    } catch (Throwable t) {
                        log.error("redis exist batch error : ", t);
                    }
                }
            });
        }
    }

    private void insertEsOrRocks(FutureRequest request, boolean isExist) {
        if (isExist) {
            bloomFilterService.getLocalBloomFilter().put(request.getTraceId());
            // 写入es
            dataSourceService.insertHeraSpan(request.gettSpanData(), request.getServiceName(), request.getSpanName());
        } else {
            insertRocks(request.getTraceId(), request.getServiceName(), request.getSpanName(), request.gettSpanData(), Const.FIRST_ORDER);
        }
    }

    private String serializeToString(TSpanData tSpanData) {
        try {
            byte[] serialize = new TSerializer(ThriftUtil.PROTOCOL_FACTORY).serialize(tSpanData);
            return new String(serialize, StandardCharsets.ISO_8859_1);
        } catch (Throwable e) {
            log.error("rocksDB serializer serialize error");
        }
        return null;
    }

    private TSpanData deserializeFromString(String decode) {
        try {
            TSpanData tSpanData = new TSpanData();
            // The ISO-8859-1 encoding prevents byte[] inconsistency caused by extra character set processing when byte[] is converted to String, resulting in missing thrift deserialization fields
            new TDeserializer(ThriftUtil.PROTOCOL_FACTORY).deserialize(tSpanData, decode.getBytes(StandardCharsets.ISO_8859_1));
            return tSpanData;
        } catch (Throwable e) {
            log.error("rocksDB deserializer deserialize error");
        }
        return null;
    }
}
