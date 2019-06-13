package com.revolut.mtt.database;

import java.sql.Connection;

/**
 * Provides already open connection, so different classes could use the same connection within one request/thread.
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
