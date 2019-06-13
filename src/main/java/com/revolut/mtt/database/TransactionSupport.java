package com.revolut.mtt.database;

import com.google.inject.Binder;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.jooby.Env;
import org.jooby.Jooby;
import org.jooby.Router;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Adds convenient transactions support to routes.
 */
@Slf4j
public class TransactionSupport implements Jooby.Module {

    @Override
    public void configure(final Env env, final Config conf, final Binder binder) throws Throwable {
        log.info("Configure Transactional Module");

        final ThreadLocalConnectionProvider connectionProvider = new ThreadLocalConnectionProvider();
        binder.bind(ConnectionProvider.class).toInstance(connectionProvider);

        final Router router = env.router();

        router.before("*", (req, rsp) -> {
            log.debug("Init new transaction");
            final DataSource dataSource = req.require(DataSource.class);
            final Connection connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            connectionProvider.setCurrentConnection(connection);
            log.debug("Transaction initialized");
        });

        router.complete("*", (req, rsp, cause) -> {
            log.debug("Complete transaction");
            // unbind connection from request
            try(Connection connection = connectionProvider.currentConnection()) {
                if (connection != null) {
                    connectionProvider.setCurrentConnection(connection);
                    if (cause.isPresent()) {
                        connection.rollback();
                        log.debug("Transaction rolled back");
                    } else {
                        connection.commit();
                        log.debug("Transaction committed");
                    }
                }
            } catch (SQLException e) {
                log.error("Exception during connection handling", e);
            }
        });
    }
}
