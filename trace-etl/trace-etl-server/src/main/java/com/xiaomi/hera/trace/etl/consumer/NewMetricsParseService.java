package com.xiaomi.hera.trace.etl.consumer;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.xiaomi.hera.trace.etl.api.MetricsRecordService;
import com.xiaomi.hera.trace.etl.api.SpanHoldService;
import com.xiaomi.hera.trace.etl.api.service.IMetricsParseService;
import com.xiaomi.hera.trace.etl.config.TraceConfig;
import com.xiaomi.hera.trace.etl.constant.SpanKind;
import com.xiaomi.hera.trace.etl.constant.SpanType;
import com.xiaomi.hera.trace.etl.domain.DriverDomain;
import com.xiaomi.hera.trace.etl.domain.HeraTraceEtlConfig;
import com.xiaomi.hera.trace.etl.domain.JaegerTracerDomain;
import com.xiaomi.hera.trace.etl.domain.MetricsParseResult;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.hera.trace.etl.service.HeraContextService;
import com.xiaomi.hera.trace.etl.service.WriteEsService;
import com.xiaomi.hera.trace.etl.util.ThriftUtil;
import com.xiaomi.hera.tspandata.TAttributeKey;
import com.xiaomi.hera.tspandata.TAttributes;
import com.xiaomi.hera.tspandata.TResource;
import com.xiaomi.hera.tspandata.TSpanData;
import com.xiaomi.hera.tspandata.TValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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
