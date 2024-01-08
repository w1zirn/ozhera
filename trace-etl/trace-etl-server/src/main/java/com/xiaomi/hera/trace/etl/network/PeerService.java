package com.xiaomi.hera.trace.etl.network;

import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;

/**
 * Extract the content of Span attributes and convert it to ip:port.
 */
public interface PeerService {

    String getPeer(SpanHolder spanHolder);
}
