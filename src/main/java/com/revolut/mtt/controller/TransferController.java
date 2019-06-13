package com.revolut.mtt.controller;

import com.revolut.mtt.model.Account;
import com.revolut.mtt.model.Transfer;
import com.revolut.mtt.model.User;
import com.revolut.mtt.repository.AccountRepository;
import com.revolut.mtt.repository.UserRepository;
import com.revolut.mtt.validation.ValidationError;
import com.revolut.mtt.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.jooby.Result;
import org.jooby.Results;
import org.jooby.Status;
import org.jooby.mvc.Body;
import org.jooby.mvc.POST;
import org.jooby.mvc.Path;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.*;

/**
 * Entry point for transfer between account operations.
 */
@Slf4j
@Singleton
@Path("/transfers")
public class TransferController {

    private final AccountRepository accountRepository;

    private final UserRepository userRepository;

    @Inject
    public TransferController(final AccountRepository accountRepository,
                              final UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    /**
     * Implementation is based on database locks. Both accounts are locked to update balance.
     * Other options:
     * 1. Use optimistic locking based on account version. Makes solution a bit complicated, and not really required in a real world.
     * 2. Lock only source account. However, during update there is a risk of data integrity problems.
     */
    @POST
    public Result createTransfer(final @Body Transfer transfer) throws Exception {
        final List<ValidationError> validationErrors = new ArrayList<>();

        // simple validation before actual calls to repositories
        //noinspection CollectionAddAllCanBeReplacedWithConstructor
        validationErrors.addAll(validateTransferData(transfer));
        if (!validationErrors.isEmpty()) {
            throw new ValidationException(validationErrors);
        }

        // check user and toAccount first, so we don't have to set lock if it's not required
        final User endUser = userRepository.fetchUser(transfer.getEndUserId())
                .orElse(null);
        validationErrors.addAll(validateEndUserExists(endUser));
        if (!validationErrors.isEmpty()) {
            throw new ValidationException(validationErrors);
        }

        log.info("Acquire account locks for accounts {}, {}", transfer.getSourceAccountId(), transfer.getDestinationAccountId());
        final Account sourceAccount;
        final Account destinationAccount;
        // to prevent deadlocks, always fetch account with lower id first
        if (transfer.getSourceAccountId() < transfer.getDestinationAccountId()) {
            sourceAccount = accountRepository.fetchAccount(transfer.getSourceAccountId(), true)
                    .orElse(null);
            destinationAccount = accountRepository.fetchAccount(transfer.getDestinationAccountId(), true)
                    .orElse(null);
        } else {
            destinationAccount = accountRepository.fetchAccount(transfer.getDestinationAccountId(), true)
                    .orElse(null);
            sourceAccount = accountRepository.fetchAccount(transfer.getSourceAccountId(), true)
                    .orElse(null);
        }
        validationErrors.addAll(validateAccountAcquired(sourceAccount, "sourceAccountId"));
        validationErrors.addAll(validateAccountAcquired(destinationAccount, "destinationAccountId"));
        validationErrors.addAll(validateSourceHasEnoughAmount(sourceAccount, transfer.getAmount()));
        validationErrors.addAll(validateAccountBelongToUser(sourceAccount, transfer.getEndUserId()));

        if (!validationErrors.isEmpty()) {
            throw new ValidationException(validationErrors);
        }
        log.info("Account locks for accounts {}, {} are successfully acquired",
                transfer.getSourceAccountId(), transfer.getDestinationAccountId());

        Objects.requireNonNull(sourceAccount);
        Objects.requireNonNull(destinationAccount);
        final boolean sourceUpdated = accountRepository.applyBalance(sourceAccount.getId(),
                sourceAccount.getBalance().add(transfer.getAmount().negate()));
        validationErrors.addAll(validateAccountUpdated(sourceUpdated, "sourceAccountId"));
        final boolean destinationUpdated = accountRepository.applyBalance(destinationAccount.getId(),
                destinationAccount.getBalance().add(transfer.getAmount()));
        validationErrors.addAll(validateAccountUpdated(destinationUpdated, "destinationAccountId"));
        if (!validationErrors.isEmpty()) {
            throw new ValidationException(validationErrors);
        }
        log.info("New balances for accounts {}, {} are applied",
                transfer.getSourceAccountId(), transfer.getDestinationAccountId());

        return Results.with(Status.OK);
    }

    private List<ValidationError> validateTransferData(final Transfer transfer) {
        final List<ValidationError> validationErrors = new ArrayList<>();
        if (transfer == null) {
            validationErrors.add(ValidationError.builder()
                    .message("Transfer should not be null")
                    .build());
            return validationErrors;
        }

        // validate amount is positive
        if (transfer.getAmount() == null || transfer.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            validationErrors.add(ValidationError.builder()
                    .field("amount")
                    .message("Amount should be positive")
                    .build());
        }

        // validate user
        if (transfer.getEndUserId() == null) {
            validationErrors.add(ValidationError.builder()
                    .field("endUserId")
                    .message("End user id should not be null")
                    .build());
        }

        // validate both accounts
        if (transfer.getSourceAccountId() == null) {
            validationErrors.add(ValidationError.builder()
                    .field("sourceAccountId")
                    .message("From account id should not be null")
                    .build());
        }
        if (transfer.getDestinationAccountId() == null) {
            validationErrors.add(ValidationError.builder()
                    .field("destinationAccountId")
                    .message("To account id should not be null")
                    .build());
        }

        // validate not same account
        if (transfer.getSourceAccountId() != null &&
                transfer.getDestinationAccountId() != null &&
                transfer.getSourceAccountId().equals(transfer.getDestinationAccountId())) {
            validationErrors.add(ValidationError.builder()
                    .field("sourceAccountId")
                    .message("Account ids should be different")
                    .build());
            validationErrors.add(ValidationError.builder()
                    .field("destinationAccountId")
                    .message("Account ids should be different")
                    .build());
        }

        return validationErrors;
    }

    private List<ValidationError> validateEndUserExists(final User endUser) {
        if (endUser == null) {
            return Collections.singletonList(ValidationError.builder()
                    .field("endUserId")
                    .message("End user should exist")
                    .build());
        }
        return Collections.emptyList();
    }

    private List<ValidationError> validateAccountAcquired(final Account account,
                                                          final String field) {
        if (account == null) {
            return Collections.singletonList(ValidationError.builder()
                    .field(field)
                    .message("Account should cannot be acquired")
                    .build());
        }
        return Collections.emptyList();
    }

    private List<ValidationError> validateSourceHasEnoughAmount(final Account account,
                                                                final BigDecimal transferAmount) {
        if (account != null && account.getBalance().compareTo(transferAmount) < 0) {
            return Collections.singletonList(ValidationError.builder()
                    .field("amount")
                    .message("Account does not have enough amount")
                    .build());
        }
        return Collections.emptyList();
    }

    private List<ValidationError> validateAccountBelongToUser(final Account account,
                                                              final Long endUserId) {
        if (account != null && endUserId != null
                && !endUserId.equals(account.getUserId())) {
            return Collections.singletonList(ValidationError.builder()
                    .field("sourceAccountId")
                    .message("Account does not belong to user")
                    .build());
        }
        return Collections.emptyList();
    }

    private List<ValidationError> validateAccountUpdated(final boolean accountUpdated,
                                                         final String field) {
        if (!accountUpdated) {
            return Collections.singletonList(ValidationError.builder()
                    .field(field)
                    .message("Account could not be updated. Try again.")
                    .build());
        }
        return Collections.emptyList();
    }
}
