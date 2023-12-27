package com.xiaomi.hera.trace.etl.source;

import com.google.common.eventbus.Subscribe;
import com.xiaomi.hera.trace.etl.service.WriteEsService;
import com.xiaomi.hera.trace.etl.source.domain.ErrorTraceSourceDomain;
import com.xiaomi.hera.trace.etl.source.observer.Listener;
import com.xiaomi.hera.trace.etl.source.observer.ObserverAdaptor;
import com.xiaomi.hera.trace.etl.source.service.SourceObtainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class ErrorSourceReceive {

    private ObserverAdaptor<ErrorTraceSourceDomain> observerAdaptor;

    @Autowired
    private WriteEsService writeEsService;

    @Autowired
    private SourceObtainService sourceObtainService;

    @PostConstruct
    private void init(){
        observerAdaptor = new ObserverAdaptor<>(Runtime.getRuntime().availableProcessors() * 2 + 1,  30000);
        observerAdaptor.register(new ErrorDomainListener());
    }

    public void submitErrorTraceDomain(ErrorTraceSourceDomain o){
        try {
            observerAdaptor.post(o);
        }catch (Throwable t){
            log.error("submit error trace domain error , ", t);
        }
    }

    class ErrorDomainListener implements Listener<ErrorTraceSourceDomain> {

        @Override
        @Subscribe
        public void listen(ErrorTraceSourceDomain o) {
            try {
                writeEsService.insert(o.getIndex(), o.getDataMap());
            }catch (Throwable t){
                log.error("Error domain trace listen error , ", t);
            }
        }
    }
}
