package com.xiaomi.hera.trace.etl.parser.converter;

import com.xiaomi.hera.trace.etl.domain.converter.ClientConverter;
import com.xiaomi.hera.trace.etl.domain.converter.LocalConverter;
import com.xiaomi.hera.trace.etl.domain.converter.ServerConverter;
import com.xiaomi.hera.trace.etl.domain.converter.TopologyConverter;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanType;
import com.xiaomi.hera.trace.etl.domain.trace.TraceAttributes;
import com.xiaomi.hera.trace.etl.metadata.OzHeraMetaDataService;
import com.xiaomi.hera.trace.etl.network.PeerService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ConverterServiceImpl implements ConverterService{

    @Autowired
    private PeerService peerService;

    @Autowired
    private OzHeraMetaDataService ozHeraMetaDataService;

    @Override
    public ClientConverter getClientConverter(SpanHolder spanHolder) {
        String peer = peerService.getPeer(spanHolder);
        if(StringUtils.isEmpty(peer)){
            return null;
        }
        Map<String, String> attributeMap = spanHolder.getAttributeMap();

        ClientConverter clientConverter = new ClientConverter();
        clientConverter.setOperationName(spanHolder.getOperationName());
        clientConverter.setApplication(spanHolder.getApplication());
        clientConverter.setError(spanHolder.getIsError());
        clientConverter.setDuration(spanHolder.getEndTime() - spanHolder.getStartTime());
        clientConverter.setEndTime(spanHolder.getEndTime());
        switch (spanHolder.getSpanType().spanTypeGroup()) {
            case RPC:
                if (spanHolder.getSpanType() == SpanType.grpc) {
                    clientConverter.setResponseCode(Integer.parseInt(attributeMap.getOrDefault(TraceAttributes.RPC_GRPC_STATUS_CODE, "0")));
                }
                clientConverter.setServiceName(attributeMap.getOrDefault(TraceAttributes.RPC_SERVICE, ""));
                clientConverter.setMethodName(attributeMap.getOrDefault(TraceAttributes.RPC_METHOD, ""));
                break;
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
                if (SpanType.redis.equals(clientConverter.getSpanType())) {
                    clientConverter.setMethodName("");
                    clientConverter.setServiceName(clientConverter.getDestServiceName() + "/" + dbName);
                }
                if (clientConverter.getDestServiceName() != null) {
                    clientConverter.setDataSource(clientConverter.getDestServiceName() + "/" + dbName);
                }else{
                    String dataSource = spanHolder.getAttribute(TraceAttributes.DB_CONNECTION_STRING) + "/" + spanHolder.getAttribute(TraceAttributes.DB_NAME);
                    clientConverter.setDataSource(dataSource);
                }
                break;
            case HTTP:
                String methodName = attributeMap.getOrDefault(TraceAttributes.HTTP_METHOD, "");
                clientConverter.setServiceName(methodName);
                // http server need this label
                clientConverter.setHttpMethod(methodName);
                clientConverter.setResponseCode(attributeMap.getOrDefault(TraceAttributes.HTTP_STATUS_CODE, "0"));
                clientConverter.setMethodName(StringUtils.defaultString(spanHolder.getName(), ""));
                break;
            case MQ:
                clientConverter.setServiceName(attributeMap.getOrDefault(TraceAttributes.MESSAGING_DESTINATION, ""));
                clientConverter.setMethodName(attributeMap.getOrDefault(TraceAttributes.MESSAGING_OPERATION, ""));
                break;
            default:
                clientConverter.setServiceName("");
                clientConverter.setMethodName("");
                break;
        }
        clientConverter.setSpanType(spanHolder.getSpanType());
        clientConverter.setSpanKind(spanHolder.getSpanKind());
        return clientConverter;
    }

    @Override
    public ServerConverter getServerConverter(SpanHolder spanHolder) {
        return null;
    }

    @Override
    public LocalConverter getLocalConverter(SpanHolder spanHolder) {
        return null;
    }

    @Override
    public TopologyConverter getTopologyConverter(SpanHolder spanHolder) {
        return null;
    }

    private String reduceString(String ori, int size) {
        if (StringUtils.isNotEmpty(ori) && ori.length() > size) {
            return ori.substring(0, size - 1);
        }
        return ori;
    }
}
