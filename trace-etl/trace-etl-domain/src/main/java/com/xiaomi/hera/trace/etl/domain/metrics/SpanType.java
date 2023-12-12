package com.xiaomi.hera.trace.etl.domain.metrics;

/**
 * @Description
 * @Author dingtao
 * @Date 2022/4/2 9:41 上午
 */
public enum SpanType {
    oracle,
    mysql,
    redis,
    hbase,
    elasticsearch,
    mongodb,
    database,
    dubbo,
    thrift,
    grpc,
    apus,
    rpc,
    http,
    logic,
    kafka,
    rocketmq,
    unknown
}
