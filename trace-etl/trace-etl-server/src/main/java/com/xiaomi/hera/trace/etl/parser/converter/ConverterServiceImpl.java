package com.xiaomi.hera.trace.etl.parser.converter;

import com.xiaomi.hera.trace.etl.domain.converter.ClientConverter;
import com.xiaomi.hera.trace.etl.domain.converter.LocalConverter;
import com.xiaomi.hera.trace.etl.domain.converter.ServerConverter;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import org.springframework.stereotype.Service;

@Service
public class ConverterServiceImpl implements ConverterService{
    @Override
    public ClientConverter getClientConverter(SpanHolder spanHolder) {
        return null;
    }

    @Override
    public ServerConverter getServerConverter(SpanHolder spanHolder) {
        return null;
    }

    @Override
    public LocalConverter getLocalConverter(SpanHolder spanHolder) {
        return null;
    }
}
