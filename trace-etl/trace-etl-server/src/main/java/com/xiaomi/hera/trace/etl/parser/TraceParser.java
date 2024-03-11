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
package com.xiaomi.hera.trace.etl.parser;

import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TraceParser {

    @Autowired
    private SpanParser spanParser;

    public void parse(SpanHolder spanHolder){
        try{
            spanParser.parseBefore(spanHolder);
            if(spanHolder.getSkip()){
                return;
            }
            switch (spanHolder.getSpanKind()){
                case Client:
                    spanParser.parseClient(spanHolder);
                    spanParser.parseTopology(spanHolder);
                    break;
                case Server:
                    spanParser.parseServer(spanHolder);
                    break;
                case Local:
                    spanParser.parseLocal(spanHolder);
                    break;
                default:
                    log.error("span type value was unexpected, span kind : {}", spanHolder.getSpanKind());
            }
            spanParser.parseAfter(spanHolder);
        }catch (Throwable t){
            log.error("trace parse error , ",t);
        }
    }
}
