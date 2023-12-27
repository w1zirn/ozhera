package com.xiaomi.hera.trace.etl.domain.metrics;

/**
 * @Description
 * @Author dingtao
 * @Date 2022/4/2 9:41 上午
 */
public enum SpanType {
    oracle(SpanTypeGroup.DATABASE),
    mysql(SpanTypeGroup.DATABASE),
    redis(SpanTypeGroup.DATABASE),
    hbase(SpanTypeGroup.DATABASE),
    elasticsearch(SpanTypeGroup.DATABASE),
    mongodb(SpanTypeGroup.DATABASE),
    database(SpanTypeGroup.DATABASE),
    dubbo(SpanTypeGroup.RPC),
    thrift(SpanTypeGroup.RPC),
    grpc(SpanTypeGroup.RPC),
    apus(SpanTypeGroup.RPC),
    rpc(SpanTypeGroup.RPC),
    http(SpanTypeGroup.HTTP),
    logic(SpanTypeGroup.LOGIC),
    kafka(SpanTypeGroup.MQ),
    rocketmq(SpanTypeGroup.MQ),
    custom_aano_method(SpanTypeGroup.CUSTOM_METHOD),
    unknown(SpanTypeGroup.UNKNOWN);

    private SpanTypeGroup spanTypeGroup;

    SpanType(SpanTypeGroup spanTypeGroup) {
        this.spanTypeGroup = spanTypeGroup;
    }

    public SpanTypeGroup spanTypeGroup() {
        return this.spanTypeGroup;
    }
}
