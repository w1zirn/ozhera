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
