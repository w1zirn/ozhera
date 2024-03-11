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
package com.xiaomi.hera.trace.etl.parser;

import com.xiaomi.hera.trace.etl.consumer.MultiMetricsCall;
import com.xiaomi.hera.trace.etl.converter.client.ClientMetricsConverter;
import com.xiaomi.hera.trace.etl.converter.local.LocalMetricsConverter;
import com.xiaomi.hera.trace.etl.converter.server.ServerMetricsConverter;
import com.xiaomi.hera.trace.etl.converter.topology.TopologyMetricsConverter;
import com.xiaomi.hera.trace.etl.domain.converter.MetricsConverter;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.hera.trace.etl.domain.source.DriverSourceDomain;
import com.xiaomi.hera.trace.etl.parser.converter.ConverterService;
import com.xiaomi.hera.trace.etl.skip.SpanSkipHandler;
import com.xiaomi.hera.trace.etl.source.DriverSourceReceive;
import com.xiaomi.hera.trace.etl.source.service.SourceObtainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class SpanParser {

    @Autowired
    private MultiMetricsCall multiMetrics;
    @Autowired
    private SpanSkipHandler spanFilter;

    @Autowired
    private ConverterService converterService;
    @Autowired
    private ClientMetricsConverter clientMetricsConverter;
    @Autowired
    private ServerMetricsConverter serverMetricsConverter;
    @Autowired
    private LocalMetricsConverter localMetricsConverter;
    @Autowired
    private TopologyMetricsConverter topologyMetricsConverter;
    @Autowired
    private SourceObtainService sourceObtainService;
    @Autowired
    private DriverSourceReceive driverSourceReceive;

    public void parseBefore(SpanHolder spanHolder) {
        // statistics span QPS
        multiMetrics.newCounter("trace_statistics_span_count", "application").with(spanHolder.getApplication()).add(1, spanHolder.getApplication());
        spanHolder.setSkip(spanFilter.spanSkip(spanHolder));
    }

    public void parseClient(SpanHolder spanHolder) {
        clientMetricsConverter.convert(converterService.getClientConverter(spanHolder));
    }

    public void parseServer(SpanHolder spanHolder) {
        serverMetricsConverter.convert(converterService.getServerConverter(spanHolder));
    }

    public void parseLocal(SpanHolder spanHolder) {
        localMetricsConverter.convert(converterService.getLocalConverter(spanHolder));
    }
    public void parseTopology(SpanHolder spanHolder) {
        MetricsConverter topologyConverter = converterService.getTopologyConverter(spanHolder);
        if(topologyConverter != null) {
            topologyMetricsConverter.convert(topologyConverter);
        }
    }

    /**
     * This method can serve as an extension method to implement operations beyond the conversion of metrics.
     * @param spanHolder
     */
    public void parseAfter(SpanHolder spanHolder){
        DriverSourceDomain driverSourceDomain = sourceObtainService.getDriverSourceDomain(spanHolder);
        if(driverSourceDomain != null){
            driverSourceReceive.submitDriverDomain(driverSourceDomain);
        }
    }
}
