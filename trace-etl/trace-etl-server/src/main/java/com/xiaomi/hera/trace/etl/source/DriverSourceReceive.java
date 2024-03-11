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
package com.xiaomi.hera.trace.etl.source;

import com.google.common.eventbus.Subscribe;
import com.xiaomi.hera.trace.etl.api.service.DataSourceService;
import com.xiaomi.hera.trace.etl.domain.source.DriverSourceDomain;
import com.xiaomi.hera.trace.etl.domain.source.ErrorTraceSourceDomain;
import com.xiaomi.hera.trace.etl.source.observer.Listener;
import com.xiaomi.hera.trace.etl.source.observer.ObserverAdaptor;
import com.xiaomi.hera.trace.etl.source.service.SourceObtainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class DriverSourceReceive {

    private ObserverAdaptor<DriverSourceDomain> observerAdaptor;

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private SourceObtainService sourceObtainService;

    @PostConstruct
    private void init(){
        observerAdaptor = new ObserverAdaptor<>(Runtime.getRuntime().availableProcessors() * 2 + 1,  30000);
        observerAdaptor.register(new DriverDomainListener());
    }

    public void submitDriverDomain(DriverSourceDomain o){
        try {
            observerAdaptor.post(o);
        }catch (Throwable t){
            log.error("submit error trace domain error , ", t);
        }
    }

    class DriverDomainListener implements Listener<DriverSourceDomain> {

        @Override
        @Subscribe
        public void listen(DriverSourceDomain o) {
            try {
                dataSourceService.insertDriver(o);
            }catch (Throwable t){
                log.error("Error domain trace listen error , ", t);
            }
        }
    }
}
