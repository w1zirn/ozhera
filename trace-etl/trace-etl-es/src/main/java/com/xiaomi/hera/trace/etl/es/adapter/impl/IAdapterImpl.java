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
package com.xiaomi.hera.trace.etl.es.adapter.impl;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.xiaomi.hera.trace.etl.es.adapter.IAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
@Slf4j
public class IAdapterImpl implements IAdapter {
    @NacosValue("${spring.redis.password}")
    private String pwd;

    @Override
    public String getRedisPwd() {
        if(StringUtils.isEmpty(pwd)){
            log.warn("redis password not set !!!");
        }
        return pwd;
    }

    @Override
    public String getMid() {
        // Get machine number from environment variables.
        String podName = System.getenv("MONE_CONTAINER_S_POD_NAME");
        if (StringUtils.isEmpty(podName)) {
            log.error("this pod con't get podName!");
            throw new RuntimeException("this pod con't get podName!");
        }
        return podName.substring(podName.lastIndexOf("-") + 1);
    }
}
