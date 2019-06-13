package com.revolut.mtt.repository;

import com.revolut.mtt.model.Account;
import com.revolut.mtt.database.ConnectionProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Optional;

/**
 * Account database operations
 */
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

    /**
     * Fetches account from database. If 'locked' is true then adds a lock to account record.
     *
     * @return empty optional if account does not exist or cannot be locked, non-empty otherwise.
     */
    public Optional<Account> fetchAccount(final Long accountId,
                                          final boolean locked) throws SQLException {
        final Connection connection = connectionProvider.currentConnection();

        final String sql = locked
                ? "select user_id, balance from account where id = ? for update"
                : "select user_id, balance from account where id = ?";

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, accountId);
            resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                return Optional.empty();
            }

            final Long userId = resultSet.getLong("user_id");
            final BigDecimal balance = resultSet.getBigDecimal("balance");
            return Optional.of(new Account(accountId, userId, balance));
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
        }
    }

    public Account createAccount(final Account account) throws SQLException {
        final Connection connection = connectionProvider.currentConnection();

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection.prepareStatement("insert into account (user_id, balance) values (?, ?)",
                            Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setLong(1, account.getUserId());
            preparedStatement.setBigDecimal(2, account.getBalance() == null ? BigDecimal.ZERO : account.getBalance());
            preparedStatement.executeUpdate();
            resultSet = preparedStatement.getGeneratedKeys();
            resultSet.next();
            final Long accountId = resultSet.getLong(1);
            return account.toBuilder()
                    .id(accountId)
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

    /**
     * Sets new balance for account.
     *
     * @return true if balance applied, otherwise false.
     */
    public boolean applyBalance(final Long accountId, final BigDecimal newBalance) throws SQLException {
        final Connection connection = connectionProvider.currentConnection();
        try (final PreparedStatement preparedStatement =
                connection.prepareStatement("update account set balance = ? where id = ?")) {
            preparedStatement.setBigDecimal(1, newBalance);
            preparedStatement.setLong(2, accountId);
            final int rowsUpdated = preparedStatement.executeUpdate();
            return rowsUpdated == 1;
        }
    }
}
