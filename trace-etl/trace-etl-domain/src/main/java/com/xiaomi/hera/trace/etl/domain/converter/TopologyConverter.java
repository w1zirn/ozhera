package com.xiaomi.hera.trace.etl.domain.converter;

import lombok.Data;

@Data
public class TopologyConverter extends MetricsConverter{
    private String destApp;

    private long duration;

    private String metaDataType;

    private String sourceApp;

}
