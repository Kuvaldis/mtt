package com.revolut.mtt.database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Interface for fetching connection to database.
 */
public interface ConnectionProvider {

    Connection currentConnection() throws SQLException;
}
