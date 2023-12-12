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