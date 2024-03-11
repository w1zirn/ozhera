package com.xiaomi.hera.trace.etl.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TraceConfigResp<T> {
    private int code;
    private String message;
    // the real type is LinkedTreeMap
    private T data;
    // not used
    private Object traceId;
    private Object attachments;
}
