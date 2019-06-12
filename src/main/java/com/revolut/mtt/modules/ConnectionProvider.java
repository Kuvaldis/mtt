package com.revolut.mtt.modules;

import java.sql.Connection;
import java.sql.SQLException;

public interface ConnectionProvider {
    Connection currentConnection() throws SQLException;
}
