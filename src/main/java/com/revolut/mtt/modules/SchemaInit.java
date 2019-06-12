package com.revolut.mtt.modules;

import com.google.inject.Binder;
import com.typesafe.config.Config;
import org.h2.tools.RunScript;
import org.jooby.Env;
import org.jooby.Jooby;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;

public class SchemaInit implements Jooby.Module {

    @Override
    public void configure(final Env env, final Config conf, final Binder binder) throws Throwable {
        env.onStart(registry -> {
            // init schema
            final DataSource dataSource = registry.require(DataSource.class);
            final Connection connection = dataSource.getConnection();
            initSchema(connection);
        });
    }

    public static void initSchema(final Connection connection) throws SQLException {
        final InputStream inputStream = SchemaInit.class
                .getClassLoader()
                .getResourceAsStream("db/create_schema.sql");
        if (inputStream == null) {
            throw new RuntimeException("Cannot create schema");
        }
        final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        RunScript.execute(connection, reader);
    }
}
