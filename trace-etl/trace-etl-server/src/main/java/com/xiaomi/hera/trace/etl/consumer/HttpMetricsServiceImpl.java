package com.xiaomi.hera.trace.etl.consumer;

import com.xiaomi.hera.trace.etl.api.MetricsService;
import org.springframework.stereotype.Service;

@Service("httpMetricsService")
public class HttpMetricsServiceImpl implements MetricsService {

    @Override
    public String getMetricsNamePrefix() {
        return "hera_";
    }

    @Override
    public String[] getMetricsLabelKeys() {
        return new String[0];
    }

    @Override
    public String[] getMetricsLabelValues() {
        return new String[0];
    }
}
