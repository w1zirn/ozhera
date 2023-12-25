package com.xiaomi.hera.trace.etl.metrics;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class MetricsConverter {

    protected String[] tagKeys(String... customKeys) {
        String[] finalKeys = new String[customKeys.length + getCommonTagKeys().size()];
        for (int index = 0; index < getCommonTagKeys().size(); index++) {
            finalKeys[index] = getCommonTagKeys().get(index);
        }
        for (int index = getCommonTagKeys().size(); index < finalKeys.length; index++) {
            finalKeys[index] = customKeys[index - getCommonTagKeys().size()];
        }
        return finalKeys;
    }

    protected String[] tagValues(String... customValues) {
        List<String> commonTagValues = commonTagValues();
        String[] finalValues = new String[customValues.length + commonTagValues.size()];
        for (int index = 0; index < commonTagValues.size(); index++) {
            finalValues[index] = commonTagValues.get(index);
        }
        for (int index = commonTagValues.size(); index < finalValues.length; index++) {
            finalValues[index] = customValues[index - commonTagValues.size()];
        }
        return finalValues;
    }

    protected List<String> getCommonTagKeys() {
        return null;
    }

    protected List<String> commonTagValues() {
        return null;
    }

    public String formatMetricName(String type, String name) {
        return getMetricsPrefix() + type + name;
    }

    protected String getMetricsPrefix() {
        return "hera";
    }
}
