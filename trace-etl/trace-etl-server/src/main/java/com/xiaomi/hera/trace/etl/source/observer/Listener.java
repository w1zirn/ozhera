package com.xiaomi.hera.trace.etl.source.observer;

public interface Listener<T> {
    void listen(T t);
}
