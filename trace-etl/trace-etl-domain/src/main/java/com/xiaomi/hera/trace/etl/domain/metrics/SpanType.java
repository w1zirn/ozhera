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
