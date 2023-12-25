package com.xiaomi.hera.trace.etl.metrics.client;

import com.xiaomi.hera.trace.etl.consumer.MultiMetricsCall;
import com.xiaomi.hera.trace.etl.domain.converter.ClientConverter;
import com.xiaomi.hera.trace.etl.domain.metrics.MetricsBucket;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.hera.trace.etl.metrics.MetricsConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ClientMetricsConverter extends MetricsConverter {

    @Autowired
    private MultiMetricsCall multiMetricsCall;

     public void convert(ClientConverter clientConverter){
         String type;
         switch (clientConverter.getSpanType()){
             case http:
                 String[] httpKeys = tagKeys("serviceName", "methodName");
                 String[] httpValues = tagValues(clientConverter.getPeerServiceName(), clientConverter.getOperation());
                 String[] httpKeysWithCode = tagKeys("serviceName", "methodName", "errorCode");
                 String[] httpValuesWithCode = tagValues(clientConverter.getPeerServiceName(), clientConverter.getOperation(), String.valueOf(clientConverter.getResponseCode()));
                 type = "aop";
                 multiMetricsCall.newCounter(formatMetricName(type, "ClientTotalMethodCount"), httpKeys)
                         .with(httpValues)
                         .add(1, httpValues);
                 multiMetricsCall.newHistogram(formatMetricName(type, "ClientMethodTimeCount"), MetricsBucket.HTTP_BUCKET, httpKeys)
                         .with(httpValues)
                         .observe(clientConverter.getDuration(), httpValues);
                 if(clientConverter.isError()){
                     multiMetricsCall.newCounter(formatMetricName("http", "ClientError"), httpKeysWithCode)
                             .with(httpValuesWithCode)
                             .add(1, httpValuesWithCode);
                 }
         }
     }
}
