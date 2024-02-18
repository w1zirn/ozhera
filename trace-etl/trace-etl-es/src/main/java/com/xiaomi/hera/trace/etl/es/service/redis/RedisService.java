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
package com.xiaomi.hera.trace.etl.es.service.redis;

import java.util.List;
import java.util.Map;

public interface RedisService {

    Boolean sismember(String key, String member);

    Long sadd(String key, String... members);

    Long setNx(String key, String value);

    String get(String key);

    String set(String key, String value);

    String set(String key, String value, long ttl);

    Boolean exists(String key);

    Long del(String key);

    List<Boolean> bfMAdd(String key, String... items);

    Boolean bfExist(String key, String item);

    List<Boolean> bfMExist(String key, String... item);

    String bfReserve(String key, double errorRate, long capacity, boolean scaling);

    List<Boolean> bfInsert(String key, String... items);

    Map<String, Object> bfInfo(String key);

    boolean getDisLock(String key);

    String getWithException(String key) throws Exception;
}
