package com.xiaomi.hera.trace.etl.domain.util;

public enum PeerType {
    TCP("tcp"),
    REDIS("redis"),
    MYSQL("mysql"),
    ORACLE("oracle"),
    HBASE("hbase"),
    FALCON_AGENT("falcon-agent"),
    LCS_AGENT("lcs-agent"),
    MQ("mq"),
    ELASTICSEARCH("elasticsearch");

    private final String value;

    PeerType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String getSchema() {
        return this.value + "://";
    }
}
