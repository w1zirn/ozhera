package com.xiaomi.hera.trace.etl.api;

import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import org.apache.commons.lang3.tuple.Pair;

/**
 * This class handles the conversion of information through Span attributes into various forms.
 */
public interface AttributeService {

    Pair<String, String> getHttpClientDestHostAndOperation(SpanHolder spanHolder);

}
