package com.xiaomi.hera.trace.etl.api;

import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;

public interface MetricsRecordService {

    void record(SpanHolder span);
}
