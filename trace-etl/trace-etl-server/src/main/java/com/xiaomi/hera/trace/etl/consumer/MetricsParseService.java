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
import com.xiaomi.hera.trace.etl.api.service.IMetricsParseService;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.hera.trace.etl.parser.TraceParser;
import com.xiaomi.hera.tspandata.TSpanData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MetricsParseService implements IMetricsParseService {

    @Autowired
    private TraceParser traceParser;

    @Autowired
    private SpanHoldService spanHoldService;

    @Override
    public void parse(TSpanData tSpanData) {
        SpanHolder spanHolder = spanHoldService.getSpanHolder(tSpanData);
        if(spanHolder != null) {
            if(!parseBefore(spanHolder)){
                return;
            }
            traceParser.parse(spanHolder);
        }
    }

    @Override
    public boolean parseBefore(SpanHolder spanHolder) {
        return true;
    }
}
