package com.xiaomi.hera.trace.etl.parser;

import com.xiaomi.hera.trace.etl.consumer.MultiMetricsCall;
import com.xiaomi.hera.trace.etl.domain.converter.ClientConverter;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.hera.trace.etl.metrics.client.ClientMetricsConverter;
import com.xiaomi.hera.trace.etl.skip.SpanFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpanParser {

    @Autowired
    private MultiMetricsCall multiMetrics;
    @Autowired
    private SpanFilter spanFilter;
    @Autowired
    private ClientMetricsConverter clientMetricsConverter;

    public void parseBefore(SpanHolder spanHolder) {
        // statistics span QPS
        multiMetrics.newCounter("trace_statistics_span_count", "application").with(spanHolder.getApplication()).add(1, spanHolder.getApplication());
        spanHolder.setSkip(spanFilter.filter(spanHolder));
    }

    public void parseClient(SpanHolder spanHolder) {
        ClientConverter clientConverter = new ClientConverter();
        clientConverter.setOperation(spanHolder.getOperationName());
//                .peerServiceName(PeerHelper.getPeer(spanHolder))
        clientConverter.setSpanType(spanHolder.getSpanType());
        clientConverter.setResponseCode(spanHolder.getStatusCode());
        clientMetricsConverter.convert(clientConverter);
    }

    public void parseServer(SpanHolder spanHolder) {
    }

    public void parseLocal(SpanHolder spanHolder) {
    }
}
