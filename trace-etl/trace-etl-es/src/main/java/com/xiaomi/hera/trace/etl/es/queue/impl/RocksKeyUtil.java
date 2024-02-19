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
package com.xiaomi.hera.trace.etl.es.queue.impl;

import com.xiaomi.hera.trace.etl.es.adapter.IAdapter;
import com.xiaomi.hera.trace.etl.es.service.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class RocksKeyUtil {

    private static final long BATCH_CNT = 77L;
    private static final String SN_KEY_PREFIX = "te_sn";
    private static final String SN_KEY_STMP = SN_KEY_PREFIX + "_last_timestamp";

    private static AtomicLong STORE_CNT = new AtomicLong(0L);
    private static AtomicLong SAVED_LAST_STMP = new AtomicLong(-1L);
    public static String SN_KEY_WORK_ID;

    private String mId;
    @Autowired
    private RedisService redis;

    @Autowired
    private IAdapter iAdapter;

    @Value("${gw.snowflake.datacenterId:0}")
    private long datacenterId;

    @PostConstruct
    public void init() {
        mId = iAdapter.getMid();
    }

    public String recoverLastTimestamp(String keyPrefix) {
        try {
            String value = redis.get(keyPrefix + "_" + SN_KEY_STMP + "_" + mId);
            if (StringUtils.isEmpty(keyPrefix)) {
                return null;
            } else {
                return value;
            }
        } catch (Exception e) {
            log.error("recoverLastTimestamp exception:{}", e);
            return null;
        }
    }

    /**
     * extension method
     * Save the latest timestamp and save it to Redis, etc.
     */
    public void storeLastTimestamp(String keyPrefix, String lastRocksKey) {
        long cnt = STORE_CNT.addAndGet(1);
        long lastTimestamp = Long.parseLong(lastRocksKey.split("_")[0]);
        boolean needSave = false;
        // If the request has been made 77 times or the time since the last save exceeds 5 seconds, then execute the Redis save operation.
        if (cnt % BATCH_CNT == 0) {
            needSave = true;
        } else if (lastTimestamp - SAVED_LAST_STMP.get() > 5000) {
            needSave = true;
        }

        if (needSave) {
            redis.set(keyPrefix + "_" + SN_KEY_STMP + "_" + mId, lastRocksKey);
            SAVED_LAST_STMP.set(lastTimestamp);
        }
    }
}
