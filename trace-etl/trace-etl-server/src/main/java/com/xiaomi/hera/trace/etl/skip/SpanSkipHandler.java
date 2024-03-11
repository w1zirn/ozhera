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
package com.xiaomi.hera.trace.etl.skip;

import com.xiaomi.hera.trace.etl.config.TraceConfig;
import com.xiaomi.hera.trace.etl.domain.HeraTraceEtlConfig;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.hera.trace.etl.domain.trace.TraceAttributes;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpanSkipHandler {

    @Autowired
    private TraceConfig traceConfig;

    @Autowired
    private GlobalSpanSkipHandler globalSpanFilterHandler;

    public boolean spanSkip(SpanHolder spanHolder){
        if(StringUtils.isEmpty(spanHolder.getApplication())){
            return false;
        }
        if(globalSpanFilterHandler.spanSkip(spanHolder)){
            return true;
        }
        HeraTraceEtlConfig heraTraceEtlConfig = getHeraTraceEtlConfig(spanHolder.getApplication());
        if (heraTraceEtlConfig == null) {
            return false;
        }
        return filterByEtlConfig(heraTraceEtlConfig, spanHolder);
    }

    private HeraTraceEtlConfig getHeraTraceEtlConfig(String application) {
        HeraTraceEtlConfig heraTraceEtlConfig = traceConfig.getConfig(application);
        return heraTraceEtlConfig;
    }

    private boolean filterByEtlConfig(HeraTraceEtlConfig heraTraceEtlConfig, SpanHolder spanHolder) {
        if (exclude(heraTraceEtlConfig.getExcludeThread(), spanHolder.getAttribute(TraceAttributes.THREAD_NAME))) {
            return true;
        }
        if (exclude(heraTraceEtlConfig.getExcludeMethod(), spanHolder.getOperationName())) {
            return true;
        }
        if (exclude(heraTraceEtlConfig.getExcludeHttpUrl(), spanHolder.getAttribute(TraceAttributes.HTTP_ROUTE))) {
            return true;
        }
        if (exclude(heraTraceEtlConfig.getExcludeHttpUrl(), spanHolder.getAttribute(TraceAttributes.HTTP_TARGET))) {
            return true;
        }
        if (exclude(heraTraceEtlConfig.getExcludeHttpUrl(), spanHolder.getAttribute(TraceAttributes.HTTP_URL))) {
            return true;
        }
        if (exclude(heraTraceEtlConfig.getExcludeUa(), spanHolder.getAttribute(TraceAttributes.HTTP_USER_AGENT))) {
            return true;
        }
        if (exclude(heraTraceEtlConfig.getExcludeSql(), spanHolder.getAttribute(TraceAttributes.DB_STATEMENT))) {
            return true;
        }
        if (exclude(heraTraceEtlConfig.getHttpStatusError(), spanHolder.getAttribute(TraceAttributes.HTTP_STATUS_CODE))) {
            return true;
        }
        if (exclude(heraTraceEtlConfig.getGrpcCodeError(), spanHolder.getAttribute(TraceAttributes.RPC_GRPC_STATUS_CODE))) {
            return true;
        }
        if (exclude(heraTraceEtlConfig.getExceptionError(), spanHolder.getExceptions())) {
            return true;
        }
        return false;
    }

    public boolean exclude(String excludeList, String excludeString) {
        if (StringUtils.isEmpty(excludeList) || StringUtils.isEmpty(excludeString)) {
            return false;
        }
        String[] splits = excludeList.split("\\|");
        if (isNumberMatch(splits))  {
            for (String split : splits) {
                if (StringUtils.isNotEmpty(split) && excludeString.equals(split)) {
                    return true;
                }
            }
        } else {
            for (String split : splits) {
                if (StringUtils.isNotEmpty(split) && excludeString.contains(split)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isNumberMatch(String[] excludeList) {
        for (String s : excludeList) {
            if (StringUtils.isNumeric(s)) {
                return true;
            }
        }
        return false;
    }
}
