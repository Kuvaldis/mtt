package com.revolut.mtt.controller;

import com.revolut.mtt.model.Account;
import com.revolut.mtt.model.User;
import com.revolut.mtt.repository.AccountRepository;
import com.revolut.mtt.repository.UserRepository;
import com.revolut.mtt.validation.ValidationError;
import com.revolut.mtt.validation.ValidationException;
import org.jooby.Result;
import org.jooby.Status;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @InjectMocks
    private AccountController accountController;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void existing_account_should_be_returned_from_repository() throws Exception {
        // given
        final long accountId = 1L;
        final long userId = 2L;
        final BigDecimal balance = new BigDecimal(100);
        when(accountRepository.fetchAccount(accountId))
                .thenReturn(Optional.of(new Account(accountId, userId, balance)));

        // when
        final Result accountResult = accountController.fetchAccount(accountId);

        // then
        assertNotNull(accountResult);
        assertEquals(Optional.of(Status.OK), accountResult.status());
        final Account account = accountResult.get();
        assertNotNull(account);
        assertEquals(1L, account.getId());
        assertEquals(2L, account.getUserId());
        assertEquals(new BigDecimal(100), account.getBalance());
        verify(accountRepository).fetchAccount(accountId);
    }

    @Test
    void non_existing_account_should_return_not_found_status() throws Exception {
        // given
        final long accountId = 4L;

        // when
        final Result accountResult = accountController.fetchAccount(accountId);

        // then
        assertNotNull(accountResult);
        assertEquals(Optional.of(Status.NOT_FOUND), accountResult.status());
        verify(accountRepository).fetchAccount(accountId);
    }

    @Test
    void account_should_be_created_and_saved_in_repository() throws Exception {
        // given
        final Account account = new Account(null, 2L, new BigDecimal(150));
        when(accountRepository.createAccount(account))
                .thenReturn(new Account(1L, 2L, new BigDecimal(150)));
        when(userRepository.fetchUser(2L))
                .thenReturn(Optional.of(new User(2L, "phoebe")));

        // when
        final Result createdAccountResult = accountController.createAccount(account);

        // then
        assertNotNull(createdAccountResult);
        assertEquals(Optional.of(Status.CREATED), createdAccountResult.status());
        final Account createdAccount = createdAccountResult.get();
        assertNotNull(createdAccount);
        assertEquals(1L, createdAccount.getId());
        assertEquals(2L, createdAccount.getUserId());
        assertEquals(new BigDecimal(150), createdAccount.getBalance());
        verify(accountRepository).createAccount(account);
        verify(userRepository).fetchUser(2L);
    }

    @Test
    void account_should_have_user_id() {
        // given
        final Account account = new Account(null, null, new BigDecimal(10));

        // when
        final ValidationException validationException =
                assertThrows(ValidationException.class, () -> accountController.createAccount(account));

        // then
        final List<ValidationError> errors = validationException.getErrors();
        assertEquals(1, errors.size());
        final ValidationError error = errors.get(0);
        assertEquals("userId", error.getField());
    }

    @Test
    void account_should_have_existing_user() throws SQLException {
        // given
        final Account account = new Account(null, 3L, new BigDecimal(10));
        when(userRepository.fetchUser(3L))
                .thenReturn(Optional.empty());

        // when
        final ValidationException validationException =
                assertThrows(ValidationException.class, () -> accountController.createAccount(account));

        // then
        final List<ValidationError> errors = validationException.getErrors();
        assertEquals(1, errors.size());
        final ValidationError error = errors.get(0);
        assertEquals("userId", error.getField());
    }

    @Test
    void account_should_have_non_negative() throws SQLException {
        // given
        final Account account = new Account(null, 2L, new BigDecimal(-10));
        when(userRepository.fetchUser(2L))
                .thenReturn(Optional.of(new User(2L, "phoebe")));

        // when
        final ValidationException validationException =
                assertThrows(ValidationException.class, () -> accountController.createAccount(account));

        // then
        final List<ValidationError> errors = validationException.getErrors();
        assertEquals(1, errors.size());
        final ValidationError error = errors.get(0);
        assertEquals("balance", error.getField());
    }
}