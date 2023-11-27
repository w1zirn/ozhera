package com.xiaomi.hera.trace.etl.consumer;

import com.xiaomi.hera.trace.etl.api.MetricsRecordService;
import com.xiaomi.hera.trace.etl.api.SpanHoldService;
import com.xiaomi.hera.trace.etl.api.service.IMetricsParseService;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.hera.tspandata.TSpanData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class NewMetricsParseService implements IMetricsParseService {

    private final List<MetricsRecordService> metricsParseServices;

    @Autowired
    public NewMetricsParseService(List<MetricsRecordService> metricsParseServices) {
        this.metricsParseServices = metricsParseServices;
    }

    @Autowired
    private SpanHoldService spanHoldService;

    @Override
    public void parse(TSpanData tSpanData) {
        SpanHolder spanHolder = spanHoldService.getSpanHolder(tSpanData);
        for (MetricsRecordService service : metricsParseServices) {
            service.record(spanHolder);
        }
    }
}
