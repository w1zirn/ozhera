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
