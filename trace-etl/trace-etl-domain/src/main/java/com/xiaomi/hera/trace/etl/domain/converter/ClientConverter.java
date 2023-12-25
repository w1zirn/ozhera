package com.xiaomi.hera.trace.etl.domain.converter;

import com.xiaomi.hera.trace.etl.domain.metrics.SpanType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientConverter {

    private String peerServiceName;

    private SpanType spanType;

    private String operation;

    private int responseCode;

    private int duration;

    private boolean error;
}
