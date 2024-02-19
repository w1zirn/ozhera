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
package com.xiaomi.hera.trace.etl.es.service.redis;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.xiaomi.hera.trace.etl.es.adapter.IAdapter;
import com.xiaomi.hera.trace.etl.es.domain.Const;
import com.xiaomi.hera.trace.etl.es.service.bloomfilter.RedisBloomFilterService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.bloom.BFInsertParams;
import redis.clients.jedis.bloom.BFReserveParams;
import redis.clients.jedis.commands.JedisCommands;
import redis.clients.jedis.params.SetParams;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class RedisServiceImpl implements RedisService {

    @NacosValue("${spring.redis.single.addr}")
    private String redisAddr;
    @NacosValue("${spring.redis.cluster.nodes}")
    private String clusterNodes;
    @Value("${spring.redis.timeout.connection}")
    private int timeout;
    @Value("${spring.redis.jedis.pool.max-active}")
    private int maxActive;
    @Value("${spring.redis.pool.max-idle}")
    private int maxIdle;
    @Value("${spring.redis.pool.max-wait}")
    private long maxWaitMillis;
    @Value("${spring.redis.max-attempts}")
    private int maxAttempts;

    private JedisCommands jedis;
    private JedisPooled jedisPooled;
    private JedisCluster jedisCluster;

    private RedisBloomFilterService redisBloomFilterService;
    @Autowired
    public void setRedisBloomFilterService(@Lazy RedisBloomFilterService redisBloomFilterService){
        this.redisBloomFilterService = redisBloomFilterService;
    }

    @Autowired
    private IAdapter iAdapter;

    private static final BFReserveParams NON_SCALING = BFReserveParams.reserveParams().nonScaling();
    private BFInsertParams param = new BFInsertParams();
    private long ttlSeconds = 5;
    private long waitTimeOut = 30000;
    private SetParams disLockParam = SetParams.setParams().ex(ttlSeconds).nx();

    @PostConstruct
    public void init() {
        param.noCreate();
        String pwd = iAdapter.getRedisPwd();
        if (StringUtils.isNotEmpty(redisAddr)) {
            String[] hp = redisAddr.split(":");
            if (StringUtils.isEmpty(pwd)) {
                jedisPooled = new JedisPooled(getGenericObjectPoolConfig(), hp[0].trim(), Integer.valueOf(hp[1]), timeout);
            } else {
                jedisPooled = new JedisPooled(getGenericObjectPoolConfig(), hp[0].trim(), Integer.valueOf(hp[1]), timeout, pwd);
            }
            jedis = jedisPooled;
        } else if (StringUtils.isNotEmpty(clusterNodes)) {
            String[] serverArray = clusterNodes.split(",");
            Set<HostAndPort> nodes = new HashSet<>();
            for (String ipPort : serverArray) {
                String[] ipPortPair = ipPort.split(":");
                nodes.add(new HostAndPort(ipPortPair[0].trim(), Integer.valueOf(ipPortPair[1].trim())));
            }
            if (StringUtils.isEmpty(pwd)) {
                jedisCluster = new JedisCluster(nodes, timeout, timeout, maxAttempts, getGenericObjectPoolConfig());
            } else {
                jedisCluster = new JedisCluster(nodes, timeout, timeout, maxAttempts, pwd, getGenericObjectPoolConfig());
            }
            jedis = jedisCluster;
        } else {
            log.error("redis config 'spring.redis.single.addr' and 'spring.redis.cluster.nodes' is empty, set one value at least!");
            throw new RuntimeException("redis config 'spring.redis.single.addr' and 'spring.redis.cluster.nodes' is empty, set one value at least!");
        }
    }

    private GenericObjectPoolConfig getGenericObjectPoolConfig() {
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        genericObjectPoolConfig.setMaxTotal(maxActive);
        genericObjectPoolConfig.setMaxIdle(maxIdle);
        genericObjectPoolConfig.setMaxWait(Duration.ofMillis(maxWaitMillis));
        genericObjectPoolConfig.setTimeBetweenEvictionRuns(Duration.ofMillis(5000L));
        genericObjectPoolConfig.setMinEvictableIdleTime(Duration.ofMinutes(15L));
        genericObjectPoolConfig.setTestWhileIdle(true);
        return genericObjectPoolConfig;
    }

    public Boolean sismember(String key, String member) {
        try {
            return jedis.sismember(key, member);
        } catch (Exception e) {
            log.error("redis sismember error key:" + key + " member:" + member, e);
        }
        return null;
    }

    public Long sadd(String key, String... members) {
        try {
            return jedis.sadd(key, members);
        } catch (Exception e) {
            log.error("redis sadd error key:" + key, e);
        }
        return 0L;
    }

    public Long setNx(String key, String value) {
        try {
            return jedis.setnx(key, value);
        } catch (Exception e) {
            log.error("redis sadd error key:" + key, e);
        }
        return 0L;
    }

    public String get(String key) {
        try {
            return jedis.get(key);
        } catch (Exception e) {
            log.error("redis sadd error key:" + key, e);
        }
        return null;
    }

    public String set(String key, String value) {
        try {
            return jedis.set(key, value);
        } catch (Exception e) {
            log.error("redis set error key:" + key, e);
        }
        return null;
    }

    public String set(String key, String value, long ttl) {
        try {
            String set = jedis.set(key, value);
            if ("OK".equals(set)) {
                jedis.expire(key, ttl);
            }
            return set;
        } catch (Exception e) {
            log.error("redis set error key:" + key, e);
        }
        return null;
    }

    @Override
    public Boolean exists(String key) {
        try {
            return jedis.exists(key);
        } catch (Exception e) {
            log.error("redis exists error key:" + key, e);
        }
        return null;
    }

    public Long del(String key) {
        try {
            return jedis.del(key);
        } catch (Exception e) {
            log.error("redis del error key:" + key, e);
        }
        return null;
    }

    @Override
    public List<Boolean> bfMAdd(String key, String... items) {
        try {
            if (jedisCluster != null) {
                return jedisCluster.bfMAdd(key, items);
            } else {
                return jedisPooled.bfMAdd(key, items);
            }
        } catch (Exception e) {
            log.error("redis bfMAdd error key:" + key, e);
        }
        return null;
    }

    @Override
    public Boolean bfExist(String key, String item) {
        try {
            if (jedisCluster != null) {
                return jedisCluster.bfExists(key, item);
            } else {
                return jedisPooled.bfExists(key, item);
            }
        } catch (Exception e) {
            log.error("redis bfExists error key:" + key, e);
        }
        return null;
    }

    @Override
    public List<Boolean> bfMExist(String key, String... item) {
        List<Boolean> booleansNew = bfMExistInternal(key, item);
        // Using a volatile variable specifically to indicate whether double reading is enabled,
        // avoiding the calculation of the time difference between the current time and the target time for each request.
        if (Const.REDIS_DOUBLE_READ) {
            List<Boolean> booleansOld = bfMExistInternal(redisBloomFilterService.reverseRedisKey(key), item);
            if (booleansNew.size() == booleansOld.size()) {
                for (int i = 0; i < booleansNew.size(); i++) {
                    booleansNew.set(i, booleansNew.get(i) || booleansOld.get(i));
                }
            } else {
                log.error("old bloom filter booleans {} and new bloom filter booleans {} have different length", booleansOld, booleansNew);
            }
        }
        return booleansNew;
    }

    private List<Boolean> bfMExistInternal(String key, String... item) {
        try {
            if (jedisCluster != null) {
                return jedisCluster.bfMExists(key, item);
            } else {
                return jedisPooled.bfMExists(key, item);
            }
        } catch (Exception e) {
            log.error("redis bfExists error key:" + key, e);
        }
        return null;
    }

    @Override
    public String bfReserve(String key, double errorRate, long capacity, boolean scaling) {
        try {
            if (jedisCluster != null) {
                if (scaling) {
                    return jedisCluster.bfReserve(key, errorRate, capacity);
                } else {
                    return jedisCluster.bfReserve(key, errorRate, capacity, NON_SCALING);
                }
            } else {
                if (scaling) {
                    return jedisPooled.bfReserve(key, errorRate, capacity);
                } else {
                    return jedisPooled.bfReserve(key, errorRate, capacity, NON_SCALING);
                }
            }
        } catch (Exception e) {
            log.error("redis bfReserve error key:" + key, e);
        }
        return null;
    }

    @Override
    public List<Boolean> bfInsert(String key, String... items) {
        try {
            if (jedisCluster != null) {
                return jedisCluster.bfInsert(key, param, items);
            } else {
                return jedisPooled.bfInsert(key, param, items);
            }
        } catch (Exception e) {
            log.error("redis bfReserve error key:" + key, e);
        }
        return null;
    }

    @Override
    public Map<String, Object> bfInfo(String key) {
        try {
            if (jedisCluster != null) {
                return jedisCluster.bfInfo(key);
            } else {
                return jedisPooled.bfInfo(key);
            }
        } catch (Exception e) {
            log.error("redis bfReserve error key:" + key, e);
        }
        return null;
    }

    @Override
    public boolean getDisLock(String key) {
        long startTime = System.currentTimeMillis();
        try {
            while (true) {
                if ("OK".equals(jedis.set(key, "1", disLockParam))) {
                    return true;
                } else {
                    Thread.sleep(200);
                }
                if (System.currentTimeMillis() - startTime > waitTimeOut) {
                    log.warn("wait for the distributed lock for more than 30 seconds...");
                    return false;
                }
            }
        } catch (Exception e) {
            log.error("redis getDisLock error key:" + key, e);
        }
        return false;
    }

    @Override
    public String getWithException(String key) throws Exception {
        return jedis.get(key);
    }

}
