package com.xiaomi.hera.trace.etl.parser;

import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanKind;
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
        }catch (Throwable t){
            log.error("trace parse error , ",t);
        }
    }
}
