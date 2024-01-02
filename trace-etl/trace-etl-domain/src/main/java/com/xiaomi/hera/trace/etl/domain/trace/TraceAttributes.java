package com.xiaomi.hera.trace.etl.domain.trace;

public class TraceAttributes {

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

}
