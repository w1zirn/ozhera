package com.xiaomi.youpin.trace.etl.test;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.xiaomi.hera.trace.etl.util.ExecutorUtil;
import org.junit.Test;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EventBusTest {

    @Test
    public void test(){
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 2,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(30000));
        EventBus eventBus = new AsyncEventBus(threadPoolExecutor);
        eventBus.register(new EventBusListener());
        eventBus.post(1);
        eventBus.post("2");
    }

    @Test
    public void test2(){
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1));
        EventBus eventBus = new AsyncEventBus(threadPoolExecutor);
        eventBus.register(new EventBusListener());
        try{
            while (true){
                eventBus.post("1");
            }
        }catch (Throwable t){
            t.printStackTrace();
        }
    }

    class EventBusListener{

        @Subscribe
        public void listen1(String param){
            System.out.println("listen1 : String : "+param);
        }

        @Subscribe
        public void listen2(Integer param){
            System.out.println("listen2 : int : "+param);
        }
    }
}
