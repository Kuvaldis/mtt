package com.revolut.mtt.controller;

import com.revolut.mtt.model.Account;
import com.revolut.mtt.model.Transfer;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferControllerTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TransferController transferController;

    private final Transfer correctTransfer = new Transfer(1L, 2L, 3L, BigDecimal.TEN);

    private final User correctUser = new User(1L, "rachel");

    private final Account correctSourceAccount = new Account(2L, 1L, new BigDecimal(100));

    private final Account correctDestinationAccount = new Account(3L, 1L, new BigDecimal(200));

    @Test
    void transfer_amount_should_be_positive() throws Exception {
        // given
        final Transfer zeroTransfer = correctTransfer.toBuilder()
                .amount(BigDecimal.ZERO)
                .build();
        final Transfer negativeTransfer = correctTransfer.toBuilder()
                .amount(BigDecimal.ONE.negate())
                .build();

        // when
        final ValidationException ztValidationException =
                assertThrows(ValidationException.class, () -> transferController.createTransfer(zeroTransfer));
        final ValidationException ntValidationException =
                assertThrows(ValidationException.class, () -> transferController.createTransfer(negativeTransfer));

        // then
        assertEquals(1, ztValidationException.getErrors().size());
        final ValidationError ztError = ztValidationException.getErrors().get(0);
        assertEquals("amount", ztError.getField());
        assertEquals(1, ntValidationException.getErrors().size());
        final ValidationError ntError = ntValidationException.getErrors().get(0);
        assertEquals("amount", ntError.getField());
        verifyTransferDidNotHappen(zeroTransfer);
        verifyTransferDidNotHappen(negativeTransfer);
    }

    @Test
    void transfer_end_user_id_should_not_be_null() throws SQLException {
        // given
        final Transfer transfer = correctTransfer.toBuilder()
                .endUserId(null)
                .build();

        // when
        final ValidationException validationException =
                assertThrows(ValidationException.class, () -> transferController.createTransfer(transfer));

        // then
        assertEquals(1, validationException.getErrors().size());
        final ValidationError error = validationException.getErrors().get(0);
        assertEquals("endUserId", error.getField());
        verifyTransferDidNotHappen(transfer);
    }

    @Test
    void transfer_source_account_id_should_not_be_null() throws SQLException {
        // given
        final Transfer transfer = correctTransfer.toBuilder()
                .sourceAccountId(null)
                .build();

        // when
        final ValidationException validationException =
                assertThrows(ValidationException.class, () -> transferController.createTransfer(transfer));

        // then
        assertEquals(1, validationException.getErrors().size());
        final ValidationError error = validationException.getErrors().get(0);
        assertEquals("sourceAccountId", error.getField());
        verifyTransferDidNotHappen(transfer);
    }

    @Test
    void transfer_destination_account_id_should_not_be_null() throws SQLException {
        // given
        final Transfer transfer = correctTransfer.toBuilder()
                .destinationAccountId(null)
                .build();

        // when
        final ValidationException validationException =
                assertThrows(ValidationException.class, () -> transferController.createTransfer(transfer));

        // then
        assertEquals(1, validationException.getErrors().size());
        final ValidationError error = validationException.getErrors().get(0);
        assertEquals("destinationAccountId", error.getField());
        verifyTransferDidNotHappen(transfer);
    }

    @Test
    void transfer_source_and_destination_account_ids_should_be_different() throws SQLException {
        // given
        final Transfer transfer = correctTransfer.toBuilder()
                .sourceAccountId(2L)
                .destinationAccountId(2L)
                .build();

        // when
        final ValidationException validationException =
                assertThrows(ValidationException.class, () -> transferController.createTransfer(transfer));

        // then
        assertEquals(2, validationException.getErrors().size());
        final Set<String> errorFields = validationException.getErrors().stream()
                .map(ValidationError::getField)
                .collect(Collectors.toSet());
        assertTrue(errorFields.contains("sourceAccountId"));
        assertTrue(errorFields.contains("destinationAccountId"));
        verifyTransferDidNotHappen(transfer);
    }

    @Test
    void transfer_end_user_should_exist() throws SQLException {
        // given
        final Transfer transfer = correctTransfer;

        // when
        final ValidationException validationException =
                assertThrows(ValidationException.class, () -> transferController.createTransfer(transfer));

        // then
        assertEquals(1, validationException.getErrors().size());
        final ValidationError error = validationException.getErrors().get(0);
        assertEquals("endUserId", error.getField());
        verify(userRepository).fetchUser(1L);
        verifyTransferDidNotHappen(transfer);
    }

    @Test
    void transfer_should_not_happen_if_accounts_cannot_be_acquired() throws SQLException {
        // given
        final Transfer transfer = correctTransfer;
        when(userRepository.fetchUser(1L))
                .thenReturn(Optional.of(correctUser));

        // when
        final ValidationException validationException =
                assertThrows(ValidationException.class, () -> transferController.createTransfer(transfer));

        // then
        assertEquals(2, validationException.getErrors().size());
        final Set<String> errorFields = validationException.getErrors().stream()
                .map(ValidationError::getField)
                .collect(Collectors.toSet());
        assertTrue(errorFields.contains("sourceAccountId"));
        assertTrue(errorFields.contains("destinationAccountId"));
        verifyAccountsLocked(transfer);
        verifyTransferDidNotHappen(transfer);
    }

    @Test
    void transfer_should_not_happen_if_amount_exceeds_source_account_balance() throws SQLException {
        // given
        final Transfer transfer = correctTransfer.toBuilder()
                .amount(new BigDecimal(100_000))
                .build();
        when(userRepository.fetchUser(1L))
                .thenReturn(Optional.of(correctUser));
        when(accountRepository.fetchAccount(2L, true))
                .thenReturn(Optional.of(correctSourceAccount));
        when(accountRepository.fetchAccount(3L, true))
                .thenReturn(Optional.of(correctDestinationAccount));

        // when
        final ValidationException validationException =
                assertThrows(ValidationException.class, () -> transferController.createTransfer(transfer));

        // then
        assertEquals(1, validationException.getErrors().size());
        final ValidationError error = validationException.getErrors().get(0);
        assertEquals("amount", error.getField());
        verifyAccountsLocked(transfer);
        verifyTransferDidNotHappen(transfer);
    }

    @Test
    void transfer_should_not_happen_if_source_account_does_not_belong_to_end_user() throws SQLException {
        // given
        final Transfer transfer = correctTransfer;
        when(userRepository.fetchUser(1L))
                .thenReturn(Optional.of(correctUser));
        when(accountRepository.fetchAccount(2L, true))
                .thenReturn(Optional.of(correctSourceAccount.toBuilder()
                        .userId(2L)
                        .build()));
        when(accountRepository.fetchAccount(3L, true))
                .thenReturn(Optional.of(correctDestinationAccount));

        // when
        final ValidationException validationException =
                assertThrows(ValidationException.class, () -> transferController.createTransfer(transfer));

        // then
        assertEquals(1, validationException.getErrors().size());
        final ValidationError error = validationException.getErrors().get(0);
        assertEquals("sourceAccountId", error.getField());
        verifyAccountsLocked(transfer);
        verifyTransferDidNotHappen(transfer);
    }

    @Test
    void transfer_should_not_happen_if_at_least_one_account_cannot_be_updated() throws SQLException {
        // given
        when(userRepository.fetchUser(1L))
                .thenReturn(Optional.of(correctUser));
        when(accountRepository.fetchAccount(2L, true))
                .thenReturn(Optional.of(correctSourceAccount));
        when(accountRepository.fetchAccount(3L, true))
                .thenReturn(Optional.of(correctDestinationAccount));

        // when
        final ValidationException validationException =
                assertThrows(ValidationException.class, () -> transferController.createTransfer(correctTransfer));

        // then
        assertEquals(2, validationException.getErrors().size());
        final Set<String> errorFields = validationException.getErrors().stream()
                .map(ValidationError::getField)
                .collect(Collectors.toSet());
        assertTrue(errorFields.contains("sourceAccountId"));
        assertTrue(errorFields.contains("destinationAccountId"));
        verifyAccountsLocked(correctTransfer);
    }

    @Test
    void transfer_should_happen_for_correct_data() throws Exception {
        // given
        when(userRepository.fetchUser(1L))
                .thenReturn(Optional.of(correctUser));
        when(accountRepository.fetchAccount(2L, true))
                .thenReturn(Optional.of(correctSourceAccount));
        when(accountRepository.fetchAccount(3L, true))
                .thenReturn(Optional.of(correctDestinationAccount));
        when(accountRepository.applyBalance(correctTransfer.getSourceAccountId(), new BigDecimal(90)))
                .thenReturn(true);
        when(accountRepository.applyBalance(correctTransfer.getDestinationAccountId(), new BigDecimal(210)))
                .thenReturn(true);

        // when
        final Result transferResult = transferController.createTransfer(correctTransfer);

        // then
        assertNotNull(transferResult);
        assertTrue(transferResult.status().isPresent());
        assertEquals(Status.OK, transferResult.status().get());
        verifyAccountsLocked(correctTransfer);
        verify(accountRepository).applyBalance(correctTransfer.getSourceAccountId(), new BigDecimal(90));
        verify(accountRepository).applyBalance(correctTransfer.getDestinationAccountId(), new BigDecimal(210));
    }

    private void verifyAccountsLocked(final Transfer transfer) throws SQLException {
        verify(accountRepository).fetchAccount(transfer.getSourceAccountId(), true);
        verify(accountRepository).fetchAccount(transfer.getDestinationAccountId(), true);
    }

    private void verifyTransferDidNotHappen(final Transfer transfer) throws SQLException {
        verify(accountRepository, never()).applyBalance(eq(transfer.getSourceAccountId()), any());
        verify(accountRepository, never()).applyBalance(eq(transfer.getDestinationAccountId()), any());
    }
}