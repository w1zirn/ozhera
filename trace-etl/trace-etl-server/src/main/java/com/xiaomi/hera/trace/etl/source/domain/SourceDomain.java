package com.xiaomi.hera.trace.etl.source.domain;

import java.util.Map;

public interface SourceDomain {

    String getPrefixIndex();

    String getIndex();

    Map<String, Object> getDataMap();

}
