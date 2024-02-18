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
package com.xiaomi.hera.trace.etl.es.scheduler;

import com.xiaomi.hera.trace.etl.es.domain.Const;
import com.xiaomi.hera.trace.etl.es.service.bloomfilter.BloomFilterService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "bloom.filter.type", havingValue = Const.BLOOM_FILTER_TYPE_LOCAL)
public class LocalBloomFilterScheduler {

    @Autowired
    private BloomFilterService bloomFilterService;

    @Scheduled(cron = "0 0 4,12 * * ?")
    private void createLocalBloomTimer(){
        bloomFilterService.createBloom();
    }
}
