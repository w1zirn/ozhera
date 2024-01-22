package com.xiaomi.hera.trace.etl.metadata;

import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.mone.app.api.model.HeraMetaDataModel;

/**
 * Convert the peer IP and port into a real, recognizable, user-defined name.
 * For example, the IP of the application 'test' is 10.0.0.0, and the exposed port is 8080.
 * Through this class, the input is 10.0.0.0:8080, and the returned application is 'test'.
 */
public interface OzHeraMetaDataService {

    void syncHeraMetaData();

    HeraMetaDataModel getHeraMetaData(String serviceName);

    HeraMetaDataModel getHeraMetaData(SpanHolder spanHolder);
}
