package com.xiaomi.hera.trace.etl.source.service;

import com.xiaomi.hera.trace.etl.domain.converter.MetricsConverter;
import com.xiaomi.hera.trace.etl.source.domain.ErrorTraceSourceDomain;

public interface SourceObtainService {

    ErrorTraceSourceDomain getErrorTraceSourceDomain(MetricsConverter metricsConverter);

    ErrorTraceSourceDomain getSlowTraceSourceDomain(MetricsConverter metricsConverter);
}
