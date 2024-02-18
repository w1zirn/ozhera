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
package com.xiaomi.hera.trace.etl.es.config;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.xiaomi.hera.trace.etl.domain.ConfigGetType;
import com.xiaomi.hera.trace.etl.es.domain.Const;
import com.xiaomi.hera.trace.etl.es.service.bloomfilter.RedisBloomFilterService;
import com.xiaomi.hera.trace.etl.es.service.redis.RedisService;
import com.xiaomi.hera.trace.etl.es.service.redis.RedisServiceImpl;
import com.xiaomi.hera.trace.etl.mapper.HeraTraceEtlConfigMapper;
import com.xiaomi.hera.trace.etl.service.DubboManagerService;
import com.xiaomi.hera.trace.etl.service.HttpManagerService;
import com.xiaomi.hera.trace.etl.service.api.ManagerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.commands.JedisCommands;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

/**
 * @Description
 * @Author dingtao
 * @Date 2022/4/28 10:32 am
 */
@Configuration
@Slf4j
public class ServiceConfiguration implements ApplicationContextAware {

    @Value("${trace.config.get.type}")
    private String configGetType;
    @Value("${trace.config.get.http.domain}")
    private String configGetHttpDomain;


    /**
     * redis config
     */
    @NacosValue("${spring.redis.single.addr}")
    private String redisAddr;
    @NacosValue("${spring.redis.cluster.nodes}")
    private String clusterNodes;
    @NacosValue("${spring.redis.password}")
    private String pwd;
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
    @Value("${bloom.filter.type}")
    private String bloomFilterType;


    @Autowired
    private HeraTraceEtlConfigMapper heraTraceEtlConfigMapper;

    private ApplicationContext ac;


    @Bean
    public ManagerService managerService() {
        if (ConfigGetType.HTTP.equals(configGetType)) {
            return new HttpManagerService(configGetHttpDomain);
        } else {
            return new DubboManagerService(heraTraceEtlConfigMapper);
        }
    }

    @Bean
    public RedisService getRedisService() {
        JedisCommands jedis;
        JedisPooled jedisPooled = null;
        JedisCluster jedisCluster = null;
        RedisBloomFilterService redisBloomFilterService = null;
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
        if(Const.BLOOM_FILTER_TYPE_REDIS.equals(bloomFilterType)){
            redisBloomFilterService = ac.getBean(RedisBloomFilterService.class);
        }
        return new RedisServiceImpl(jedis, jedisPooled, jedisCluster, redisBloomFilterService);
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

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ac = applicationContext;
    }
}
