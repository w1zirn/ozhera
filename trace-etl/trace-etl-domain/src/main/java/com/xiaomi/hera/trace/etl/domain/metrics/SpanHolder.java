package com.xiaomi.hera.trace.etl.domain.metrics;

import com.xiaomi.hera.trace.etl.domain.trace.InstrumentationInfo;
import com.xiaomi.hera.trace.etl.domain.trace.TraceAttributes;
import com.xiaomi.hera.tspandata.TAttributeKey;
import com.xiaomi.hera.tspandata.TAttributeType;
import com.xiaomi.hera.tspandata.TAttributes;
import com.xiaomi.hera.tspandata.TEvent;
import com.xiaomi.hera.tspandata.TKind;
import com.xiaomi.hera.tspandata.TSpanData;
import com.xiaomi.hera.tspandata.TStatus;
import com.xiaomi.hera.tspandata.TValue;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class SpanHolder {

    private TSpanData span;
    private Map<String, String> attributeMap;
    private String appName;
    private String application;
    private boolean skip;
    private SpanType spanType;
    private String peerServiceName;

    public SpanHolder(TSpanData span) {
        this.span = span;
        this.attributeMap = new HashMap<>();
        putIntoAttributeMap(this.span.getAttributes());
        if (this.span.getResouce() != null) {
            putIntoAttributeMap(this.span.getResouce().getAttributes());
        }
        setSpanType();
        this.peerServiceName = Peer
    }

    private void setSpanType() {
        if (span.getAttributes() == null) {
            this.spanType = SpanType.unknown;
            return;
        }
        SpanType requestType;
        String rpcSystem = attributeMap.get(TraceAttributes.RPC_SYSTEM);
        if (rpcSystem != null) {
            // rpc
            try {
                // for dubbo, in 1.13.1 otel javaagent rpc.system=apache_dubbo while in older version rpc.system=dubbo
                if (InstrumentationInfo.DUBBO_APACHE.equalsIgnoreCase(rpcSystem)) {
                    requestType = SpanType.dubbo;
                } else {
                    requestType = SpanType.valueOf(rpcSystem.toLowerCase());
                }
            } catch (IllegalArgumentException ex) {
                requestType = SpanType.rpc;
            }
            this.spanType = requestType;
            return;
        }
        String dbSystem = attributeMap.get(TraceAttributes.DB_SYSTEM);
        if (dbSystem != null) {
            // database
            try {
                requestType = SpanType.valueOf(dbSystem.toLowerCase());
            } catch (IllegalArgumentException ex) {
                requestType = SpanType.database;
            }
            this.spanType = requestType;
            return;
        }
        if (attributeMap.get(TraceAttributes.HTTP_URL) != null
                || attributeMap.get(TraceAttributes.HTTP_METHOD) != null) {
            this.spanType = SpanType.http;
            return;
        }
        String mq = attributeMap.get(TraceAttributes.MESSAGE_SYSTEM);
        if(mq != null){
            try {
                requestType = SpanType.valueOf(mq.toLowerCase());
            } catch (IllegalArgumentException ex) {
                requestType = SpanType.unknown;
            }
            this.spanType = requestType;
            return;
        }
        if (TKind.INTERNAL.equals(span.getKind())) {
            this.spanType = SpanType.logic;
            return;
        }
        this.spanType = SpanType.unknown;
    }

    public TSpanData getSpan() {
        return span;
    }

    public Map<String, String> getAttributeMap() {
        return attributeMap;
    }

    public boolean hasAttribute(String attributeKey) {
        return this.attributeMap.containsKey(attributeKey);
    }

    public String getAttribute(String attributeKey) {
        return this.attributeMap.get(attributeKey);
    }

    public String getAttributeOrEmpty(String attributeKey) {
        return getAttributeOrDefault(attributeKey, "");
    }

    public String getAttributeOrDefault(String attributeKey, String defaultValue) {
        String value = this.attributeMap.get(attributeKey);
        if (value != null) {
            value = value.trim();
        } else {
            value = defaultValue;
        }
        return value;
    }

    public void addAttribute(String key, String value) {
        if (key != null && value != null && !this.attributeMap.containsKey(key) && span.getAttributes() != null) {
            this.attributeMap.put(key, value);
            span.getAttributes()
                    .getKeys()
                    .add(new TAttributeKey().setType(TAttributeType.STRING).setValue(key));
            span.getAttributes().getValues().add(new TValue().setStringValue(value));
        }
    }

    /**
     * put attributes to attributeMap
     *
     * @param attributes attributes
     * @return
     */
    private void putIntoAttributeMap(TAttributes attributes) {
        if (attributes != null) {
            List<TAttributeKey> keys = attributes.getKeys();
            List<TValue> values = attributes.getValues();
            for (int index = 0; index < keys.size(); index++) {
                this.attributeMap.put(
                        keys.get(index).getValue(),
                        ThriftUtil.valueToString(keys.get(index), values.get(index)));
            }
        }
    }

    public SpanKind getSpanKind() {
        if (!this.getSpan().isSetKind()) {
            this.getSpan().setKind(TKind.INTERNAL);
        }
        TKind spanKind = this.getSpan().getKind();
        switch (spanKind) {
            case CLIENT:
            case PRODUCER:
                return SpanKind.Client;
            case CONSUMER:
            case SERVER:
                return SpanKind.Server;
            default:
                return SpanKind.Local;
        }
    }

    public SpanType getSpanType() {
        return spanType;
    }

    public String getTraceId() {
        return this.getSpan().getTraceId();
    }

    public String getSpanId() {
        return this.getSpan().getSpanId();
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    public boolean getSkip() {
        return skip;
    }

    public String getService() {
        if (!getSpan().isSetExtra() || !getSpan().getExtra().isSetServiceName()) {
            return "";
        }
        return this.getSpan().getExtra().getServiceName();
    }

    public String getServiceInstance() {
        if (!getSpan().isSetExtra() || !getSpan().getExtra().isSetIp()) {
            return "";
        }
        return this.getSpan().getExtra().getIp();
    }

    public String getServiceInstanceHost() {
        if (!getSpan().isSetExtra() || !getSpan().getExtra().isSetHostname()) {
            return "";
        }
        return this.getSpan().getExtra().getHostname();
    }

    public String getServerEnv() {
        return getAttributeOrEmpty("service.env");
    }

    public String getName() {
        return this.getSpan().getName();
    }


    public long getStartTime() {
        return this.getSpan().getStartEpochNanos() / 1000000;
    }

    public long getEndTime() {
        return this.getSpan().getEndEpochNanos() / 1000000;
    }

    public boolean getIsError() {
        return this.getSpan().getStatus() == TStatus.ERROR;
    }

    public int getStatusCode() {
        String httpStatusCode = attributeMap.get(TraceAttributes.HTTP_STATUS_CODE);
        if (httpStatusCode != null) {
            try {
                return Integer.parseInt(httpStatusCode);
            } catch (NumberFormatException e) {
                log.warn("span {} has illegal status code {}", span, httpStatusCode);
            }
        }

        return 0;
    }

    public String getExceptions() {
        if (!getIsError()) {
            return null;
        }
        StringBuilder exceptionBuilder = new StringBuilder();
        if (!span.isSetEvents()) {
            return null;
        }
        for (TEvent event : span.getEvents()) {
            exceptionBuilder.append(event.getName());
            if (!event.isSetAttributes()) {
                continue;
            }
            List<TAttributeKey> keys = event.getAttributes().getKeys();
            List<TValue> values = event.getAttributes().getValues();
            for (int index = 0; index < keys.size(); index++) {
                exceptionBuilder.append(keys.get(index).getValue());
                exceptionBuilder.append(ThriftUtil.valueToString(keys.get(index), values.get(index)));
            }
        }
        return exceptionBuilder.toString();
    }

    @Override
    public String toString() {
        return this.getTraceId() + "_" + this.getSpanId();
    }

    public String getOperationName() {
        return span.getName();
    }
}
