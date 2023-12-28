package com.xiaomi.hera.trace.etl.parser.converter;

import com.xiaomi.hera.trace.etl.domain.converter.ClientConverter;
import com.xiaomi.hera.trace.etl.domain.converter.LocalConverter;
import com.xiaomi.hera.trace.etl.domain.converter.ServerConverter;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.hera.trace.etl.domain.trace.TraceAttributes;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class ConverterServiceImpl implements ConverterService{
    @Override
    public ClientConverter getClientConverter(SpanHolder spanHolder) {
        ClientConverter clientConverter = new ClientConverter();
        clientConverter.setOperationName(spanHolder.getOperationName());
        clientConverter.setApplication(spanHolder.getApplication());
        clientConverter.setError(spanHolder.getIsError());
        clientConverter.setDuration(spanHolder.getEndTime() - spanHolder.getStartTime());
        clientConverter.setEndTime(spanHolder.getEndTime());
        String dataSource = spanHolder.getAttribute(TraceAttributes.DB_CONNECTION_STRING) + "/" + spanHolder.getAttribute(TraceAttributes.DB_NAME);
        clientConverter.setDataSource(dataSource);
        switch (spanHolder.getSpanType().spanTypeGroup()) {
            case RPC:
                if (clientCall.getType() == RequestType.GRPC) {
                    clientCall.setResponseCode(Integer.parseInt(attributeMap.getOrDefault(SemanticAttributes.RPC_GRPC_STATUS_CODE.getKey(), "0")));
                }
                clientCall.setOperationType(attributeMap.getOrDefault(SemanticAttributes.RPC_SERVICE.getKey(), ""));
                clientCall.setOperation(attributeMap.getOrDefault(SemanticAttributes.RPC_METHOD.getKey(), ""));
                break;
            case DATABASE:
                clientCall.setOperationType(attributeMap.getOrDefault(SemanticAttributes.DB_OPERATION.getKey(), ""));
                String stmt = attributeMap.getOrDefault(SemanticAttributes.DB_STATEMENT.getKey(), "");
                clientCall.setOperation(reduceString(stmt, 100));
                if ("".equals(clientCall.getOperationType()) && !"".equals(stmt)) {
                    clientCall.setOperationType(stmt.split(" ")[0]);
                }
                // for es type client call, take (http method + indexName) as operation
                // like PUT /cloud_mitelemetry_trace_staging@skywalking_instance_traffic-2023.03.10
                if (RequestType.ELASTICSEARCH.equals(clientCall.getType()) && StringUtils.isNotEmpty(clientCall.getOperation())) {
                    String operation = clientCall.getOperation();
                    String[] arr = operation.split("/");
                    if (arr.length >= 2) {
                        operation = arr[0].concat("/").concat(arr[1]);
                    }
                    clientCall.setOperation(operation);
                }
                if (RequestType.REDIS.equals(clientCall.getType())) {
                    clientCall.setOperation("");
                }
                String dbName = attributeMap.get(SemanticAttributes.DB_NAME.getKey());
                if (dbName != null) {
                    clientCall.setDestServiceName(clientCall.getDestServiceName() + "/" + dbName);
                }
                break;
            case HTTP:
                clientCall.setOperationType(attributeMap.getOrDefault(SemanticAttributes.HTTP_METHOD.getKey(), ""));
                clientCall.setOperation(StringUtils.defaultString(sourceEndpointName, ""));
                break;
            case MQ:
                clientCall.setOperationType(attributeMap.getOrDefault(SemanticAttributes.MESSAGING_DESTINATION.getKey(), ""));
                clientCall.setOperation(attributeMap.getOrDefault(SemanticAttributes.MESSAGING_OPERATION.getKey(), ""));
                break;
            default:
                clientCall.setOperationType("");
                clientCall.setOperation("");
                break;
        }
        clientConverter.setHttpMethod();
        clientConverter.setSpanType(spanHolder.getSpanType());
        clientConverter.setSpanKind(spanHolder.getSpanKind());
        clientConverter.setSql(spanHolder.getAttribute(TraceAttributes.DB_STATEMENT));
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
}
