package com.revolut.mtt.modules;

import java.sql.Connection;

/**
 * Provides already open connection, so different beans could use the same connection within one request.
 */
public class ThreadLocalConnectionProvider implements ConnectionProvider {

    private final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();

    @Override
    public Connection currentConnection() {
        return connectionHolder.get();
    }

    void setCurrentConnection(final Connection connection) {
        connectionHolder.set(connection);
    }
}
