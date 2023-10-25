package com.xiaomi.hera.trace.etl.record;

import com.xiaomi.hera.trace.etl.api.MetricsRecordService;
import com.xiaomi.hera.trace.etl.api.MetricsService;
import com.xiaomi.hera.trace.etl.consumer.MultiMetricsCall;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HttpMetricsRecordService implements MetricsRecordService {

    private static final String TYPE = "aop";

    @Autowired
    private MetricsService metricsNameService;

    @Autowired
    private MultiMetricsCall multiMetrics;
    
    @Override
    public void record(SpanHolder span) {
//        if (SpanKind.SERVER.equals(span.getKind())) {
//            multiMetrics.newCounter("jaeger_" + jtc.getType() + "TotalMethodCount", "methodName", "application", "serverIp", "serverEnv", "serverEnvId", "functionModule", "functionName", "functionId", "mimeterSceneTask", "mimeterSerialLink", "mimeterApi", "containerName")
//                    .with(jtc.getMethod(), metricsServiceName, jtc.getServerIp(), jtc.getServiceEnv(), jtc.getServiceEnvId(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getMiMeterSceneId(), jtc.getMiMeterTraceId(), jtc.getMiMeterInterfaceId(), jtc.getContainerName())
//                    .add(1, jtc.getMethod(), metricsServiceName, jtc.getServerIp(), jtc.getServiceEnv(), jtc.getServiceEnvId(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getMiMeterSceneId(), jtc.getMiMeterTraceId(), jtc.getMiMeterInterfaceId(), jtc.getContainerName());
//            // 成功失败数
//            if (jtc.isSuccess()) {
//                multiMetrics.newCounter("jaeger_" + jtc.getType() + "SuccessMethodCount", "methodName", "application", "serverIp", "serverEnv", "serverEnvId", "functionModule", "functionName", "functionId", "mimeterSceneTask", "mimeterSerialLink", "mimeterApi", "containerName")
//                        .with(jtc.getMethod(), metricsServiceName, jtc.getServerIp(), jtc.getServiceEnv(), jtc.getServiceEnvId(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getMiMeterSceneId(), jtc.getMiMeterTraceId(), jtc.getMiMeterInterfaceId(), jtc.getContainerName())
//                        .add(1, jtc.getMethod(), metricsServiceName, jtc.getServerIp(), jtc.getServiceEnv(), jtc.getServiceEnvId(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getMiMeterSceneId(), jtc.getMiMeterTraceId(), jtc.getMiMeterInterfaceId(), jtc.getContainerName());
//                // 慢查询
//                if (jtc.getDuration() > (config == null ? httpSlowTime : config.getHttpSlowThreshold())) {
//                    multiMetrics.newCounter("jaeger_httpSlowQuery", "methodName", "application", "serverIp", "serverEnv", "serverEnvId", "functionModule", "functionName", "functionId", "mimeterSceneTask", "mimeterSerialLink", "mimeterApi", "containerName")
//                            .with(jtc.getMethod(), metricsServiceName, jtc.getServerIp(), jtc.getServiceEnv(), jtc.getServiceEnvId(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getMiMeterSceneId(), jtc.getMiMeterTraceId(), jtc.getMiMeterInterfaceId(), jtc.getContainerName())
//                            .add(1, jtc.getMethod(), metricsServiceName, jtc.getServerIp(), jtc.getServiceEnv(), jtc.getServiceEnvId(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getMiMeterSceneId(), jtc.getMiMeterTraceId(), jtc.getMiMeterInterfaceId(), jtc.getContainerName());
//                    esService.submitErrorEsTrace(jtc.getMethod(), metricsServiceName, jtc.getTraceId(), "http", jtc.getServerIp(), String.valueOf(jtc.getEndTime()), "", String.valueOf(jtc.getDuration()), "timeout", jtc.getHttpCode(), jtc.getServiceEnv(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getContainerName());
//                }
//            } else {
//                multiMetrics.newCounter("jaeger_httpError", "methodName", "application", "serverIp", "errorCode", "serverEnv", "serverEnvId", "functionModule", "functionName", "functionId", "mimeterSceneTask", "mimeterSerialLink", "mimeterApi", "containerName")
//                        .with(jtc.getMethod(), metricsServiceName, jtc.getServerIp(), jtc.getHttpCode(), jtc.getServiceEnv(), jtc.getServiceEnvId(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getMiMeterSceneId(), jtc.getMiMeterTraceId(), jtc.getMiMeterInterfaceId(), jtc.getContainerName())
//                        .add(1, jtc.getMethod(), metricsServiceName, jtc.getServerIp(), jtc.getHttpCode(), jtc.getServiceEnv(), jtc.getServiceEnvId(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getMiMeterSceneId(), jtc.getMiMeterTraceId(), jtc.getMiMeterInterfaceId(), jtc.getContainerName());
//                esService.submitErrorEsTrace(jtc.getMethod(), metricsServiceName, jtc.getTraceId(), "http", jtc.getServerIp(), String.valueOf(jtc.getEndTime()), "", String.valueOf(jtc.getDuration()), "error", jtc.getHttpCode(), jtc.getServiceEnv(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getContainerName());
//            }
//            multiMetrics.newHistogram("jaeger_" + jtc.getType() + "MethodTimeCount", aopDubboBuckets, new String[]{"methodName", "application", "serverIp", "serverEnv", "serverEnvId", "functionModule", "functionName", "functionId", "mimeterSceneTask", "mimeterSerialLink", "mimeterApi", "containerName"})
//                    .with(jtc.getMethod(), metricsServiceName, jtc.getServerIp(), jtc.getServiceEnv(), jtc.getServiceEnvId(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getMiMeterSceneId(), jtc.getMiMeterTraceId(), jtc.getMiMeterInterfaceId(), jtc.getContainerName())
//                    .observe(jtc.getDuration(), jtc.getMethod(), metricsServiceName, jtc.getServerIp(), jtc.getServiceEnv(), jtc.getServiceEnvId(), jtc.getFunctionModule(), jtc.getFunctionId(), jtc.getFunctionName(), jtc.getMiMeterSceneId(), jtc.getMiMeterTraceId(), jtc.getMiMeterInterfaceId(), jtc.getContainerName());
//            multiMetrics.newHistogram("jaeger_" + jtc.getType() + "MethodTimeCount_without_methodName", aopDubboBuckets, new String[]{"application", "serverIp", "serverEnv", "serverEnvId", "functionModule", "functionName", "functionId", "mimeterSceneTask", "mimeterSerialLink", "mimeterApi", "containerName"})
//                    .with(metricsServiceName, jtc.getServerIp(), jtc.getServiceEnv(), jtc.getServiceEnvId(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getMiMeterSceneId(), jtc.getMiMeterTraceId(), jtc.getMiMeterInterfaceId(), jtc.getContainerName())
//                    .observe(jtc.getDuration(), metricsServiceName, jtc.getServerIp(), jtc.getServiceEnv(), jtc.getServiceEnvId(), jtc.getFunctionModule(), jtc.getFunctionId(), jtc.getFunctionName(), jtc.getMiMeterSceneId(), jtc.getMiMeterTraceId(), jtc.getMiMeterInterfaceId(), jtc.getContainerName());
//        } else if (SpanKind.CLIENT.equals(jtc.getKind())) {
//            multiMetrics.newCounter("jaeger_" + jtc.getType() + "ClientTotalMethodCount", "methodName", "application", "serverIp", "serverEnv", "serverEnvId", "functionModule", "functionName", "functionId", "mimeterSceneTask", "mimeterSerialLink", "mimeterApi", "serviceName", "containerName")
//                    .with(jtc.getMethod(), metricsServiceName, jtc.getServerIp(), jtc.getServiceEnv(), jtc.getServiceEnvId(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getMiMeterSceneId(), jtc.getMiMeterTraceId(), jtc.getMiMeterInterfaceId(), jtc.getDomain(), jtc.getContainerName())
//                    .add(1, jtc.getMethod(), metricsServiceName, jtc.getServerIp(), jtc.getServiceEnv(), jtc.getServiceEnvId(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getMiMeterSceneId(), jtc.getMiMeterTraceId(), jtc.getMiMeterInterfaceId(), jtc.getDomain(), jtc.getContainerName());
//            // 成功失败数
//            if (jtc.isSuccess()) {
//                multiMetrics.newCounter("jaeger_" + jtc.getType() + "ClientSuccessMethodCount", "methodName", "application", "serverIp", "serverEnv", "serverEnvId", "functionModule", "functionName", "functionId", "mimeterSceneTask", "mimeterSerialLink", "mimeterApi", "serviceName", "containerName")
//                        .with(jtc.getMethod(), metricsServiceName, jtc.getServerIp(), jtc.getServiceEnv(), jtc.getServiceEnvId(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getMiMeterSceneId(), jtc.getMiMeterTraceId(), jtc.getMiMeterInterfaceId(), jtc.getDomain(), jtc.getContainerName())
//                        .add(1, jtc.getMethod(), metricsServiceName, jtc.getServerIp(), jtc.getServiceEnv(), jtc.getServiceEnvId(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getMiMeterSceneId(), jtc.getMiMeterTraceId(), jtc.getMiMeterInterfaceId(), jtc.getDomain(), jtc.getContainerName());
//                // 慢查询
//                if (jtc.getDuration() > (config == null ? httpSlowTime : config.getHttpSlowThreshold())) {
//                    multiMetrics.newCounter("jaeger_httpClientSlowQuery", "methodName", "application", "serverIp", "serverEnv", "serverEnvId", "functionModule", "functionName", "functionId", "mimeterSceneTask", "mimeterSerialLink", "mimeterApi", "serviceName", "containerName")
//                            .with(jtc.getMethod(), metricsServiceName, jtc.getServerIp(), jtc.getServiceEnv(), jtc.getServiceEnvId(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getMiMeterSceneId(), jtc.getMiMeterTraceId(), jtc.getMiMeterInterfaceId(), jtc.getDomain(), jtc.getContainerName())
//                            .add(1, jtc.getMethod(), metricsServiceName, jtc.getServerIp(), jtc.getServiceEnv(), jtc.getServiceEnvId(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getMiMeterSceneId(), jtc.getMiMeterTraceId(), jtc.getMiMeterInterfaceId(), jtc.getDomain(), jtc.getContainerName());
//                    esService.submitErrorEsTrace(jtc.getMethod(), metricsServiceName, jtc.getTraceId(), "http_client", jtc.getServerIp(), String.valueOf(jtc.getEndTime()), "", String.valueOf(jtc.getDuration()), "timeout", jtc.getHttpCode(), jtc.getServiceEnv(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getContainerName());
//                }
//            } else {
//                multiMetrics.newCounter("jaeger_httpClientError", "methodName", "application", "serverIp", "errorCode", "serverEnv", "serverEnvId", "functionModule", "functionName", "functionId", "mimeterSceneTask", "mimeterSerialLink", "mimeterApi", "serviceName", "containerName")
//                        .with(jtc.getMethod(), metricsServiceName, jtc.getServerIp(), jtc.getHttpCode(), jtc.getServiceEnv(), jtc.getServiceEnvId(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getMiMeterSceneId(), jtc.getMiMeterTraceId(), jtc.getMiMeterInterfaceId(), jtc.getDomain(), jtc.getContainerName())
//                        .add(1, jtc.getMethod(), metricsServiceName, jtc.getServerIp(), jtc.getHttpCode(), jtc.getServiceEnv(), jtc.getServiceEnvId(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getMiMeterSceneId(), jtc.getMiMeterTraceId(), jtc.getMiMeterInterfaceId(), jtc.getDomain(), jtc.getContainerName());
//                esService.submitErrorEsTrace(jtc.getMethod(), metricsServiceName, jtc.getTraceId(), "http_client", jtc.getServerIp(), String.valueOf(jtc.getEndTime()), "", String.valueOf(jtc.getDuration()), "error", jtc.getHttpCode(), jtc.getServiceEnv(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getContainerName());
//            }
//            multiMetrics.newHistogram("jaeger_" + jtc.getType() + "ClientMethodTimeCount", aopDubboBuckets, new String[]{"methodName", "application", "serverIp", "serverEnv", "serverEnvId", "functionModule", "functionName", "functionId", "mimeterSceneTask", "mimeterSerialLink", "mimeterApi", "serviceName", "containerName"})
//                    .with(jtc.getMethod(), metricsServiceName, jtc.getServerIp(), jtc.getServiceEnv(), jtc.getServiceEnvId(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getMiMeterSceneId(), jtc.getMiMeterTraceId(), jtc.getMiMeterInterfaceId(), jtc.getDomain(), jtc.getContainerName())
//                    .observe(jtc.getDuration(), jtc.getMethod(), metricsServiceName, jtc.getServerIp(), jtc.getServiceEnv(), jtc.getServiceEnvId(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getMiMeterSceneId(), jtc.getMiMeterTraceId(), jtc.getMiMeterInterfaceId(), jtc.getDomain(), jtc.getContainerName());
//            multiMetrics.newHistogram("jaeger_" + jtc.getType() + "ClientMethodTimeCount_without_methodName", aopDubboBuckets, new String[]{"application", "serverIp", "serverEnv", "serverEnvId", "functionModule", "functionName", "functionId", "mimeterSceneTask", "mimeterSerialLink", "mimeterApi", "serviceName", "containerName"})
//                    .with(metricsServiceName, jtc.getServerIp(), jtc.getServiceEnv(), jtc.getServiceEnvId(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getMiMeterSceneId(), jtc.getMiMeterTraceId(), jtc.getMiMeterInterfaceId(), jtc.getDomain(), jtc.getContainerName())
//                    .observe(jtc.getDuration(), metricsServiceName, jtc.getServerIp(), jtc.getServiceEnv(), jtc.getServiceEnvId(), jtc.getFunctionModule(), jtc.getFunctionName(), jtc.getFunctionId(), jtc.getMiMeterSceneId(), jtc.getMiMeterTraceId(), jtc.getMiMeterInterfaceId(), jtc.getDomain(), jtc.getContainerName());
//        }
    }
}
