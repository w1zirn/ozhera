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
