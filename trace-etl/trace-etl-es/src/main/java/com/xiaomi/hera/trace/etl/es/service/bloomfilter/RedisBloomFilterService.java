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
package com.xiaomi.hera.trace.etl.es.service.bloomfilter;

import com.google.common.hash.BloomFilter;
import com.xiaomi.hera.trace.etl.es.domain.FutureRequest;
import com.xiaomi.hera.trace.etl.es.service.pool.ConsumerPool;
import com.xiaomi.hera.trace.etl.es.service.redis.RedisService;
import com.xiaomi.hera.tspandata.TSpanData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static com.xiaomi.hera.trace.etl.common.HashUtil.consistentHash;
import static com.xiaomi.hera.trace.etl.es.domain.Const.CONSUMER_TYPE;
import static com.xiaomi.hera.trace.etl.es.domain.Const.REDIS_DOUBLE_READ;
import static com.xiaomi.hera.trace.etl.es.domain.Const.ROCKS_TYPE;

@Service
@ConditionalOnProperty(name = "bloom.filter.type", havingValue = "redis")
@Slf4j
public class RedisBloomFilterService implements BloomFilterService {

    @Autowired
    private RedisService redisService;

    @Resource
    private LocalBloomFilterService localBloomFilterService;

    @Value("${redis.bloom.excepted.insertions}")
    private long redisBloomExceptedInsertions;

    @Value("${redis.bloom.accuracy}")
    private double redisBloomAccuracy;

    private volatile String redisKeyPrefix = "";

    private static final String DISTRIBUTE_LOCK_KEY = "distribute_lock_key";
    /**
     * Store the Redis key corresponding to the current Bloom filter that should be used.
     */
    private static final String BLOOM_REDIS_KEY_ORDER = "bloom_key_order";
    private static final int DEFAULT_BLOOM_REDIS_KEY_ORDER = 1;
    private static final String REDIS_KEY_CONST_PREFIX = "trace_bloom_filter";
    private static final String REDIS_BLOOM_LAST_DELETE_TIME = "bloom_last_delete_time";
    private static volatile int redisKeyOrder = 1;

    @PostConstruct
    private void init() {
        redisKeyPrefix = getRedisKeyPrefix();
        log.info("get redis key prefix {}", redisKeyPrefix);
        localBloomFilterService.createBloom();
    }

    @Override
    public boolean isExistLocal(String traceId, String serviceName, String spanName, String type, String order, TSpanData tSpanData) {
        try {
            boolean local = localBloomFilterService.isExistLocal(traceId, serviceName, spanName, type, order, tSpanData);
            if (local) {
                return true;
            } else {
                // batch check exist
                batchExist(traceId, serviceName, spanName, type, order, tSpanData);
                return false;
            }
        } catch (Exception e) {
            log.error("traceID: " + traceId + " whether it exists in the Redis Bloom filter with failure", e);
        }
        return true;
    }

    @Override
    public BloomFilter<CharSequence> getLocalBloomFilter() {
        return localBloomFilterService.getLocalBloomFilter();
    }

    @Override
    public void addBatch(String traceId) {

    }

    private void batchExist(String traceId, String serviceName, String spanName, String type, String order, TSpanData tSpanData) {
        if (CONSUMER_TYPE.equals(type)) {
            // count duration of await time
            BlockingQueue<FutureRequest> consumerRequestQueue = ConsumerPool.CONSUMER_BATCH_REDIS_QUEUE.get(consistentHash(traceId, ConsumerPool.CONSUMER_BATCH_REDIS_KEY_SIZE));
            await(consumerRequestQueue);
            consumerRequestQueue.offer(new FutureRequest(traceId, tSpanData, serviceName, spanName));
        } else if (ROCKS_TYPE.equals(type)) {
            BlockingQueue<FutureRequest> rocksRequestQueue = ConsumerPool.ROCKS_BATCH_REDIS_QUEUE.get(consistentHash(traceId, ConsumerPool.CONSUMER_BATCH_REDIS_KEY_SIZE));
            await(rocksRequestQueue);
            rocksRequestQueue.offer(new FutureRequest(traceId, tSpanData, serviceName, spanName, order));
        }
    }

    private void await(BlockingQueue<FutureRequest> requestQueue) {
        while (true) {
            try {
                if (requestQueue.remainingCapacity() > ConsumerPool.REDIS_BATCH_CONSUMER_QUEUE_THRESHOLD) {
                    return;
                }
                Thread.sleep(1);
            } catch (Throwable t) {
                log.error("wait queue error : ", t);
            }
        }
    }

    @Override
    public void createBloom() {

    }

    @Override
    public void createBloom(String key) {
        for (int i = 0; i < ConsumerPool.CONSUMER_BATCH_REDIS_KEY_SIZE; i++) {
            String redisKey = key + "_" + i;
            if (!redisService.exists(redisKey)) {
                redisService.bfReserve(redisKey, redisBloomAccuracy, redisBloomExceptedInsertions, false);
            }
        }
    }

    private String getRedisKeyPrefix() {
        // At project startup, retrieve the current Redis key for the Bloom filter from Redis.
        String bloomKeyOrder = null;
        try {
            bloomKeyOrder = redisService.getWithException(BLOOM_REDIS_KEY_ORDER);
        } catch (Exception e) {
            log.error("failed to get redis key, exit the app");
            System.exit(-1);
        }
        if (StringUtils.isEmpty(bloomKeyOrder)) {
            // Using the default redisKeyOrder.
            String redisKey = REDIS_KEY_CONST_PREFIX + "_" + DEFAULT_BLOOM_REDIS_KEY_ORDER;
            createBloom(redisKey);
            redisService.set(BLOOM_REDIS_KEY_ORDER, String.valueOf(DEFAULT_BLOOM_REDIS_KEY_ORDER));
            redisKeyOrder = DEFAULT_BLOOM_REDIS_KEY_ORDER;
            log.info("fail to get redisKeyOrder from redis, using default {}", redisKey);
            return redisKey;
        }
        // Using the redisKeyOrder obtained from Redis.
        String redisKey = REDIS_KEY_CONST_PREFIX + "_" + bloomKeyOrder;
        createBloom(redisKey);
        redisKeyOrder = Integer.parseInt(bloomKeyOrder);
        log.info("get redisKeyOrder from redis {}", redisKeyOrder);
        return redisKey;
    }

    @Override
    public void deleteBloom() {
        int oldOrder = redisKeyOrder == 0 ? 1 : 0;
        String oldKeyPrefix = REDIS_KEY_CONST_PREFIX + "_" + oldOrder;
        long lastDelTime = getRedisBloomLastDelTime(REDIS_BLOOM_LAST_DELETE_TIME);
        if (System.currentTimeMillis() - lastDelTime <= TimeUnit.MINUTES.toMillis(30)) {
            log.info("old redis bloom filter {} has been deleted, just return", oldKeyPrefix);
            return;
        }
        /*
         * While loop ensures continuous retrying in case of lock acquisition failure or exception throwing.
         * */
        while (true) {
            if (redisService.getDisLock(DISTRIBUTE_LOCK_KEY)) {
                try {
                    lastDelTime = getRedisBloomLastDelTime(REDIS_BLOOM_LAST_DELETE_TIME);
                    if (System.currentTimeMillis() - lastDelTime <= TimeUnit.MINUTES.toMillis(30)) {
                        log.info("old redis bloom filter {} has been deleted, just return", oldKeyPrefix);
                        return;
                    }
                    for (int i = 0; i < ConsumerPool.CONSUMER_BATCH_REDIS_KEY_SIZE; i++) {
                        String redisKey = oldKeyPrefix + "_" + i;
                        redisService.del(redisKey);
                    }
                    redisService.set(REDIS_BLOOM_LAST_DELETE_TIME, String.valueOf(System.currentTimeMillis()));
                    log.info("successfully delete old redis bloom filter {}", oldKeyPrefix);
                    return;
                } catch (Throwable t) {
                    log.info("delete bloom has error ", t);
                } finally {
                    redisService.del(DISTRIBUTE_LOCK_KEY);
                }
            }
        }
    }

    @Override
    public void updateBloom() {
        if (redisService.getDisLock(DISTRIBUTE_LOCK_KEY)) {
            int newOrder = redisKeyOrder == 0 ? 1 : 0;
            String newKeyPrefix = REDIS_KEY_CONST_PREFIX + "_" + newOrder;
            try {
                createBloom(newKeyPrefix);
                redisService.set(BLOOM_REDIS_KEY_ORDER, String.valueOf(newOrder));
                log.info("update redis bloom filter key success , new key is : " + newKeyPrefix);
            } catch (Exception e) {
                log.error("update redis bloom error : ", e);
            } finally {
                redisKeyPrefix = newKeyPrefix;
                redisKeyOrder = newOrder;
                redisService.del(DISTRIBUTE_LOCK_KEY);
            }
        } else {
            log.error("update redis bloom error, don't get dis lock");
            int newOrder = redisKeyOrder == 0 ? 1 : 0;
            String newKeyPrefix = REDIS_KEY_CONST_PREFIX + "_" + newOrder;
            redisKeyPrefix = newKeyPrefix;
            redisKeyOrder = newOrder;
        }

        // Synchronize to enable double reading.
        REDIS_DOUBLE_READ = true;
        log.info("set redis double read on REDIS_DOUBLE_READ is {}", REDIS_DOUBLE_READ);
    }

    private long getRedisBloomLastDelTime(String redisDelKey) {
        long lastDelTime = 0L;
        try {
            String lastDelTimeStamp = redisService.get(redisDelKey);
            lastDelTime = Long.parseLong(lastDelTimeStamp);
        } catch (Throwable t) {
            log.info("failed to parse last del time, e is {}", redisDelKey, t);
        }
        return lastDelTime;
    }

    public String getRedisKey(int index) {
        return redisKeyPrefix + "_" + index;
    }

    public String reverseRedisKey(String redisKey) {
        String[] split = redisKey.split("_");
        split[3] = (split[3].equals("0") ? "1" : "0");
        return String.join("_", split);
    }
}
