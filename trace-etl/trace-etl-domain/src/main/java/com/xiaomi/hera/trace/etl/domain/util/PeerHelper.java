package com.xiaomi.hera.trace.etl.domain.util;

import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.hera.trace.etl.domain.trace.TraceAttributes;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class PeerHelper {

    public static String getPeer(SpanHolder spanHolder) {
        PeerType peerType = getPeerType(spanHolder);
        if (peerType != null) {
            // with isRemote not provided,set false
            return getPeer(spanHolder, peerType, false);
        }
        return null;
    }

    public static String getPeer(SpanHolder spanHolder, boolean isRemote) {
        PeerType peerType = getPeerType(spanHolder);
        if (peerType != null) {
            return getPeer(spanHolder, peerType, isRemote);
        }
        return null;
    }

    public static String getPeer(SpanHolder spanHolder, PeerType peerType, boolean isRemote) {
        String peer = null;
        switch (peerType) {
            case TCP:
                peer = getTcpPeer(spanHolder);
                break;
            case REDIS:
            case MYSQL:
            case ORACLE:
            case HBASE:
            case ELASTICSEARCH:
                peer = getDbPeer(spanHolder, peerType, isRemote);
                break;
            case FALCON_AGENT:
                peer = PeerType.FALCON_AGENT.getValue();
                break;
            case LCS_AGENT:
                peer = PeerType.LCS_AGENT.getValue();
                break;
            case MQ:
                peer = getMqPeer(spanHolder);
        }
        return peer;
    }

    // get db peer from span, see 'https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/trace/semantic_conventions/database.md'
    private static String getDbPeer(SpanHolder spanHolder, PeerType peerType, boolean isRemote) {
        long startTime = System.currentTimeMillis();
        // first consider 'net.peer.name' or 'net.peer.ip'
        String host = spanHolder.getAttribute(TraceAttributes.NET_PEER_NAME);
        String ip = spanHolder.getAttribute(TraceAttributes.NET_PEER_IP);
        String peer = StringUtils.firstNonBlank(host, ip);
        if (StringUtils.isNotEmpty(peer) &&
                spanHolder.getAttribute(TraceAttributes.NET_PEER_PORT) != null) {
            peer += ":" + spanHolder.getAttribute(TraceAttributes.NET_PEER_PORT);
        }

        // then consider 'net.sock.peer.port'
        if (StringUtils.isEmpty(peer)) {
            peer = spanHolder.getAttribute(TraceAttributes.NET_SOCK_PEER_ADDR);
            if (StringUtils.isNotEmpty(peer) &&
                    spanHolder.getAttribute(TraceAttributes.NET_SOCK_PEER_PORT) != null) {
                peer += ":" + spanHolder.getAttribute(TraceAttributes.NET_SOCK_PEER_PORT);
            }
        }

        // then consider 'db.connection_string'
        if (StringUtils.isEmpty(peer)) {
            peer = spanHolder.getAttribute(TraceAttributes.DB_CONNECTION_STRING);
        }

        if (StringUtils.isEmpty(peer)) {
            return "unknown-" + peerType.getValue();
        }

        if (!isRemote) {
            return peer;
        }

        String clusterName = getDbClusterName(peerType, peer);
        PerfCounter.countDuration("get_remote_db_cluster_name_time_cost", System.currentTimeMillis() - startTime);
        return clusterName;
    }

    public static String getDbClusterName(PeerType peerType, String connectionStr) {
        String cachedClusterName = GOC_DATA_CACHE.getIfPresent(connectionStr);
        PerfCounter.count("get_remote_db_cluster_name_total_count", 1);
        if (cachedClusterName != null) {
            PerfCounter.count("get_remote_db_cluster_name_hit_count", 1);
            return cachedClusterName;
        } else {
            return getDbClusterNameFromRemote(peerType, connectionStr);
        }
    }

    public static boolean isCorrectIP(String ipStr) {
        if (StringUtils.isEmpty(ipStr)) {
            return false;
        }

        if (ipStr.matches(IP_REGEX)) {
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

    // may be domain
    public static boolean isPossibleDomain(String str) {
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

    private static String getDbClusterNameFromRemote(PeerType peerType, String connectionStr) {
        String clusterName = formatDBClusterName(peerType, connectionStr);
        GOC_DATA_CACHE.put(connectionStr, clusterName);
        LOG.info("get db cluster name, name={}, connectionStr={}", clusterName, connectionStr);
        return clusterName;
    }

    public static String formatDBClusterName(PeerType peerType, String clusterName) {

        switch (peerType) {
            case HBASE:
                if (!clusterName.startsWith(peerType.getSchema())) {
                    final String pattern = "hadoop";
                    if (clusterName.contains(pattern)) {
                        return DbTypeEnum.hbase.getSchema()
                                + clusterName.substring(
                                0, clusterName.indexOf(pattern) + pattern.length());
                    }
                    break;
                }
            case MYSQL:
            case ORACLE:
            case REDIS:
                if (!clusterName.startsWith(peerType.getSchema())) {
                    return peerType.getSchema() + clusterName;
                }
                break;
            default:
                break;
        }

        return clusterName;
    }

    private static PeerType getPeerType(SpanHolder spanHolder) {
        String dbSystem = spanHolder.getAttribute(TraceAttributes.DB_SYSTEM);
        if (dbSystem != null) {
            if (dbSystem.equals(PeerType.REDIS.getValue())) {
                return PeerType.REDIS;
            }
            if (dbSystem.equals(PeerType.MYSQL.getValue())) {
                return PeerType.MYSQL;
            }
            if (dbSystem.equals(PeerType.ORACLE.getValue())) {
                return PeerType.ORACLE;
            }
            if (dbSystem.equals(PeerType.HBASE.getValue())) {
                return PeerType.HBASE;
            }
            if (dbSystem.equals(PeerType.ELASTICSEARCH.getValue())) {
                return PeerType.ELASTICSEARCH;
            }
        }

        String portStr = spanHolder.getAttribute(TraceAttributes.NET_PEER_PORT);
        if (StringUtils.isNotEmpty(portStr)) {
            int port = 0;
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException ex) {
                log.debug("number format error, service is {}, port is {}", spanHolder.getService(), portStr);
            }

            if (FALCON_AGENT_PORTS.contains(port)) {
                return PeerType.FALCON_AGENT;
            } else if (LCS_AGENT_PORTS.contains(port)) {
                return PeerType.LCS_AGENT;
            }
        }

        String messagingSystem = spanHolder.getAttribute(TraceAttributes.MESSAGING_SYSTEM);
        if (StringUtils.isNotEmpty(messagingSystem)) {
            return PeerType.MQ;
        }
        return PeerType.TCP;
    }

    public static String getTcpPeer(SpanHolder spanHolder) {
        String name = spanHolder.getAttribute(TraceAttributes.NET_PEER_NAME);
        String ip = spanHolder.getAttribute(TraceAttributes.NET_PEER_IP);
        String host = spanHolder.getAttribute(TraceAttributes.HTTP_HOST);
        int port = 0;
        if (spanHolder.hasAttribute(TraceAttributes.NET_PEER_PORT)) {
            try {
                port =
                        Integer.parseInt(
                                spanHolder.getAttribute(TraceAttributes.NET_PEER_PORT));
            } catch (NumberFormatException ex) {
                log.debug("service {} has wrong port format, port is {}", spanHolder.getService(), spanHolder.getAttribute(TraceAttributes.NET_PEER_PORT));
            }
        }

        String peer = StringUtils.firstNonBlank(name, ip, host);
        if (peer == null) {
            return null;
        }
        if (port == 0 || port == 80 || port == 443) {
            return peer;
        }
        return peer + ":" + port;
    }

    public static String getMqPeer(SpanHolder spanHolder) {
        return spanHolder
                .getAttributeMap()
                .getOrDefault(TraceAttributes.MESSAGING_DESTINATION, "unknown");
    }

    public static boolean containsIp(String ipString) {
        Pattern pattern = Pattern.compile("((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)");
        Matcher m = pattern.matcher(ipString);
        return m.find();
    }
}
