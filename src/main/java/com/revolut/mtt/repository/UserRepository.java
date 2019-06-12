package com.revolut.mtt.repository;

import com.revolut.mtt.model.User;
import com.revolut.mtt.modules.ConnectionProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.*;
import java.util.Optional;

@Singleton
public class UserRepository {

    private final ConnectionProvider connectionProvider;

    @Inject
    public UserRepository(final ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public Optional<User> fetchUser(final Long userId) throws SQLException {
        final Connection connection = connectionProvider.currentConnection();
        final PreparedStatement preparedStatement =
                connection.prepareStatement("select username from app_user where id = ?");
        preparedStatement.setLong(1, userId);
        final ResultSet resultSet = preparedStatement.executeQuery();

        if (!resultSet.next()) {
            return Optional.empty();
        }

        final String username = resultSet.getString("username");
        return Optional.of(new User(userId, username));
    }

    public User createUser(final User user) throws SQLException {
        final Connection connection = connectionProvider.currentConnection();
        final PreparedStatement preparedStatement =
                connection.prepareStatement("insert into app_user (username) values (?)", Statement.RETURN_GENERATED_KEYS);
        preparedStatement.setString(1, user.getUsername());
        preparedStatement.executeUpdate();
        final ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
        generatedKeys.next();
        final Long userId = generatedKeys.getLong(1);
        return user.toBuilder()
                .id(userId)
                .build();
    }
}
