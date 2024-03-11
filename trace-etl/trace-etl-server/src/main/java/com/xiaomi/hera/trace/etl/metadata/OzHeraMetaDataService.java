/*
 * Copyright 2020 Xiaomi
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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

    HeraMetaDataModel getHeraMetaData(String peerIpPort);

    HeraMetaDataModel getHeraMetaData(SpanHolder spanHolder);

    String getMetricsMetaDataName(String peerIpPort);

    /**
     * Obtaining HeraMetaData through Dubbo metadata information is solely for
     * the convenience of retrieving the counterpart's IP, port, and other data from the Dubbo registry.
     * @param dubboMeta
     * @return
     */
    HeraMetaDataModel getMetaDataByDubbo(String dubboMeta);

    void insert(HeraMetaDataModelDTO model);
}
