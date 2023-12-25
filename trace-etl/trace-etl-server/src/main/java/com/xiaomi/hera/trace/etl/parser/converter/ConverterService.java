package com.xiaomi.hera.trace.etl.parser.converter;

import com.xiaomi.hera.trace.etl.domain.converter.ClientConverter;
import com.xiaomi.hera.trace.etl.domain.converter.LocalConverter;
import com.xiaomi.hera.trace.etl.domain.converter.ServerConverter;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;

public interface ConverterService {

    ClientConverter getClientConverter(SpanHolder spanHolder);
    ServerConverter getServerConverter(SpanHolder spanHolder);
    LocalConverter getLocalConverter(SpanHolder spanHolder);
}
