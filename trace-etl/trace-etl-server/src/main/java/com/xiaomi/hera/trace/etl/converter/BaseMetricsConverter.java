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
package com.xiaomi.hera.trace.etl.converter;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.xiaomi.data.push.client.Pair;
import com.xiaomi.hera.trace.etl.api.MetricsTagService;
import com.xiaomi.hera.trace.etl.config.TraceConfig;
import com.xiaomi.hera.trace.etl.consumer.MultiMetricsCall;
import com.xiaomi.hera.trace.etl.domain.HeraTraceEtlConfig;
import com.xiaomi.hera.trace.etl.domain.converter.MetricsConverter;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.commons.lang3.StringUtils.defaultString;

@Service
@Slf4j
public class BaseMetricsConverter {

    @Value("${metrics.prefix}")
    private String metricsPrefix;

    @NacosValue(value = "${query.slowtime.http}", autoRefreshed = true)
    private int httpSlowTime;

    @NacosValue(value = "${query.slowtime.dubbo}", autoRefreshed = true)
    private int dubboSlowTime;

    @NacosValue(value = "${query.slowtime.mysql}", autoRefreshed = true)
    private int mysqlSlowTime;

    @Autowired
    private TraceConfig traceConfig;

    @Autowired
    protected MultiMetricsCall multiMetricsCall;

    @Autowired
    private MetricsTagService metricsTagService;

    private final static int DEFAULT_SLOW_TIME = 1000;

    protected String[] tagKeys(String... customKeys) {
        String[] finalKeys = new String[customKeys.length + getCommonTagKeys().size()];
        for (int index = 0; index < getCommonTagKeys().size(); index++) {
            finalKeys[index] = getCommonTagKeys().get(index);
        }
        for (int index = getCommonTagKeys().size(); index < finalKeys.length; index++) {
            finalKeys[index] = customKeys[index - getCommonTagKeys().size()];
        }
        return finalKeys;
    }

    protected String[] tagValues(MetricsConverter metricsConverter, String... customValues) {
        List<String> commonTagValues = commonTagValues(metricsConverter);
        String[] finalValues = new String[customValues.length + commonTagValues.size()];
        for (int index = 0; index < commonTagValues.size(); index++) {
            finalValues[index] = commonTagValues.get(index);
        }
        for (int index = commonTagValues.size(); index < finalValues.length; index++) {
            finalValues[index] = customValues[index - commonTagValues.size()];
        }
        return finalValues;
    }

    protected List<String> getCommonTagKeys() {
        return metricsTagService.getCommonTagsKeys();
    }

    protected List<String> commonTagValues(MetricsConverter metricsConverter) {
        return metricsTagService.getCommonTagsValues(metricsConverter);
    }

    protected void metricsExtend(MetricsConverter metricsConverter) {

    }

    public String buildMetricName(String type, String name) {
        return getMetricsPrefix() + "_" + type + name;
    }

    public String buildMetricName(String type) {
        return getMetricsPrefix() + "_" + type;
    }

    protected String getMetricsPrefix() {
        return metricsPrefix;
    }

    public double getSlowThreshold(SpanType spanType, String application) {
        if (spanType == null || StringUtils.isEmpty(application)) {
            return DEFAULT_SLOW_TIME;
        }
        HeraTraceEtlConfig config = traceConfig.getConfig(application);
        if(config == null){
            return DEFAULT_SLOW_TIME;
        }
        switch (spanType) {
            case http:
                return config.getHttpSlowThreshold() == null ? httpSlowTime : config.getHttpSlowThreshold();
            case mysql:
                return config.getMysqlSlowThreshold() == null ? mysqlSlowTime : config.getMysqlSlowThreshold();
            case dubbo:
                return config.getDubboSlowThreshold() == null ? dubboSlowTime : config.getDubboSlowThreshold();
            default:
                return DEFAULT_SLOW_TIME;
        }
    }

    public Pair<String, String> splitIpPort(String dsn) {
        if(dsn.contains(":")){
            String[] ipPort = dsn.split(":");
            return new Pair<>(ipPort[0], ipPort[1]);
        }
        return new Pair<>("","");
    }

    public Map<String, String> parseRPCServiceAndMethod(MetricsConverter serverConverter) {
        String rpcService = defaultString(serverConverter.getServiceName(), "");
        String rpcMethod = defaultString(serverConverter.getMethodName(), "");
        Map<String, String> ret = new HashMap<>(2);
        ret.put("rpcService", rpcService);
        ret.put("rpcMethod", rpcMethod);
        if (StringUtils.isNotEmpty(rpcService) && StringUtils.isNotEmpty(rpcMethod)) {
            return ret;
        }
        String operationName = serverConverter.getOperationName();
        SpanType type = serverConverter.getSpanType();
        try {
            if (SpanType.thrift == type) {
                ret.put("rpcMethod", operationName);
                return ret;
            }
            String splitStr = "/";
            if (SpanType.apus == type) {
                splitStr = "$";
            }
            String[] arr = operationName.split(splitStr);
            if (arr.length > 1) {
                rpcService = arr[0];
                rpcMethod = arr[1];
            } else {
                rpcService = arr[0];
            }
            ret.put("rpcService", rpcService);
            ret.put("rpcMethod", rpcMethod);
            return ret;
        } catch (Exception e) {
            return ret;
        }
    }
}
