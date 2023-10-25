package com.xiaomi.hera.trace.etl.api;

import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.hera.tspandata.TSpanData;

public interface SpanHoldService {

    SpanHolder getSpanHolder(TSpanData data);
}
