package com.xiaomi.hera.trace.etl.converter;

import com.xiaomi.hera.trace.etl.api.AttributeService;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.hera.trace.etl.domain.trace.TraceAttributes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AttributeServiceImpl implements AttributeService {

    private static final Pattern URL_UUID_PATTERN = Pattern.compile("(/[a-f0-9]{32})");
    private static final Pattern URL_NUMBER_PATTERN = Pattern.compile("(/[0-9]+)");
    private static final Pattern IP_AND_PORT_PATTERN = Pattern.compile("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}:\\d+$");
    private static final String IPV4_REGEX = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";

    @Override
    public Pair<String, String> getHttpClientDestHostAndOperation(SpanHolder spanHolder) {
        String host = null, uri = null;
        // first consider http.url as uri
        String tempUri = spanHolder.getAttributeMap().get(TraceAttributes.HTTP_URL);
        if (StringUtils.isNotEmpty(tempUri)) {
            try {
                URL url = new URL(tempUri);
                uri = url.getPath();
                host = url.getHost();
            } catch (Exception ex) {
                log.warn("getClientHttpHostAndOperation error,uri={}", uri);
            }
        }
        // second consider span name as uri
        if (StringUtils.isEmpty(uri)) {
            uri = formatUri(spanHolder.getSpan().getName());
        }
        if (StringUtils.isEmpty(host)) {
            host = spanHolder.getAttributeMap().get(TraceAttributes.NET_PEER_NAME);
        }
        if (!isPossibleDomain(host)) {
            host = null;
        }
        return Pair.of(host, uri);
    }

    private static String formatUri(String uri) {
        if (StringUtils.isNotEmpty(uri)) {
            if (uri.contains("?")) {
                uri = uri.substring(0, uri.indexOf("?"));
            }
            try {
                List<String> matches = new ArrayList<>();
                Matcher uuidMatcher = URL_UUID_PATTERN.matcher(uri);
                while (uuidMatcher.find()) {
                    matches.add(uuidMatcher.group(1));
                }
                Matcher numberMatcher = URL_NUMBER_PATTERN.matcher(uri);
                while (numberMatcher.find()) {
                    matches.add(numberMatcher.group(1));
                }
                for (String match : matches) {
                    uri = uri.replace(match, "/{id}");
                }
            } catch (Exception ex) {
                log.warn("Failed to format uri={}", uri);
            }
        }
        return uri;
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
