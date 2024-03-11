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
package com.xiaomi.hera.trace.etl.converter.topology;

import com.xiaomi.hera.trace.etl.converter.BaseMetricsConverter;
import com.xiaomi.hera.trace.etl.domain.converter.MetricsConverter;
import com.xiaomi.hera.trace.etl.domain.metrics.MetricsBucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TopologyMetricsConverter extends BaseMetricsConverter {

    public void convert(MetricsConverter topologyConverter){
        String[] keys = new String[]{"application", "destApp", "type"};
        String[] values = new String[]{topologyConverter.getMetricsApplication(), topologyConverter.getDestApp(), topologyConverter.getSpanTypeGroup()};
        multiMetricsCall.newHistogram("app_call_relation_latency_client", MetricsBucket.HTTP_BUCKET, keys)
                .with(values)
                .observe(topologyConverter.getDuration(), values);
        if (topologyConverter.isError()) {
            multiMetricsCall.newCounter("app_call_relation_error_count_client", keys)
                    .with(values)
                    .add(1, values);
        } else {
            multiMetricsCall.newCounter("app_call_relation_success_count_client", keys)
                    .with(values)
                    .add(1, values);
        }
        metricsExtend(topologyConverter);
    }
}
