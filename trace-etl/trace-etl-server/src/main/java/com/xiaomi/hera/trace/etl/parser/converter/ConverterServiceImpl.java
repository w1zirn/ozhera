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
package com.xiaomi.hera.trace.etl.parser.converter;

import com.xiaomi.data.push.client.Pair;
import com.xiaomi.hera.trace.etl.api.AttributeService;
import com.xiaomi.hera.trace.etl.domain.converter.MetricsConverter;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanType;
import com.xiaomi.hera.trace.etl.domain.trace.TraceAttributes;
import com.xiaomi.hera.trace.etl.metadata.OzHeraMetaDataService;
import com.xiaomi.hera.trace.etl.network.PeerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.defaultString;

@Service
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class ConverterServiceImpl implements ConverterService {

    @Autowired
    private PeerService peerService;

    @Autowired
    private OzHeraMetaDataService ozHeraMetaDataService;

    @Autowired
    private AttributeService attributeService;

    @Autowired
    private OzHeraMetaDataService metaDataService;

    @Override
    public MetricsConverter getClientConverter(SpanHolder spanHolder) {
        Map<String, String> attributeMap = spanHolder.getAttributeMap();

        MetricsConverter clientConverter = new MetricsConverter();
        publicConvert(clientConverter, spanHolder);

        switch (spanHolder.getSpanType().spanTypeGroup()) {
            case DATABASE:
                clientConverter.setMethodName(attributeMap.getOrDefault(TraceAttributes.DB_OPERATION, ""));
                String stmt = attributeMap.getOrDefault(TraceAttributes.DB_STATEMENT, "");
                clientConverter.setSql(reduceString(stmt, 100));
                if ("".equals(clientConverter.getMethodName()) && !"".equals(stmt)) {
                    clientConverter.setMethodName(stmt.split(" ")[0]);
                }
                // for es type client call, take (http method + indexName) as methodName
                // like PUT /mione-staging-zgq-jaeger-span
                if (SpanType.elasticsearch.equals(clientConverter.getSpanType()) && StringUtils.isNotEmpty(clientConverter.getMethodName())) {
                    String methodName = clientConverter.getSql();
                    String[] arr = methodName.split("/");
                    if (arr.length >= 2) {
                        methodName = arr[0].concat("/").concat(arr[1]);
                    }
                    clientConverter.setMethodName(methodName);
                }
                String dbName = attributeMap.get(TraceAttributes.DB_NAME);
                String peerIpPort = peerService.getPeer(spanHolder);
                String destApp = metaDataService.getMetricsMetaDataName(peerIpPort);
                if (SpanType.redis.equals(clientConverter.getSpanType())) {
                    clientConverter.setServiceName(destApp == null ? peerIpPort : destApp + ":" + getPort(peerIpPort));
                }
                if (destApp != null) {
                    clientConverter.setDataSource(destApp + "/" + dbName);
                } else {
                    String dataSource = spanHolder.getAttribute(TraceAttributes.DB_CONNECTION_STRING) + "/" + dbName;
                    clientConverter.setDataSource(dataSource);
                }
                break;
            case HTTP:
                String destHost = attributeService.getHttpClientDestHost(spanHolder);
                clientConverter.setServiceName(defaultString(destHost, ""));
                break;
        }
        return clientConverter;
    }

    private String getPort(String ipPort){
        if(ipPort.contains(":")){
            return ipPort.split(":")[1];
        }
        return "";
    }

    @Override
    public MetricsConverter getServerConverter(SpanHolder spanHolder) {


        MetricsConverter serverConverter = new MetricsConverter();
        publicConvert(serverConverter, spanHolder);

        switch (spanHolder.getSpanType().spanTypeGroup()) {
            case HTTP:
                Map<String, String> attributeMap = spanHolder.getAttributeMap();
                // http server need this label
                String methodName = attributeMap.getOrDefault(TraceAttributes.HTTP_METHOD, "");
                serverConverter.setHttpMethod(methodName);
        }

        return serverConverter;
    }

    @Override
    public MetricsConverter getLocalConverter(SpanHolder spanHolder) {
        MetricsConverter localConverter = new MetricsConverter();
        publicConvert(localConverter, spanHolder);
        return localConverter;
    }

    @Override
    public MetricsConverter getTopologyConverter(SpanHolder spanHolder) {
        String destApp = "";
        // only statistics dubbo redis mysql topology now
        if (SpanType.dubbo.equals(spanHolder.getSpanType())) {
            String peer = peerService.getPeer(spanHolder);
            destApp = metaDataService.getMetricsMetaDataName(peer);
            if (destApp == null) {
                return null;
            }
        } else if (SpanType.mysql.equals(spanHolder.getSpanType())) {
            destApp = "mysql";
        } else if (SpanType.redis.equals(spanHolder.getSpanType())) {
            destApp = "redis";
        } else {
            return null;
        }
        MetricsConverter topologyConverter = new MetricsConverter();
        topologyConverter.setDestApp(destApp);
        topologyConverter.setMetricsApplication(spanHolder.getApplication());
        topologyConverter.setError(spanHolder.getIsError());
        topologyConverter.setDuration(spanHolder.getEndTime() - spanHolder.getStartTime());
        topologyConverter.setSpanTypeGroup(spanHolder.getSpanType().spanTypeGroup().name());
        return topologyConverter;
    }

    private String reduceString(String ori, int size) {
        if (StringUtils.isNotEmpty(ori) && ori.length() > size) {
            return ori.substring(0, size - 1);
        }
        return ori;
    }

    private void publicConvert(MetricsConverter converter, SpanHolder spanHolder) {
        Map<String, String> attributeMap = spanHolder.getAttributeMap();
        converter.setTraceId(spanHolder.getTraceId());
        converter.setOperationName(spanHolder.getOperationName());
        converter.setMetricsApplication(spanHolder.getApplication());
        converter.setApplication(spanHolder.getService());
        converter.setError(spanHolder.getIsError());
        converter.setDuration(spanHolder.getEndTime() - spanHolder.getStartTime());
        converter.setEndTime(spanHolder.getEndTime());
        converter.setResponseCode(spanHolder.getStatusCode());
        switch (spanHolder.getSpanType().spanTypeGroup()) {
            case RPC:
                if (spanHolder.getSpanType() == SpanType.grpc) {
                    converter.setResponseCode(Integer.parseInt(attributeMap.getOrDefault(TraceAttributes.RPC_GRPC_STATUS_CODE, "0")));
                }
                converter.setServiceName(attributeMap.getOrDefault(TraceAttributes.RPC_SERVICE, ""));
                converter.setMethodName(attributeMap.getOrDefault(TraceAttributes.RPC_METHOD, ""));
                break;
            case HTTP:
                converter.setMethodName(StringUtils.defaultString(spanHolder.getName(), ""));
                break;
            case MQ:
                converter.setMethodName(attributeMap.getOrDefault(TraceAttributes.MESSAGING_OPERATION, ""));
                String topic = attributeMap.get(TraceAttributes.MESSAGING_DESTINATION);
                if (topic == null) {
                    // support 1.26.0
                    topic = attributeMap.get(TraceAttributes.MESSAGING_DESTINATION_NAME);
                }
                converter.setTopic(topic);
                break;
            default:
                converter.setServiceName("");
                converter.setMethodName(spanHolder.getName());
                break;
        }
        converter.setSpanType(spanHolder.getSpanType());
        converter.setSpanKind(spanHolder.getSpanKind());
        converter.setServerIp(spanHolder.getServerIp());
        converter.setServerEnv(spanHolder.getServerEnv());
        converter.setServerEnvId(spanHolder.getServerEnvId());
    }
}
