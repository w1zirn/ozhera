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
        ClientConverter clientConverter = new ClientConverter();
        clientConverter.setOperationName(spanHolder.getOperationName());
        clientConverter.setApplication(spanHolder.getApplication());
        clientConverter.setError(spanHolder.getIsError());
        clientConverter.setDuration(spanHolder.getEndTime() - spanHolder.getStartTime());
        clientConverter.setEndTime(spanHolder.getEndTime());
        clientConverter.setDataSource();
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
