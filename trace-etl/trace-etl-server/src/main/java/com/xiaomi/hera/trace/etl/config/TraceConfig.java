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
package com.xiaomi.hera.trace.etl.config;

import com.xiaomi.hera.trace.etl.domain.HeraTraceConfigVo;
import com.xiaomi.hera.trace.etl.domain.HeraTraceEtlConfig;
import com.xiaomi.hera.trace.etl.service.api.ManagerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description trace config
 * @Author dingtao
 * @Date 2022/4/25 3:12 下午
 */
@Configuration
@Slf4j
public class TraceConfig {

    private ConcurrentHashMap<String, HeraTraceEtlConfig> heraTraceConfig = new ConcurrentHashMap<>();

    @Autowired
    private ManagerService managerService;

    @Value("${trace.config.get.gap.minutes}")
    private int configGetGapMinutes;
    @Value("${trace.config.get.init.delay.minutes}")
    private int configGetDelayMinutes;

    @PostConstruct
    public void init() {
        new ScheduledThreadPoolExecutor(1).scheduleAtFixedRate(() -> {
            try {
                List<HeraTraceEtlConfig> all = managerService.getAll(new HeraTraceConfigVo());
                if(all != null) {
                    for (HeraTraceEtlConfig config : all) {
                        heraTraceConfig.put(getServiceName(config), config);
                    }
                }
            }catch(Throwable t){
                log.error("schedule trace config error : ",t);
            }
        },  configGetDelayMinutes,configGetGapMinutes, TimeUnit.MINUTES);
    }

    public HeraTraceEtlConfig getConfig(String serviceName) {
        return heraTraceConfig.get(serviceName);
    }

    public void insert(HeraTraceEtlConfig config) {
        heraTraceConfig.putIfAbsent(getServiceName(config), config);
    }

    public void update(HeraTraceEtlConfig config) {
        heraTraceConfig.put(getServiceName(config), config);
    }

    public void delete(HeraTraceEtlConfig config) {
        heraTraceConfig.remove(getServiceName(config));
    }

    private String getServiceName(HeraTraceEtlConfig config) {
        StringBuilder sb = new StringBuilder();
        sb.append(config.getBindId()).append("_").append(config.getAppName().replaceAll("-", "_"));
        return sb.toString();
    }
}
