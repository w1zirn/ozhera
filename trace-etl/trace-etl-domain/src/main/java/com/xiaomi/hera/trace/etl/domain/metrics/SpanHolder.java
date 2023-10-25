package com.xiaomi.hera.trace.etl.domain.metrics;

import com.xiaomi.hera.tspandata.TAttributeKey;
import com.xiaomi.hera.tspandata.TAttributeType;
import com.xiaomi.hera.tspandata.TAttributes;
import com.xiaomi.hera.tspandata.TEvent;
import com.xiaomi.hera.tspandata.TKind;
import com.xiaomi.hera.tspandata.TSpanData;
import com.xiaomi.hera.tspandata.TValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpanHolder {

  private TSpanData span;
  private Map<String, String> attributeMap;
  private Integer appId;
  private String appName;
  private String application;
  private boolean skipAnalysis;

  public SpanHolder(TSpanData span) {
    this.span = span;
    this.attributeMap = new HashMap<>();
    putIntoAttributeMap(this.span.getAttributes());
    if (this.span.getResouce() != null) {
      putIntoAttributeMap(this.span.getResouce().getAttributes());
    }
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
            AttributesHelper.valueToString(keys.get(index), values.get(index)));
      }
    }
  }

  public SpanKind getSpanType() {
    if (!this.getSpan().isSetKind()) {
      this.getSpan().setKind(TKind.INTERNAL);
    }
    TKind spanKind = this.getSpan().getKind();
    switch (spanKind) {
      case CLIENT:
        return SpanKind.
      case PRODUCER:
        return SpanKind;
      case SERVER:
      case CONSUMER:
        return SpanType.Entry;
      default:
        return SpanType.Local;
    }
  }

  public String getTraceId() {
    return this.getSpan().getTraceId();
  }

  public String getSpanId() {
    return this.getSpan().getSpanId();
  }

  public Integer getAppId() {
    return appId;
  }

  public void setAppId(Integer appId) {
    this.appId = appId;
  }

  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public String getApplication() {
    return application;
  }

  public void setApplication(String application) {
    this.application = application;
  }

  public void setSkipAnalysis(boolean skipAnalysis) {
     this.skipAnalysis = skipAnalysis;
  }

  public boolean getSkipAnalysis() {
    return skipAnalysis;
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

  public String getOperationName() {
    return AttributesHelper.getOperationName(this);
  }

  public String getPeer() {
    return PeerHelper.getPeer(this);
  }

  public String getPeer(boolean isRemote) {
    return PeerHelper.getPeer(this, isRemote);
  }

  public SpanLayer getSpanLayer() {
    return AttributesHelper.getSpanLayer(this);
  }

  public RequestypeEnum getRequestType() {
    return AttributesHelper.getRequestType(this);
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
    String httpStatusCode = attributeMap.get(SemanticAttributes.HTTP_STATUS_CODE.getKey());
    if (httpStatusCode != null) {
      try {
        return Integer.parseInt(httpStatusCode);
      } catch (NumberFormatException e) {
        log.warn("span {} has illegal status code {}", span, httpStatusCode);
      }
    }

    return 0;
  }

  public String getDBStatement() {
    return getAttribute(SemanticAttributes.DB_STATEMENT.getKey());
  }

  public String getDBType() {
    return getAttribute(SemanticAttributes.DB_SYSTEM.getKey());
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
        exceptionBuilder.append(AttributesHelper.valueToString(keys.get(index), values.get(index)));
      }
    }
    return exceptionBuilder.toString();
  }

  @Override
  public String toString() {
    return this.getTraceId() + "_" + this.getSpanId();
  }
}
