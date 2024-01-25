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
package com.xiaomi.hera.trace.etl.source.service;

import com.xiaomi.hera.trace.etl.common.ErrorSpanUtils;
import com.xiaomi.hera.trace.etl.config.EnvConfig;
import com.xiaomi.hera.trace.etl.domain.converter.MetricsConverter;
import com.xiaomi.hera.trace.etl.domain.source.ErrorTraceSourceDomain;
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
                .url(metricsConverter.getOperationName())
                .dataSource(metricsConverter.getDataSource())
                .serviceName(metricsConverter.getApplication())
                .timestamp(String.valueOf(metricsConverter.getEndTime()))
                .duration(String.valueOf(metricsConverter.getDuration()))
                .errorType(errorType)
                .errorCode(String.valueOf(metricsConverter.getResponseCode()))
                .serverEnv(metricsConverter.getServerEnv())
                .prefixIndex(EnvConfig.ERROR_TRACE_INDEX_PREFIX)
                .build();
        return domain;
    }

}
