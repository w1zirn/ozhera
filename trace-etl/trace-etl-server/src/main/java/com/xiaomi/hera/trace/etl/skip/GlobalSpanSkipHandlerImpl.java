package com.xiaomi.hera.trace.etl.skip;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanKind;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanType;
import com.xiaomi.hera.trace.etl.domain.trace.TraceAttributes;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class GlobalSpanSkipHandlerImpl implements GlobalSpanSkipHandler {

    @NacosValue(value = "${query.excludeMethod}", autoRefreshed = true)
    private String excludeMethod;

    @NacosValue(value = "${query.exclude.httpServer}", autoRefreshed = true)
    private String excludeHttpServer;

    @NacosValue(value = "${query.excludeThread}", autoRefreshed = true)
    private String excludeThread;

    @NacosValue(value = "${query.excludeDB}", autoRefreshed = true)
    private String excludeDB;

    @NacosValue(value = "${query.excludeHttpurl}", autoRefreshed = true)
    private String excludeHttpurl;

    @NacosValue(value = "${query.excludeUA}", autoRefreshed = true)
    private String excludeUA;

    @NacosValue(value = "${query.dispatcher.excludeServiceName}", autoRefreshed = true)
    private String excludeServiceName;

    @Override
    public boolean spanSkip(SpanHolder spanHolder) {
        String serviceName = spanHolder.getSpan().getExtra().getServiceName();
        if (StringUtils.isEmpty(serviceName) || exclude(excludeServiceName, serviceName)) {
            return true;
        }
        String operationName = spanHolder.getName();
        if (StringUtils.isEmpty(operationName) || exclude(excludeMethod, operationName)) {
            return true;
        }
        if (exclude(excludeThread, spanHolder.getAttribute(TraceAttributes.THREAD_NAME))) {
            return true;
        }
        if (exclude(excludeHttpurl, spanHolder.getAttribute(TraceAttributes.HTTP_ROUTE))) {
            return true;
        }
        if (exclude(excludeHttpurl, spanHolder.getAttribute(TraceAttributes.HTTP_TARGET))) {
            return true;
        }
        if (exclude(excludeHttpurl, spanHolder.getAttribute(TraceAttributes.HTTP_URL))) {
            return true;
        }
        if (exclude(excludeHttpurl, spanHolder.getAttribute(TraceAttributes.NET_HOST_NAME))) {
            return true;
        }
        if (exclude(excludeHttpurl, spanHolder.getAttribute(TraceAttributes.NET_SOCK_HOST_ADDR))) {
            return true;
        }
        if (exclude(excludeUA, spanHolder.getAttribute(TraceAttributes.HTTP_USER_AGENT))) {
            return true;
        }
        if (exclude(excludeUA, spanHolder.getAttribute(TraceAttributes.HTTP_USER_AGENT_ORIGINAL))) {
            return true;
        }
        if (exclude(excludeDB, spanHolder.getAttribute(TraceAttributes.DB_STATEMENT))) {
            return true;
        }
        // filter http server
        if(SpanKind.Server.equals(spanHolder.getSpanKind()) && SpanType.http.equals(spanHolder.getSpanType())){
            if (exclude(excludeHttpServer, operationName)) {
                return true;
            }
        }
        return false;
    }

    private boolean exclude(String excludeList, String excludeString) {
        if(StringUtils.isEmpty(excludeString)){
            return false;
        }
        String[] splits = excludeList.split("\\|");
        for (String split : splits) {
            if (StringUtils.isNotEmpty(split) && excludeString.contains(split)) {
                return true;
            }
        }
        return false;
    }
}
