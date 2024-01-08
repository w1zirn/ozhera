package com.xiaomi.hera.trace.etl.network;

import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class PeerServiceImpl implements PeerService{
    @Override
    public String getPeer(SpanHolder spanHolder) {
        return null;
    }
}
