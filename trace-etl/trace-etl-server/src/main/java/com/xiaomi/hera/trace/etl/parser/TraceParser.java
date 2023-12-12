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
            if(SpanKind.Client.equals(spanHolder.getSpanKind())){
                spanParser.parseClient(spanHolder);
            }else if(SpanKind.Server.equals(spanHolder.getSpanKind())){
                spanParser.parseServer(spanHolder);
            }else if(SpanKind.Local.equals(spanHolder.getSpanKind())){
                spanParser.parseLocal(spanHolder);
            }else{
                log.error("span type value was unexpected, span kind : {}", spanHolder.getSpanKind());
            }
        }catch (Throwable t){
            log.error("trace parse error , ",t);
        }
    }
}
