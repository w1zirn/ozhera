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
package com.xiaomi.hera.trace.etl.domain.source;

import lombok.Builder;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@Builder
public class ErrorTraceSourceDomain implements SourceDomain {

    public static final String TRACE_ID = "traceId";
    public static final String DOMAIN = "domain";
    public static final String TYPE = "type";
    public static final String HOST = "host";
    public static final String URL = "url";
    public static final String DATA_SOURCE = "dataSource";
    public static final String SERVICE_NAME = "serviceName";
    public static final String TIMESTAMP = "timestamp";
    public static final String DURATION = "duration";
    public static final String ERROR_TYPE = "errorType";
    public static final String ERROR_CODE = "errorCode";
    public static final String SERVER_ENV = "serverEnv";

    protected String traceId;
    protected String domain;
    protected String type;
    protected String host;
    protected String url;
    protected String dataSource;
    protected String serviceName; // application
    protected String timestamp;
    protected String duration;
    protected String errorCode;
    protected String errorType;
    protected String serverEnv;

    private String prefixIndex;

    @Override
    public String getPrefixIndex() {
        return this.prefixIndex;
    }

    @Override
    public String getIndex() {
        String format = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String replace = format.replace("-", ".");
        return this.getPrefixIndex() + replace;
    }

    @Override
    public Map<String, Object> getDataMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(TRACE_ID, this.getTraceId());
        map.put(DOMAIN, this.getDomain());
        map.put(TYPE, this.getType());
        map.put(HOST, this.getHost());
        map.put(URL, this.getUrl());
        map.put(DATA_SOURCE, this.getDataSource());
        map.put(SERVICE_NAME, this.getServiceName());
        map.put(TIMESTAMP, this.getTimestamp());
        map.put(DURATION, this.getDuration());
        map.put(ERROR_TYPE, this.getErrorType());
        map.put(ERROR_CODE, this.getErrorCode());
        map.put(SERVER_ENV, this.getServerEnv());
        return map;
    }
}
