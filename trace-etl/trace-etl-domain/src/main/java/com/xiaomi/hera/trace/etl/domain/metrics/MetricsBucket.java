package com.xiaomi.hera.trace.etl.domain.metrics;

public class MetricsBucket {

    public static final double[] HTTP_BUCKET = new double[] {50.0D, 100.0D, 150.0D, 200.0D, 250.0D, 300.0D, 400.0D, 500.0D, 700.0D, 1000.0D, 2000.0D, 3000.0D, 5000.0D};
    public static final double[] DUBBO_BUCKET = new double[] {50.0D, 100.0D, 150.0D, 200.0D, 250.0D, 300.0D, 400.0D, 500.0D, 700.0D, 1000.0D, 2000.0D, 3000.0D, 5000.0D};
    public static final double[] REDIS_BUCKET = new double[] {1.0D, 10.0D, 100.0D, 500.0D, 1000.0D};
    public static final double[] SQL_BUCKET = new double[] {10.0D, 50.0D, 100.0D, 500.0D, 1000.0D};
    public static final double[] MQ_BUCKET = new double[] {10.0D, 50.0D, 100.0D, 500.0D, 1000.0D};

}
