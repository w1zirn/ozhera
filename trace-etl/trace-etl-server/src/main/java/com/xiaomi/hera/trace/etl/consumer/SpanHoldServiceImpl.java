package com.xiaomi.hera.trace.etl.consumer;

import com.xiaomi.hera.trace.etl.api.SpanHoldService;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.hera.tspandata.TSpanData;
import org.springframework.stereotype.Service;

@Service
public class SpanHoldServiceImpl implements SpanHoldService {

    @Override
    public SpanHolder getSpanHolder(TSpanData data) {
        return new SpanHolder(data);
    }
}
