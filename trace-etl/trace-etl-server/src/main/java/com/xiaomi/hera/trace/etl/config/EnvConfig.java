package com.xiaomi.hera.trace.etl.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * @Description To get config from a non-Spring management class, the configuration is set in when springboot starts
 */
@Configuration("envConfig")
public class EnvConfig {

    @Value("${server.type}")
    private String serverType;
    @Value("${es.trace.index.error.prefix}")
    private String errorTraceIndexPrefix;
    @Value("${es.trace.index.driver.prefix}")
    private String driverTraceIndexPrefix;

    public static String ERROR_TRACE_INDEX_PREFIX;

    public static String DRIVER_TRACE_INDEX_PREFIX;

    public static String SERVER_TYPE;

    @PostConstruct
    public void init() {
        ERROR_TRACE_INDEX_PREFIX = errorTraceIndexPrefix;
        DRIVER_TRACE_INDEX_PREFIX = driverTraceIndexPrefix;
        SERVER_TYPE = serverType;
    }
}
