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
package com.xiaomi.hera.trace.etl.domain;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

@Data
@ToString
public class HeraTraceEtlConfig implements Serializable {

    private Integer id;

    private String bindId;

    private String appName;

    private Integer baseInfoId;

    private Integer platformType;

    private String excludeMethod;

    private String excludeHttpserverMethod;

    private String excludeThread;

    private String excludeSql;

    private String excludeHttpUrl;

    private String excludeUa;

    private Integer httpSlowThreshold;

    private Integer dubboSlowThreshold;

    private Integer mysqlSlowThreshold;

    private Integer traceFilter;

    private Integer traceDurationThreshold;

    private String traceDebugFlag;

    private String httpStatusError;

    private String exceptionError;

    private String grpcCodeError;

    private Date createTime;

    private Date updateTime;

    private String createUser;

    private String updateUser;



}