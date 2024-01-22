package com.xiaomi.hera.trace.etl.converter.local;

import com.xiaomi.hera.trace.etl.consumer.MultiMetricsCall;
import com.xiaomi.hera.trace.etl.domain.converter.LocalConverter;
import com.xiaomi.hera.trace.etl.domain.metrics.MetricsBucket;
import com.xiaomi.hera.trace.etl.converter.BaseMetricsConverter;
import com.xiaomi.hera.trace.etl.source.ErrorSourceReceive;
import com.xiaomi.hera.trace.etl.source.service.SourceObtainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class LocalMetricsConverter extends BaseMetricsConverter {

    @Autowired
    private ErrorSourceReceive errorSourceReceive;

    @Autowired
    private SourceObtainService sourceObtainService;

    public void convert(LocalConverter localConverter) {
        String type;
        switch (localConverter.getSpanType()) {
            case custom_aano_method:
                String[] customKeys = tagKeys("methodName");
                String[] customValues = tagValues(localConverter, localConverter.getMethodName());
                type = "CustomizeMethod";
                multiMetricsCall.newCounter(buildMetricName(type, "TotalCount"), customKeys)
                        .with(customValues)
                        .add(1, customValues);
                multiMetricsCall.newHistogram(buildMetricName(type, "TimeCost"), MetricsBucket.DUBBO_BUCKET, customKeys)
                        .with(customValues)
                        .observe(localConverter.getDuration(), customValues);
                if (localConverter.isError()) {
                    multiMetricsCall.newCounter(buildMetricName(type, "Error"), customKeys)
                            .with(customValues)
                            .add(1, customValues);
                    errorSourceReceive.submitErrorTraceDomain(sourceObtainService.getErrorTraceSourceDomain(localConverter));
                } else {
                    multiMetricsCall.newCounter(buildMetricName(type, "SuccessCount"), customKeys)
                            .with(customValues)
                            .add(1, customValues);
                    if (localConverter.getDuration() > getSlowThreshold(localConverter.getSpanType(), localConverter.getApplication())) {
                        multiMetricsCall.newCounter(buildMetricName(type, "SlowQuery"), customKeys)
                                .with(customValues)
                                .add(1, customValues);
                        errorSourceReceive.submitErrorTraceDomain(sourceObtainService.getSlowTraceSourceDomain(localConverter));
                    }
                }
                break;
        }
    }
}
