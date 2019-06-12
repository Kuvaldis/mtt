package com.revolut.mtt.repository;

import com.github.database.rider.core.api.connection.ConnectionHolder;
import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.junit5.DBUnitExtension;
import com.revolut.mtt.model.Account;
import com.revolut.mtt.modules.SchemaInit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(DBUnitExtension.class)
class AccountRepositoryTest {

    private AccountRepository accountRepository;

    private static ConnectionHolder connectionHolder = () -> DriverManager.getConnection("jdbc:h2:mem:mtt-test;DB_CLOSE_DELAY=-1");

    @BeforeAll
    static void beforeAll() throws SQLException {
        SchemaInit.initSchema(connectionHolder.getConnection());
    }

    @BeforeEach
    void setUp() {
        this.accountRepository = new AccountRepository(connectionHolder::getConnection);
    }

    @Test
    @DataSet("existing_users.yml")
    void should_fetch_account_by_id() throws SQLException {
        // given
        final Long accountId = 10L;

        /// when
        final Optional<Account> optionalAccount = accountRepository.fetchAccount(accountId);

        // then
        assertTrue(optionalAccount.isPresent());
        final Account account = optionalAccount.get();
        assertEquals(10L, account.getId());
        assertEquals(1L, account.getUserId());
        assertEquals(new BigDecimal("100.31"), account.getBalance());
    }

    @Test
    @DataSet("existing_users.yml")
    void should_not_fetch_non_existing_account() throws SQLException {
        // given
        final Long accountId = 123L;

        /// when
        final Optional<Account> optionalAccount = accountRepository.fetchAccount(accountId);

        // then
        assertFalse(optionalAccount.isPresent());
    }

    @Test
    @DataSet("existing_users.yml")
    void should_create_new_account() throws SQLException {
        // given
        final Account newAccount = Account.builder()
                .userId(3L)
                .balance(new BigDecimal(123))
                .build();

        // when
        final Account account = accountRepository.createAccount(newAccount);

        // then
        assertNotNull(account);
        assertNotNull(account.getId());
        assertEquals(3L, account.getUserId());
        assertEquals(new BigDecimal(123), account.getBalance());
    }
}