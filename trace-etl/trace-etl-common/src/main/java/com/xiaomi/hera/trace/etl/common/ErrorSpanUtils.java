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
package com.xiaomi.hera.trace.etl.common;

import com.xiaomi.hera.trace.etl.domain.metrics.SpanKind;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ErrorSpanUtils {
    public static String toErrorSpanType(SpanType spanType, SpanKind spanKind) {
        String type = "";
        switch (spanType) {
            case http:
                type = spanType.name().toLowerCase();
                if (spanKind == SpanKind.Client) {
                    type = type + "_client";
                }
                break;
            case dubbo:
                type = spanType.name().toLowerCase();
                if (spanKind == SpanKind.Client) {
                    type = type + "_consumer";
                } else if (spanKind == SpanKind.Server) {
                    type = type + "_provider";
                }
                break;
            case grpc:
            case thrift:
            case apus:
                type = spanType.name().toLowerCase();
                if (spanKind == SpanKind.Client) {
                    type = type + "_client";
                } else if (spanKind == SpanKind.Server) {
                    type = type + "_server";
                }
                break;
            case elasticsearch:
            case redis:
            case oracle:
            case mysql:
                type = spanType.name().toLowerCase();
                break;
            case kafka:
            case rocketmq:
                type = spanType.name().toLowerCase();
                if (spanKind == SpanKind.Client) {
                    type = type + "_producer";
                } else if (spanKind == SpanKind.Server) {
                    type = type + "_consumer";
                }
                break;
            case custom_aano_method:
                type = "customize_method";
                break;
        }
        return type;
    }
}
