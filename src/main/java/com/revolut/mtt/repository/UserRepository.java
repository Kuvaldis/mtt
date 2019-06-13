package com.revolut.mtt.repository;

import com.revolut.mtt.model.User;
import com.revolut.mtt.database.ConnectionProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.*;
import java.util.Optional;

/**
 * User database operations
 */
@Singleton
public class UserRepository {

    private final ConnectionProvider connectionProvider;

    @Inject
    public UserRepository(final ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public Optional<User> fetchUser(final Long userId) throws SQLException {
        final Connection connection = connectionProvider.currentConnection();

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement("select username from app_user where id = ?");
            preparedStatement.setLong(1, userId);
            resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                return Optional.empty();
            }
            final String username = resultSet.getString("username");
            return Optional.of(new User(userId, username));
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        }
    }

    public Optional<User> fetchUserByUsername(final String username) throws SQLException {
        final Connection connection = connectionProvider.currentConnection();

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement("select id from app_user where username = ?");
            preparedStatement.setString(1, username);
            resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) {
                return Optional.empty();
            }

            final Long userId = resultSet.getLong(1);
            return Optional.of(new User(userId, username));
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        }
    }

    public User createUser(final User user) throws SQLException {
        final Connection connection = connectionProvider.currentConnection();

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement("insert into app_user (username) values (?)",
                    Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, user.getUsername());
            preparedStatement.executeUpdate();
            resultSet = preparedStatement.getGeneratedKeys();
            resultSet.next();
            final Long userId = resultSet.getLong(1);
            return user.toBuilder()
                    .id(userId)
                    .build();
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        }
    }
}
