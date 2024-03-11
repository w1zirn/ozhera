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
package com.xiaomi.hera.trace.etl.source.observer;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ObserverAdaptor<T> {

    private EventBus asyncEventBus;

    public ObserverAdaptor(int processSize, int queueSize) {
        BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(queueSize);
        ThreadPoolExecutor errorESthreadPoolExecutor = new ThreadPoolExecutor(processSize, processSize,
                0L, TimeUnit.MILLISECONDS,
                queue);
        this.asyncEventBus = new AsyncEventBus(errorESthreadPoolExecutor);
    }

    public void register(Listener<T> listener) {
        asyncEventBus.register(listener);
    }

    public void post(T o) {
        asyncEventBus.post(o);
    }
}
