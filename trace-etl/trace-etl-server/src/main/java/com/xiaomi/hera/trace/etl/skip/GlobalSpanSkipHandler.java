package com.xiaomi.hera.trace.etl.skip;

import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;

public interface GlobalSpanSkipHandler {

    boolean spanSkip(SpanHolder spanHolder);
}
