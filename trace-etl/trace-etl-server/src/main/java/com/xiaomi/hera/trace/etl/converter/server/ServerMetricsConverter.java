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
package com.xiaomi.hera.trace.etl.converter.server;

import com.xiaomi.hera.trace.etl.domain.converter.ServerConverter;
import com.xiaomi.hera.trace.etl.domain.metrics.MetricsBucket;
import com.xiaomi.hera.trace.etl.converter.BaseMetricsConverter;
import com.xiaomi.hera.trace.etl.source.ErrorSourceReceive;
import com.xiaomi.hera.trace.etl.source.service.SourceObtainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class ServerMetricsConverter extends BaseMetricsConverter {

    @Autowired
    private ErrorSourceReceive errorSourceReceive;

    @Autowired
    private SourceObtainService sourceObtainService;

    public void convert(ServerConverter serverConverter) {
        String type;
        switch (serverConverter.getSpanType()) {
            case http:
                String[] httpKeys = tagKeys("methodName", "httpMethod");
                String[] httpValues = tagValues(serverConverter, serverConverter.getMethodName(), serverConverter.getHttpMethod());
                String[] httpKeysWithCode = tagKeys("methodName", "httpMethod", "errorCode");
                String[] httpValuesWithCode = tagValues(serverConverter, serverConverter.getMethodName(), serverConverter.getHttpMethod(), String.valueOf(serverConverter.getResponseCode()));
                type = "aop";
                multiMetricsCall.newCounter(buildMetricName(type, "TotalMethodCount"), httpKeys).with(httpValues).add(1, httpValues);
                multiMetricsCall.newHistogram(buildMetricName(type, "MethodTimeCount"), MetricsBucket.HTTP_BUCKET, httpKeys).with(httpValues).observe(serverConverter.getDuration(), httpValues);
                if (serverConverter.isError()) {
                    multiMetricsCall.newCounter(buildMetricName("http", "Error"), httpKeysWithCode).with(httpValuesWithCode).add(1, httpValuesWithCode);
                    errorSourceReceive.submitErrorTraceDomain(sourceObtainService.getErrorTraceSourceDomain(serverConverter));
                } else {
                    multiMetricsCall.newCounter(buildMetricName(type, "SuccessMethodCount"), httpKeys).with(httpValues).add(1, httpValues);
                    if (serverConverter.getDuration() > getSlowThreshold(serverConverter.getSpanType(), serverConverter.getApplication())) {
                        multiMetricsCall.newCounter(buildMetricName("http", "SlowQuery"), httpKeys).with(httpValues).add(1, httpValues);
                        errorSourceReceive.submitErrorTraceDomain(sourceObtainService.getSlowTraceSourceDomain(serverConverter));
                    }
                }
                break;
            case dubbo:
                type = "dubbo";
                String[] dubboKeys = tagKeys("serviceName", "methodName");
                Map<String, String> rpcMap = parseRPCServiceAndMethod(serverConverter);
                String[] dubboValues = tagValues(serverConverter, rpcMap.get("rpcService"), rpcMap.get("rpcMethod"));
                multiMetricsCall.newCounter(buildMetricName(type, "MethodCalledCount"), dubboKeys).with(dubboValues).add(1, dubboValues);
                multiMetricsCall.newHistogram(buildMetricName(type, "ProviderCount"), MetricsBucket.DUBBO_BUCKET, dubboKeys).with(dubboValues).observe(serverConverter.getDuration(), dubboValues);
                if (serverConverter.isError()) {
                    multiMetricsCall.newCounter(buildMetricName(type, "ProviderError"), dubboKeys).with(dubboValues).add(1, dubboValues);
                    errorSourceReceive.submitErrorTraceDomain(sourceObtainService.getErrorTraceSourceDomain(serverConverter));
                } else {
                    multiMetricsCall.newCounter(buildMetricName(type, "MethodCalledSuccessCount"), dubboKeys).with(dubboValues).add(1, dubboValues);
                    if (serverConverter.getDuration() > getSlowThreshold(serverConverter.getSpanType(), serverConverter.getApplication())) {
                        multiMetricsCall.newCounter(buildMetricName(type, "ProviderSlowQuery"), dubboKeys).with(dubboValues).add(1, dubboValues);
                        errorSourceReceive.submitErrorTraceDomain(sourceObtainService.getSlowTraceSourceDomain(serverConverter));
                    }
                }
                break;
            case kafka:
            case rocketmq:
                type = serverConverter.getSpanType().name() + "Consumer";
                String[] mqKeys = tagKeys("topic", "method");
                String[] mqValues = tagValues(serverConverter, serverConverter.getTopic(), serverConverter.getMethodName());
                multiMetricsCall.newCounter(buildMetricName(type), mqKeys).with(mqValues).add(1, mqValues);
                multiMetricsCall.newHistogram(buildMetricName(type, "TimeCost"), MetricsBucket.MQ_BUCKET, mqKeys).with(mqValues).observe(serverConverter.getDuration(), mqValues);
                if (serverConverter.isError()) {
                    multiMetricsCall.newCounter(buildMetricName(type, "Error"), mqKeys).with(mqValues).add(1, mqValues);
                    errorSourceReceive.submitErrorTraceDomain(sourceObtainService.getErrorTraceSourceDomain(serverConverter));
                } else {
                    multiMetricsCall.newCounter(buildMetricName(type, "Success"), mqKeys).with(mqValues).add(1, mqValues);
                    if (serverConverter.getDuration() > getSlowThreshold(serverConverter.getSpanType(), serverConverter.getApplication())) {
                        multiMetricsCall.newCounter(buildMetricName(type, "SlowConsume"), mqKeys).with(mqValues).add(1, mqValues);
                        errorSourceReceive.submitErrorTraceDomain(sourceObtainService.getSlowTraceSourceDomain(serverConverter));
                    }
                }
                break;
            case grpc:
            case thrift:
            case apus:
                type = serverConverter.getSpanType().name() + "Server";
                String[] rpcKeys = tagKeys("serviceName", "methodName");
                rpcMap = parseRPCServiceAndMethod(serverConverter);
                String[] rpcValues = tagValues(serverConverter, rpcMap.get("rpcService"), rpcMap.get("rpcMethod"));
                multiMetricsCall.newCounter(buildMetricName(type), rpcKeys).with(rpcValues).add(1, rpcValues);
                multiMetricsCall.newHistogram(buildMetricName(type, "TimeCost"), MetricsBucket.DUBBO_BUCKET, rpcKeys).with(rpcValues).observe(serverConverter.getDuration(), rpcValues);
                if (serverConverter.isError()) {
                    multiMetricsCall.newCounter(buildMetricName(type, "Error"), rpcKeys).with(rpcValues).add(1, rpcValues);
                } else {
                    multiMetricsCall.newCounter(buildMetricName(type, "Success"), rpcKeys).with(rpcValues).add(1, rpcValues);
                    if (serverConverter.getDuration() > getSlowThreshold(serverConverter.getSpanType(), serverConverter.getApplication())) {
                        multiMetricsCall.newCounter(buildMetricName(type, "SlowQuery"), rpcKeys).with(rpcValues).add(1, rpcValues);
                        errorSourceReceive.submitErrorTraceDomain(sourceObtainService.getSlowTraceSourceDomain(serverConverter));
                    }
                }
                break;
        }
        // extension
        metricsExtend(serverConverter);
    }
}
