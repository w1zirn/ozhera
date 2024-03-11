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
import com.google.common.hash.Funnel;
import com.google.common.hash.Funnels;
import com.xiaomi.hera.tspandata.TSpanData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.Charset;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Slf4j
public class LocalBloomFilterService implements BloomFilterService {

    @Value("${local.bloom.excepted.insertions}")
    private long localBloomExceptedInsertions;
    @Value("${local.bloom.accuracy}")
    private double localBloomAccuracy;

    private volatile BloomFilter<CharSequence> localBloomFilter;

    private Funnel<CharSequence> charSequenceFunnel = Funnels.stringFunnel(Charset.defaultCharset());

    private ReentrantLock lock = new ReentrantLock();

    @PostConstruct
    public void init() {
        createBloom();
    }

    public boolean isExistLocal(String traceId, String serviceName, String spanName, String type, String order, TSpanData tSpanData) {
        return isExistLocal(traceId);
    }

    private boolean isExistLocal(String traceId) {
        try {
            return localBloomFilter.mightContain(traceId);
        } catch (Exception e) {
            log.error("judgment traceID: " + traceId + " whether there are failures in the local bloomfilter:", e);
        }
        return true;
    }

    @Override
    public BloomFilter<CharSequence> getLocalBloomFilter() {
        return this.localBloomFilter;
    }

    public void addBatch(String traceId) {
        lock.lock();
        try {
            localBloomFilter.put(traceId);
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void createBloom() {
        this.localBloomFilter = BloomFilter.create(charSequenceFunnel, localBloomExceptedInsertions, localBloomAccuracy);
        log.info("create local bloom filter success");
    }

    @Override
    public void createBloom(String key) {
        createBloom();
    }

    @Override
    public void deleteBloom() {

    }

    @Override
    public void updateBloom() {
        createBloom();
        log.info("update local bloom filter success");
    }

}
