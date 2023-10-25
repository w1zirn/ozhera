package com.xiaomi.hera.trace.etl.api;

public interface MetricsService {

    String getMetricsNamePrefix();

    String[] getMetricsLabelKeys();

    String[] getMetricsLabelValues();
}
