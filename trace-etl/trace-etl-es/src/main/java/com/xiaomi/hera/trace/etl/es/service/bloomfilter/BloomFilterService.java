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
package com.xiaomi.hera.trace.etl.es.service.bloomfilter;

import com.google.common.hash.BloomFilter;
import com.xiaomi.hera.tspandata.TSpanData;

public interface BloomFilterService {

    boolean isExistLocal(String traceId, String serviceName, String spanName, String type, String order, TSpanData spanData);

    BloomFilter<CharSequence> getLocalBloomFilter();

    void addBatch(String traceId);

    void createBloom();
    void createBloom(String key);

    void deleteBloom();

    void updateBloom();
}
