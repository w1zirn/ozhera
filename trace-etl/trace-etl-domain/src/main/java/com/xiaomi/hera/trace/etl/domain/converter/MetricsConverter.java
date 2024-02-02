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
package com.xiaomi.hera.trace.etl.domain.converter;

import com.xiaomi.hera.trace.etl.domain.metrics.SpanKind;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanType;
import lombok.Data;

@Data
public class MetricsConverter {

    private String serviceName;

    private String methodName;

    private String operationName;

    /**
     * metricsApplication is the application displayed in the metrics label,
     * representing the value obtained by converting all hyphens to underscores.
     *
     * eg：111-test-demo
     * metricsApplication is 111_test_demo
     * application is 111-test-demo
     */
    private String metricsApplication;

    private String application;

    private SpanType spanType;

    private SpanKind spanKind;

    private int responseCode;

    private long duration;

    private boolean error;

    private String traceId;

    private String serverIp;

    private String dataSource;

    private String sql;

    private String topic;

    private String httpMethod;

    private long endTime;

    private String serverEnv;

    private String serverEnvId;

    private String destApp;

    private String spanTypeGroup;

    public MetricsConverter(MetricsConverter metricsConverter){
        this.serviceName = metricsConverter.getServiceName();
        this.methodName = metricsConverter.getMethodName();
        this.operationName = metricsConverter.getOperationName();
        this.metricsApplication = metricsConverter.getMetricsApplication();
        this.application = metricsConverter.getApplication();
        this.spanType = metricsConverter.getSpanType();
        this.spanKind = metricsConverter.getSpanKind();
        this.responseCode = metricsConverter.getResponseCode();
        this.duration = metricsConverter.getDuration();
        this.error = metricsConverter.isError();
        this.traceId = metricsConverter.getTraceId();
        this.serverIp = metricsConverter.getServerIp();
        this.dataSource = metricsConverter.getDataSource();
        this.sql = metricsConverter.getSql();
        this.topic = metricsConverter.getTopic();
        this.httpMethod = metricsConverter.getHttpMethod();
        this.endTime = metricsConverter.getEndTime();
        this.serverEnv = metricsConverter.getServerEnv();
        this.serverEnvId = metricsConverter.getServerEnvId();
        this.destApp = metricsConverter.getDestApp();
        this.spanTypeGroup = metricsConverter.getSpanTypeGroup();
    }

    public MetricsConverter(){}
}
