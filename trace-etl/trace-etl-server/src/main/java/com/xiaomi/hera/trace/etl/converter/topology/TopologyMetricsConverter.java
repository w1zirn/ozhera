package com.xiaomi.hera.trace.etl.converter.topology;

import com.xiaomi.hera.trace.etl.converter.BaseMetricsConverter;
import com.xiaomi.hera.trace.etl.domain.converter.TopologyConverter;
import com.xiaomi.hera.trace.etl.domain.metrics.MetricsBucket;

public class TopologyMetricsConverter extends BaseMetricsConverter {

    public void convert(TopologyConverter topologyConverter){
        String[] keys = tagKeys("application", "destApp", "type");
        String[] values = tagValues(topologyConverter, topologyConverter.getSourceApp(), topologyConverter.getDestApp(), topologyConverter.getMetaDataType());
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
    }
}
