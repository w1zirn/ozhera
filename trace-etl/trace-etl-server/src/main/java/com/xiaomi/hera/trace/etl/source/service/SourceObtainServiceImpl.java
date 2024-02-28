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
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanTypeGroup;
import com.xiaomi.hera.trace.etl.domain.source.DriverSourceDomain;
import com.xiaomi.hera.trace.etl.domain.source.ErrorTraceSourceDomain;
import com.xiaomi.hera.trace.etl.domain.trace.TraceAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class SourceObtainServiceImpl implements SourceObtainService{

    private final static String ERROR_TYPE_ERROR = "error";
    private final static String ERROR_TYPE_TIMEOUT = "timeout";
    private final static String DRIVER_ATTRIBUTE_KEY = "dbDriver";

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

    @Override
    public DriverSourceDomain getDriverSourceDomain(SpanHolder spanHolder) {
        if(DRIVER_ATTRIBUTE_KEY.equals(spanHolder.getSpan().getName())) {
            return DriverSourceDomain.builder()
                    .timeStamp(String.valueOf(System.currentTimeMillis()))
                    .appName(spanHolder.getApplication())
                    .domainPort(spanHolder.getAttribute(TraceAttributes.DB_DRIVER_DOMAIN_PORT))
                    .dataBaseName(spanHolder.getAttribute(TraceAttributes.DB_DRIVER_DBNAME))
                    .userName(spanHolder.getAttribute(TraceAttributes.DB_DRIVER_USER_NAME))
                    .password(spanHolder.getAttribute(TraceAttributes.DB_DRIVER_PASSWORD))
                    .type(spanHolder.getAttribute(TraceAttributes.DB_DRIVER_TYPE))
                    .prefixIndex(EnvConfig.DRIVER_TRACE_INDEX_PREFIX)
                    .build();
        }
        return null;
    }

    @Override
    public ErrorTraceSourceDomain getTraceSourceDomainInternal(MetricsConverter metricsConverter, String errorType) {
        ErrorTraceSourceDomain domain = new ErrorTraceSourceDomain();
        domain.setTraceId(metricsConverter.getTraceId());
        domain.setType(ErrorSpanUtils.toErrorSpanType(metricsConverter.getSpanType(),metricsConverter.getSpanKind()));
        domain.setDomain(esDomain);
        domain.setHost(metricsConverter.getServerIp());
        if(SpanTypeGroup.DATABASE.equals(metricsConverter.getSpanTypeGroup())){
            domain.setUrl(metricsConverter.getSql());
        }
        domain.setDataSource(metricsConverter.getDataSource());
        domain.setServiceName(metricsConverter.getMetricsApplication());
        domain.setTimestamp(String.valueOf(metricsConverter.getEndTime()));
        domain.setDuration(String.valueOf(metricsConverter.getDuration()));
        domain.setErrorType(errorType);
        domain.setErrorCode(String.valueOf(metricsConverter.getResponseCode()));
        domain.setServerEnv(metricsConverter.getServerEnv());
        domain.setPrefixIndex(EnvConfig.ERROR_TRACE_INDEX_PREFIX);
        return domain;
    }

}
