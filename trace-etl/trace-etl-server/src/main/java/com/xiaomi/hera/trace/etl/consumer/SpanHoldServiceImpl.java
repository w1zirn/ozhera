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
package com.xiaomi.hera.trace.etl.consumer;

import com.xiaomi.hera.trace.etl.api.SpanHoldService;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.hera.tspandata.TSpanData;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
public class SpanHoldServiceImpl implements SpanHoldService {

    @Override
    public SpanHolder getSpanHolder(TSpanData data) {
        SpanHolder spanHolder = new SpanHolder(data);
        String service = spanHolder.getService();
        if(service == null){
            return null;
        }
        spanHolder.setApplication(service.replace("-", "_"));
        return spanHolder;
    }
}
