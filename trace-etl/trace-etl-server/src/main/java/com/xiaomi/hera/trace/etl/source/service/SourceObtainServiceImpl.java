package com.xiaomi.hera.trace.etl.source.service;

import com.xiaomi.hera.trace.etl.common.ErrorSpanUtils;
import com.xiaomi.hera.trace.etl.domain.converter.MetricsConverter;
import com.xiaomi.hera.trace.etl.source.domain.ErrorTraceSourceDomain;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class SourceObtainServiceImpl implements SourceObtainService{

    private final static String ERROR_TYPE_ERROR = "error";
    private final static String ERROR_TYPE_TIMEOUT = "timeout";

    @Value("${es.domain}")
    private String esDomain;


    @Override
    public ErrorTraceSourceDomain getErrorTraceSourceDomain(MetricsConverter metricsConverter) {
        return getTraceSourceDomainInternal(metricsConverter, ERROR_TYPE_ERROR);
    }

    @Override
    public ErrorTraceSourceDomain getSlowTraceSourceDomain(MetricsConverter metricsConverter) {
        return getTraceSourceDomainInternal(metricsConverter, ERROR_TYPE_TIMEOUT);
    }

    private ErrorTraceSourceDomain getTraceSourceDomainInternal(MetricsConverter metricsConverter, String errorType) {
        ErrorTraceSourceDomain domain = ErrorTraceSourceDomain.builder()
                .traceId(metricsConverter.getTraceId())
                .type(ErrorSpanUtils.toErrorSpanType(metricsConverter.getSpanType(),metricsConverter.getSpanKind()))
                .domain(esDomain)
                .host(metricsConverter.getServerIp())
                .url(metricsConverter.getOperation())
                .dataSource(metricsConverter.getDataSource())
                .serviceName(metricsConverter.getServiceName())
                .timestamp(String.valueOf(metricsConverter.getEndTime()))
                .duration(String.valueOf(metricsConverter.getDuration()))
                .errorType(errorType)
                .errorCode(String.valueOf(metricsConverter.getResponseCode()))
                .serverEnv(metricsConverter.getServerEnv())
                .build();
        return domain;
    }

}
