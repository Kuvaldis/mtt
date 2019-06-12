package com.revolut.mtt.repository;

import com.revolut.mtt.model.Account;
import com.revolut.mtt.modules.ConnectionProvider;
import org.checkerframework.checker.units.qual.A;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Optional;

@Singleton
public class AccountRepository {

    private final ConnectionProvider connectionProvider;

    @Inject
    public AccountRepository(final ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    public Optional<Account> fetchAccount(final Long accountId) throws SQLException {
        return fetchAccount(accountId, false);
    }

    public Optional<Account> fetchAccount(final Long accountId,
                                          final boolean locked) throws SQLException {
        final Connection connection = connectionProvider.currentConnection();

        final String sql = locked
                ? "select user_id, balance from account where id = ? for update"
                : "select user_id, balance from account where id = ?";
        final PreparedStatement preparedStatement =
                connection.prepareStatement(sql);
        preparedStatement.setLong(1, accountId);
        final ResultSet resultSet = preparedStatement.executeQuery();

        if (!resultSet.next()) {
            return Optional.empty();
        }

        final Long userId = resultSet.getLong("user_id");
        final BigDecimal balance = resultSet.getBigDecimal("balance");
        return Optional.of(new Account(accountId, userId, balance));
    }

    public Account createAccount(final Account account) throws SQLException {
        final Connection connection = connectionProvider.currentConnection();
        final PreparedStatement preparedStatement =
                connection.prepareStatement("insert into account (user_id, balance) values (?, ?)", Statement.RETURN_GENERATED_KEYS);
        preparedStatement.setLong(1, account.getUserId());
        preparedStatement.setBigDecimal(2, account.getBalance());
        preparedStatement.executeUpdate();
        final ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
        generatedKeys.next();
        final Long accountId = generatedKeys.getLong(1);
        return account.toBuilder()
                .id(accountId)
                .build();
    }

    public boolean addBalance(final Long accountId, final BigDecimal amount) throws SQLException {
        final Connection connection = connectionProvider.currentConnection();
        final PreparedStatement preparedStatement =
                connection.prepareStatement("update account set balance = balance + ? where id = ?");
        preparedStatement.setBigDecimal(1, amount);
        preparedStatement.setLong(2, accountId);
        final int rowsUpdated = preparedStatement.executeUpdate();
        return rowsUpdated == 1;
    }
}
