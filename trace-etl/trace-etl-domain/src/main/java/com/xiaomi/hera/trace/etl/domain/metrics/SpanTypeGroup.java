package com.xiaomi.hera.trace.etl.domain.metrics;

public enum SpanTypeGroup {
    DATABASE,
    HTTP,
    RPC,
    /**
     * Logic request only.
     */
    LOGIC,
    MQ,
    CUSTOM_METHOD,
    UNKNOWN
}
