package com.xiaomi.hera.trace.etl.parser.converter;

import com.xiaomi.hera.trace.etl.api.AttributeService;
import com.xiaomi.hera.trace.etl.domain.converter.ClientConverter;
import com.xiaomi.hera.trace.etl.domain.converter.LocalConverter;
import com.xiaomi.hera.trace.etl.domain.converter.MetricsConverter;
import com.xiaomi.hera.trace.etl.domain.converter.ServerConverter;
import com.xiaomi.hera.trace.etl.domain.converter.TopologyConverter;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanType;
import com.xiaomi.hera.trace.etl.domain.trace.TraceAttributes;
import com.xiaomi.hera.trace.etl.metadata.OzHeraMetaDataService;
import com.xiaomi.hera.trace.etl.network.PeerService;
import com.xiaomi.mone.app.api.model.HeraMetaDataModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.defaultString;

@Service
public class ConverterServiceImpl implements ConverterService{

    @Autowired
    private PeerService peerService;

    @Autowired
    private OzHeraMetaDataService ozHeraMetaDataService;

    @Autowired
    private AttributeService attributeService;

    @Autowired
    private OzHeraMetaDataService metaDataService;

    @Override
    public ClientConverter getClientConverter(SpanHolder spanHolder) {
        Map<String, String> attributeMap = spanHolder.getAttributeMap();

        ClientConverter clientConverter = new ClientConverter();
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
                    String methodName = clientConverter.getMethodName();
                    String[] arr = methodName.split("/");
                    if (arr.length >= 2) {
                        methodName = arr[0].concat("/").concat(arr[1]);
                    }
                    clientConverter.setMethodName(methodName);
                }
                String dbName = attributeMap.get(TraceAttributes.DB_NAME);
                String destApp = metaDataService.getMetricsMetaDataName(peerService.getPeer(spanHolder));
                if (SpanType.redis.equals(clientConverter.getSpanType())) {
                    clientConverter.setMethodName("");
                    clientConverter.setServiceName(destApp + "/" + dbName);
                }
                if (destApp != null) {
                    clientConverter.setDataSource(destApp + "/" + dbName);
                }else{
                    String dataSource = spanHolder.getAttribute(TraceAttributes.DB_CONNECTION_STRING) + "/" + spanHolder.getAttribute(TraceAttributes.DB_NAME);
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

    @Override
    public ServerConverter getServerConverter(SpanHolder spanHolder) {


        ServerConverter serverConverter = new ServerConverter();
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
    public LocalConverter getLocalConverter(SpanHolder spanHolder) {
        LocalConverter localConverter = new LocalConverter();
        publicConvert(localConverter, spanHolder);
        return localConverter;
    }

    @Override
    public TopologyConverter getTopologyConverter(SpanHolder spanHolder) {
        String destApp = metaDataService.getMetricsMetaDataName(peerService.getPeer(spanHolder));
        if(destApp == null){
            return null;
        }
        TopologyConverter topologyConverter = new TopologyConverter();
        topologyConverter.setDestApp(destApp);
        topologyConverter.setSourceApp(spanHolder.getApplication());
        topologyConverter.setError(spanHolder.getIsError());
        topologyConverter.setDuration(spanHolder.getEndTime() - spanHolder.getStartTime());
        topologyConverter.setMetaDataType(spanHolder.getSpanType().spanTypeGroup().name());
        return topologyConverter;
    }

    private String reduceString(String ori, int size) {
        if (StringUtils.isNotEmpty(ori) && ori.length() > size) {
            return ori.substring(0, size - 1);
        }
        return ori;
    }

    private void publicConvert(MetricsConverter converter, SpanHolder spanHolder){
        Map<String, String> attributeMap = spanHolder.getAttributeMap();
        converter.setTraceId(spanHolder.getTraceId());
        converter.setOperationName(spanHolder.getOperationName());
        converter.setApplication(spanHolder.getApplication());
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
            case MQ:
                converter.setMethodName(attributeMap.getOrDefault(TraceAttributes.MESSAGING_OPERATION, ""));
                String topic = attributeMap.get(TraceAttributes.MESSAGING_DESTINATION);
                if(topic == null){
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
    }
}
