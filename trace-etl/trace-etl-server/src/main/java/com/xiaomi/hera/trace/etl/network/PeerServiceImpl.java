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
package com.xiaomi.hera.trace.etl.network;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.xiaomi.data.push.client.Pair;
import com.xiaomi.hera.trace.etl.domain.metrics.SpanHolder;
import com.xiaomi.hera.trace.etl.domain.trace.TraceAttributes;
import com.xiaomi.hera.trace.etl.domain.util.DbTypeEnum;
import com.xiaomi.hera.trace.etl.domain.util.PeerType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(name = "service.selector.property", havingValue = "outer")
@Slf4j
public class PeerServiceImpl implements PeerService {

    private static final Cache<String, String> GOC_DATA_CACHE =
            CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.MINUTES).build();

    @Override
    public String getPeer(SpanHolder spanHolder) {
        try {
            PeerType peerType = getPeerType(spanHolder);
            if (peerType != null) {
                // with isRemote not provided,set false
                return getPeer(spanHolder, peerType);
            }
        } catch (Exception e) {
            log.error("get peer error : ", e);
        }
        return null;
    }

    private String getPeer(SpanHolder spanHolder, PeerType peerType) {
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
                peer = getDbPeer(spanHolder, peerType);
                break;
            case MQ:
                peer = getMqPeer(spanHolder);
        }
        return peer;
    }

    private String getDbPeer(SpanHolder spanHolder, PeerType peerType) {
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

        return peer;

    }

    private String getDbClusterName(PeerType peerType, String connectionStr) {
        String cachedClusterName = GOC_DATA_CACHE.getIfPresent(connectionStr);
        if (cachedClusterName != null) {
            return cachedClusterName;
        } else {
            return getDbClusterNameFromRemote(peerType, connectionStr);
        }
    }

    private String getDbClusterNameFromRemote(PeerType peerType, String connectionStr) {
        String clusterName = formatDBClusterName(peerType, connectionStr);
        GOC_DATA_CACHE.put(connectionStr, clusterName);
        log.info("get db cluster name, name={}, connectionStr={}", clusterName, connectionStr);
        return clusterName;
    }

    private String formatDBClusterName(PeerType peerType, String clusterName) {

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

    private String getTcpPeer(SpanHolder spanHolder) {
        String name = StringUtils.firstNonBlank(spanHolder.getAttribute(TraceAttributes.NET_PEER_NAME), spanHolder.getAttribute(TraceAttributes.NET_SOCK_PEER_ADDR));
        String ip = StringUtils.firstNonBlank(spanHolder.getAttribute(TraceAttributes.NET_PEER_IP));
        String host = spanHolder.getAttribute(TraceAttributes.HTTP_HOST);
        String portStr = StringUtils.firstNonBlank(spanHolder.getAttribute(TraceAttributes.NET_PEER_PORT), spanHolder.getAttribute(TraceAttributes.NET_SOCK_PEER_PORT));
        int port = 0;
        if (portStr != null) {
            try {
                port =
                        Integer.parseInt(portStr);
            } catch (NumberFormatException ex) {
                log.debug("service {} has wrong port format, port is {}", spanHolder.getService(), portStr);
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

        String messagingSystem = spanHolder.getAttribute(TraceAttributes.MESSAGING_SYSTEM);
        if (StringUtils.isNotEmpty(messagingSystem)) {
            return PeerType.MQ;
        }
        return PeerType.TCP;
    }

    private String getMqPeer(SpanHolder spanHolder) {
        return spanHolder
                .getAttributeMap()
                .getOrDefault(TraceAttributes.MESSAGING_DESTINATION, "unknown");
    }
}
