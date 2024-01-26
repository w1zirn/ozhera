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
package com.xiaomi.hera.trace.etl.source.service;

import com.xiaomi.hera.trace.etl.domain.converter.MetricsConverter;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.hera.trace.etl.domain.source.DriverSourceDomain;
import com.xiaomi.hera.trace.etl.domain.source.ErrorTraceSourceDomain;

public interface SourceObtainService {

    ErrorTraceSourceDomain getErrorTraceSourceDomain(MetricsConverter metricsConverter);

    ErrorTraceSourceDomain getSlowTraceSourceDomain(MetricsConverter metricsConverter);

    DriverSourceDomain getDriverSourceDomain(SpanHolder spanHolder);
}
