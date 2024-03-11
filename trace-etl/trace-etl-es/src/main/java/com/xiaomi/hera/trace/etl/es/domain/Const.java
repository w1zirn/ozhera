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
package com.xiaomi.hera.trace.etl.es.domain;

public class Const {

    public static final String CONSUMER_TYPE = "CONSUMER_TYPE";
    public static final String ROCKS_TYPE = "ROCKS_TYPE";
    public static volatile boolean REDIS_DOUBLE_READ = false;

    public static final String FIRST_ORDER = "first";
    public static final String SECOND_ORDER = "second";

    public static final String FIRST_TIMESTAMP_REDIS_PREFIX = "first_new";
    public static final String SECOND_TIMESTAMP_REDIS_PREFIX = "second_new";
}
