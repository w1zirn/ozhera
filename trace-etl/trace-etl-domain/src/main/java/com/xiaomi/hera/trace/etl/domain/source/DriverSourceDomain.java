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
public class DriverSourceDomain implements SourceDomain {

    public static final String APP_NAME = "appName";
    public static final String USER_NAME = "userName";
    public static final String PASSWORD = "password";
    public static final String DOMAIN_PORT = "domainPort";
    public static final String DATA_BASE_NAME = "dataBaseName";
    public static final String TYPE = "type";
    public static final String TIMESTAMP = "timeStamp";

    private String appName;
    private String userName;
    private String password;
    private String domainPort;
    private String dataBaseName;
    private String type;
    private String timeStamp;

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
        Map<String, Object> result = new HashMap<>();
        result.put(APP_NAME, appName);
        result.put(USER_NAME, userName);
        result.put(PASSWORD, password);
        result.put(DOMAIN_PORT, domainPort);
        result.put(DATA_BASE_NAME, dataBaseName);
        result.put(TYPE, type);
        result.put(TIMESTAMP, timeStamp);
        return result;
    }
}
