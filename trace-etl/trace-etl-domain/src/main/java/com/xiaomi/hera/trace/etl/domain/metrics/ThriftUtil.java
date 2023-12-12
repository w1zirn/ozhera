package com.xiaomi.hera.trace.etl.domain.metrics;

import com.xiaomi.hera.tspandata.TAttributeKey;
import com.xiaomi.hera.tspandata.TAttributeType;
import com.xiaomi.hera.tspandata.TValue;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

import java.util.List;
import java.util.stream.Collectors;

public class ThriftUtil {

    public static final TProtocolFactory PROTOCOL_FACTORY = new TCompactProtocol.Factory();

    public static String valueToString(TAttributeKey key, TValue value) {
        TAttributeType type = key.getType();
        switch (type) {
            case STRING:
                return value.getStringValue();
            case BOOLEAN:
                return String.valueOf(value.isBoolValue());
            case LONG:
                return String.valueOf(value.getLongValue());
            case DOUBLE:
                return String.valueOf(value.getDoubleValue());
            case STRING_ARRAY:
                return separated(value.getStringArrayValue());
            case BOOLEAN_ARRAY:
                return separated(value.getBoolArrayValue());
            case LONG_ARRAY:
                return separated(value.getLongArrayValue());
            case DOUBLE_ARRAY:
                return separated(value.getDoubleArrayValue());
        }
        throw new IllegalStateException("Unknown attribute type: " + type);
    }

    private static String separated(List<?> values) {
        return values.stream()
                .map(Object::toString)
                .collect(Collectors.joining(","));
    }
}
