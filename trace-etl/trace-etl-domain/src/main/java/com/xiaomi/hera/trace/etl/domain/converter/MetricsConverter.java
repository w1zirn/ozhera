package com.xiaomi.hera.trace.etl.domain.converter;

import com.xiaomi.hera.trace.etl.domain.metrics.SpanKind;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanType;
import lombok.Builder;
import lombok.Data;

@Data
public class MetricsConverter {

    private String serviceName;

    private String methodName;

    private String operationName;

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
}
