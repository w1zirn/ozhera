package com.xiaomi.hera.trace.etl.skip;

import com.xiaomi.hera.trace.etl.config.TraceConfig;
import com.xiaomi.hera.trace.etl.domain.HeraTraceEtlConfig;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SpanFilter {

    @Autowired
    private TraceConfig traceConfig;

    public boolean filter(SpanHolder spanHolder){
        if(StringUtils.isEmpty(spanHolder.getApplication())){
            return false;
        }
        HeraTraceEtlConfig heraTraceEtlConfig = getHeraTraceEtlConfig(spanHolder.getApplication());
        if (heraTraceEtlConfig == null) {
            return false;
        }
        if()
    }

    private HeraTraceEtlConfig getHeraTraceEtlConfig(String application) {
        HeraTraceEtlConfig heraTraceEtlConfig = traceConfig.getConfig(application);
        return heraTraceEtlConfig;
    }

    private boolean getSpanSkipAnalysisInternal(HeraTraceEtlConfig heraTraceEtlConfig, SpanHolder spanHolder) {
        if (exclude(heraTraceEtlConfig.getExcludeThread(), spanHolder.getAttribute(SemanticAttributes.THREAD_NAME.getKey()))) {
            return true;
        }
        if (exclude(heraTraceEtlConfig.getExcludeMethod(), spanHolder.getOperationName())) {
            return true;
        }
        if (exclude(heraTraceEtlConfig.getExcludeHttpUrl(), spanHolder.getAttribute(SemanticAttributes.HTTP_ROUTE.getKey()))) {
            return true;
        }
        if (exclude(heraTraceEtlConfig.getExcludeHttpUrl(), spanHolder.getAttribute(SemanticAttributes.HTTP_TARGET.getKey()))) {
            return true;
        }
        if (exclude(heraTraceEtlConfig.getExcludeHttpUrl(), spanHolder.getAttribute(SemanticAttributes.HTTP_URL.getKey()))) {
            return true;
        }
        if (exclude(heraTraceEtlConfig.getExcludeUa(), spanHolder.getAttribute(SemanticAttributes.HTTP_USER_AGENT.getKey()))) {
            return true;
        }
        if (exclude(heraTraceEtlConfig.getExcludeSql(), spanHolder.getAttribute(SemanticAttributes.DB_STATEMENT.getKey()))) {
            return true;
        }
        if (exclude(heraTraceEtlConfig.getHttpStatusError(), spanHolder.getAttribute(SemanticAttributes.HTTP_STATUS_CODE.getKey()))) {
            return true;
        }
        if (exclude(heraTraceEtlConfig.getGrpcCodeError(), spanHolder.getAttribute(SemanticAttributes.RPC_GRPC_STATUS_CODE.getKey()))) {
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
