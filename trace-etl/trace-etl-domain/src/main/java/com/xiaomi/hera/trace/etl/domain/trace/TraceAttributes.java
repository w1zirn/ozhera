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
package com.xiaomi.hera.trace.etl.domain.trace;

public class TraceAttributes {

    /**
     * thread
     */
    public static final String THREAD_NAME = "thread.name";
    /**
     * http
     */
    public static final String HTTP_ROUTE = "http.route";
    public static final String HTTP_URL = "http.url";
    public static final String HTTP_TARGET = "http.target";
    public static final String HTTP_USER_AGENT = "http.user_agent";
    public static final String HTTP_METHOD = "http.method";
    public static final String HTTP_HOST = "http.host";
    public static final String HTTP_USER_AGENT_ORIGINAL = "user_agent.original";
    public static final String HTTP_STATUS_CODE = "http.status_code";
    /**
     * db
     */
    public static final String DB_STATEMENT = "db.statement";
    public static final String DB_SYSTEM = "db.system";
    public static final String DB_NAME = "db.name";
    public static final String DB_OPERATION = "db.operation";
    public static final String DB_CONNECTION_STRING = "db.connection_string";
    /**
     * rpc
     */
    public static final String RPC_SYSTEM = "rpc.system";
    public static final String RPC_SERVICE = "rpc.service";
    public static final String RPC_METHOD = "rpc.method";
    public static final String RPC_GRPC_STATUS_CODE = "rpc.grpc.status_code";
    /**
     * net msg
     */
    public static final String NET_HOST_NAME = "net.host.name";
    public static final String NET_SOCK_HOST_ADDR = "net.sock.host.addr";
    public static final String NET_PEER_NAME = "net.peer.name";
    public static final String NET_SOCK_PEER_ADDR = "net.sock.peer.addr";
    public static final String NET_PEER_IP = "net.peer.ip";
    public static final String NET_PEER_PORT = "net.peer.port";
    public static final String NET_SOCK_PEER_PORT = "net.sock.peer.port";
    /**
     * MQ
     */
    public static final String MESSAGING_SYSTEM = "messaging.system";
    public static final String MESSAGING_DESTINATION = "messaging.destination";
    public static final String MESSAGING_DESTINATION_NAME = "messaging.destination.name";
    public static final String MESSAGING_OPERATION = "messaging.operation";

    /**
     * driver
     */
    public static final String DB_DRIVER_DOMAIN_PORT = "db.driver.domainPort";
    public static final String DB_DRIVER_USER_NAME = "db.driver.userName";
    public static final String DB_DRIVER_PASSWORD = "db.driver.password";
    public static final String DB_DRIVER_TYPE = "db.driver.type";
    public static final String DB_DRIVER_DBNAME = "db.driver.dbName";

    /**
     * hera-context
     */
    public static final String HERA_CONTEXT = "span.hera_context";

    /**
     * common
     */
    public static final String ERROR = "error";
    public static final String SERVICE_ENV = "service.env";
    public static final String SERVICE_ENV_ID = "service.env.id";


    /**
     * nginx attribute
     */
    public static final String HTTP_REMOTE_ADDRESS = "http.remote.address";
    public static final String HTTP_REQUEST = "http.request";
    public static final String HTTP_REFERER = "http.referer";
    public static final String HTTP_X_FORWARDED_FOR = "http.x-forwarded-for";
    public static final String HTTP_UPSTREAM_ADDRESS = "http.upstream.address";
    public static final String HTTP_UPSTREAM_STATUS = "http.upstream.status";
}
