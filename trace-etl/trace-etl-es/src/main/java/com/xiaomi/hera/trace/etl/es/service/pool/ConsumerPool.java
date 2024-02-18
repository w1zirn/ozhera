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
package com.xiaomi.hera.trace.etl.es.service.pool;

import com.xiaomi.hera.trace.etl.es.domain.FutureRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ConsumerPool {
    public static final ThreadPoolExecutor CONSUMER_POOL;
    public static final BlockingQueue CONSUMER_QUEUE = new ArrayBlockingQueue(30000);
    private static AtomicInteger threadNumber = new AtomicInteger(1);
    public static final int CONSUMER_QUEUE_THRESHOLD = 3000;


    public static final Map<Integer,BlockingQueue<FutureRequest>> CONSUMER_BATCH_REDIS_QUEUE = new HashMap<>();
    public static final Map<Integer,BlockingQueue<FutureRequest>> ROCKS_BATCH_REDIS_QUEUE = new HashMap<>();
    public static final Map<Integer, BlockingQueue<String>> BATCH_REDIS_ADD_QUEUE = new HashMap<>();
    public static final int CONSUMER_BATCH_REDIS_KEY_SIZE = 10;
    private static final int processorSize = Runtime.getRuntime().availableProcessors() > 0 ? Runtime.getRuntime().availableProcessors() : 1;
    public static final int REDIS_EXIST_BATCH = 20;
    public static final int REDIS_ADD_BATCH = 20;
    /**
     * redis bloom filter 模式下，当队列中可用容量小于此阈值时，将阻塞主线程
     */
    public static final int REDIS_BATCH_CONSUMER_QUEUE_THRESHOLD = 20;

    static {
        log.info("current CPU num : " + processorSize);

        CONSUMER_POOL = new ThreadPoolExecutor(10,10,1, TimeUnit.MINUTES,CONSUMER_QUEUE, r -> {
            Thread thread = new Thread(r);
            thread.setDaemon(false);
            thread.setName("consumer-" + threadNumber.getAndIncrement());
            return thread;
        });

        for(int i = 0; i <CONSUMER_BATCH_REDIS_KEY_SIZE; i++){
            CONSUMER_BATCH_REDIS_QUEUE.put(i,new ArrayBlockingQueue<>(50));
        }
        for(int i = 0; i <CONSUMER_BATCH_REDIS_KEY_SIZE; i++){
            ROCKS_BATCH_REDIS_QUEUE.put(i,new ArrayBlockingQueue<>(50));
        }
        for(int i = 0; i <CONSUMER_BATCH_REDIS_KEY_SIZE; i++){
            BATCH_REDIS_ADD_QUEUE.put(i, new ArrayBlockingQueue<>(50));
        }
    }

}
