package com.xiaomi.hera.trace.etl.metrics.client;

import com.xiaomi.hera.trace.etl.consumer.MultiMetricsCall;
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

     public void convert(SpanHolder spanHolder){
         String type;
         switch (spanHolder.getSpanType()){
             case http:
//                 String[] httpKeys = tagKeys("serviceName", "methodName");
//                 String[] httpValues = tagValues(clientCall.getDestServiceName(), clientCall.getOperation());
//                 String[] httpKeysWithCode = tagKeys("serviceName", "methodName", "errorCode");
//                 String[] httpValuesWithCode = tagValues(clientCall, clientCall.getDestServiceName(), clientCall.getOperation(), String.valueOf(clientCall.getResponseCode()));
//                 type = "aop";
//                 multiMetricsCall.
         }
     }
}
