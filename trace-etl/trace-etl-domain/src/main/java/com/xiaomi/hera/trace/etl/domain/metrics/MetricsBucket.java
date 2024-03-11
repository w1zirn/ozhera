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
package com.xiaomi.hera.trace.etl.domain.metrics;

public class MetricsBucket {

    public static final double[] HTTP_BUCKET = new double[] {50.0D, 100.0D, 150.0D, 200.0D, 250.0D, 300.0D, 400.0D, 500.0D, 700.0D, 1000.0D, 2000.0D, 3000.0D, 5000.0D};
    public static final double[] DUBBO_BUCKET = new double[] {50.0D, 100.0D, 150.0D, 200.0D, 250.0D, 300.0D, 400.0D, 500.0D, 700.0D, 1000.0D, 2000.0D, 3000.0D, 5000.0D};
    public static final double[] REDIS_BUCKET = new double[] {1.0D, 10.0D, 100.0D, 500.0D, 1000.0D};
    public static final double[] SQL_BUCKET = new double[] {10.0D, 50.0D, 100.0D, 500.0D, 1000.0D};
    public static final double[] MQ_BUCKET = new double[] {10.0D, 50.0D, 100.0D, 500.0D, 1000.0D};

}
