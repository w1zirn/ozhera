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

import com.xiaomi.hera.trace.etl.domain.ConfigGetType;
import com.xiaomi.hera.trace.etl.mapper.HeraTraceEtlConfigMapper;
import com.xiaomi.hera.trace.etl.service.DubboManagerService;
import com.xiaomi.hera.trace.etl.service.HttpManagerService;
import com.xiaomi.hera.trace.etl.service.api.ManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Description
 * @Author dingtao
 * @Date 2022/4/28 10:32 am
 */
@Configuration
@Slf4j
public class ServiceConfiguration{

    @Value("${trace.config.get.type}")
    private String configGetType;
    @Value("${trace.config.get.http.domain}")
    private String configGetHttpDomain;

    @Autowired
    private HeraTraceEtlConfigMapper heraTraceEtlConfigMapper;

    @Bean
    public ManagerService managerService() {
        if (ConfigGetType.HTTP.equals(configGetType)) {
            return new HttpManagerService(configGetHttpDomain);
        } else {
            return new DubboManagerService(heraTraceEtlConfigMapper);
        }
    }

}
