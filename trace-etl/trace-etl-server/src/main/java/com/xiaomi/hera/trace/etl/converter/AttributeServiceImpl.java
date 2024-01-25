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
package com.xiaomi.hera.trace.etl.converter;

import com.xiaomi.hera.trace.etl.api.AttributeService;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.hera.trace.etl.domain.trace.TraceAttributes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.net.URL;

@Service
@Slf4j
public class AttributeServiceImpl implements AttributeService {

    private static final String IPV4_REGEX = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";

    @Override
    public String getHttpClientDestHost(SpanHolder spanHolder) {
        String host = null;
        // first consider http.url as uri
        String tempUri = spanHolder.getAttributeMap().get(TraceAttributes.HTTP_URL);
        if (StringUtils.isNotEmpty(tempUri)) {
            try {
                URL url = new URL(tempUri);
                host = url.getHost();
            } catch (Exception ex) {
                log.warn("getClientHttpHost error,uri={}", tempUri);
            }
        }
        if (StringUtils.isEmpty(host)) {
            host = spanHolder.getAttributeMap().get(TraceAttributes.NET_PEER_NAME);
        }
        if (!isPossibleDomain(host)) {
            host = null;
        }
        return host;
    }

    private static boolean isPossibleDomain(String str) {
        if (StringUtils.isEmpty(str)) {
            return false;
        }
        if (isCorrectIP(str)) {
            return false;
        }
        long dotCount = str.chars().filter(ch -> ch == '.').count();
        if (dotCount < 2) {
            return false;
        }
        return true;
    }

    private static boolean isCorrectIP(String ipStr) {
        if (StringUtils.isEmpty(ipStr)) {
            return false;
        }

        if (ipStr.matches(IPV4_REGEX)) {
            String[] ipArray = ipStr.split("\\.");
            for (int i = 0; i < ipArray.length; i++) {
                int number = Integer.parseInt(ipArray[i]);
                if (number < 0 || number > 255) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
