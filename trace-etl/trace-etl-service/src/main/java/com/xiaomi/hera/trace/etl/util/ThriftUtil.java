package com.xiaomi.hera.trace.etl.util;

import com.xiaomi.hera.tspandata.TAttributeKey;
import com.xiaomi.hera.tspandata.TAttributeType;
import com.xiaomi.hera.tspandata.TValue;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocolFactory;

import java.util.List;

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
                return commaSeparated(value.getStringArrayValue());
            case BOOLEAN_ARRAY:
                return commaSeparated(value.getBoolArrayValue());
            case LONG_ARRAY:
                return commaSeparated(value.getLongArrayValue());
            case DOUBLE_ARRAY:
                return commaSeparated(value.getDoubleArrayValue());
        }
        throw new IllegalStateException("Unknown attribute type: " + type);
    }

    private static String commaSeparated(List<?> values) {
        StringBuilder builder = new StringBuilder();
        for (Object value : values) {
            if (builder.length() != 0) {
                builder.append(',');
            }
            builder.append(value);
        }
        return builder.toString();
    }
}
