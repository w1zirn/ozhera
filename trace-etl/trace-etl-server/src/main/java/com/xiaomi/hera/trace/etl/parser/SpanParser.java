package com.xiaomi.hera.trace.etl.parser;

import com.xiaomi.hera.trace.etl.consumer.MultiMetricsCall;
import com.xiaomi.hera.trace.etl.converter.client.ClientMetricsConverter;
import com.xiaomi.hera.trace.etl.converter.local.LocalMetricsConverter;
import com.xiaomi.hera.trace.etl.converter.server.ServerMetricsConverter;
import com.xiaomi.hera.trace.etl.converter.topology.TopologyMetricsConverter;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.hera.trace.etl.parser.converter.ConverterService;
import com.xiaomi.hera.trace.etl.skip.SpanSkipHandler;
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
    private TopologyMetricsConverter topoMetricsConverter;

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
        topoMetricsConverter.convert(converterService.getTopologyConverter(spanHolder));
    }
}
