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
package com.xiaomi.hera.trace.etl.es.dubbo;

import com.xiaomi.hera.trace.etl.api.service.TraceEtlService;
import com.xiaomi.hera.trace.etl.domain.HeraTraceEtlConfig;
import com.xiaomi.hera.trace.etl.es.config.TraceConfig;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

/**
 * @Description
 * @Author dingtao
 * @Date 2022/4/25 3:09 pm
 */
@Service(cluster = "broadcast",group = "${dubbo.group}")
@ConditionalOnProperty(name = "trace.config.get.type", havingValue = "dubbo")
public class TraceEtlServiceImpl implements TraceEtlService {

    @Autowired
    private TraceConfig traceConfig;

    @Override
    public void insertConfig(HeraTraceEtlConfig config) {
        traceConfig.insert(config);
    }

    @Override
    public void updateConfig(HeraTraceEtlConfig config) {
        traceConfig.update(config);
    }

    @Override
    public void deleteConfig(HeraTraceEtlConfig config) {
        traceConfig.delete(config);
    }
}
