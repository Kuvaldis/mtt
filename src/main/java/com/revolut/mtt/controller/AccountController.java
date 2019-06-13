package com.revolut.mtt.controller;

import com.revolut.mtt.model.Account;
import com.revolut.mtt.repository.AccountRepository;
import com.revolut.mtt.repository.UserRepository;
import com.revolut.mtt.validation.ValidationError;
import com.revolut.mtt.validation.ValidationException;
import org.jooby.MediaType;
import org.jooby.Result;
import org.jooby.Results;
import org.jooby.Status;
import org.jooby.mvc.Body;
import org.jooby.mvc.GET;
import org.jooby.mvc.POST;
import org.jooby.mvc.Path;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Entry point for account operations.
 */
@Singleton
@Path("/accounts")
public class AccountController {

    private final AccountRepository accountRepository;

    private final UserRepository userRepository;

    @Inject
    public AccountController(final AccountRepository accountRepository,
                             final UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @GET
    @Path("/{accountId}")
    public Result fetchAccount(final long accountId) throws Exception {
        return accountRepository.fetchAccount(accountId)
                .map(Results::ok)
                .orElseGet(() -> Results.with(Status.NOT_FOUND));
    }

    @POST
    public Result createAccount(final @Body Account account) throws Exception {
        final List<ValidationError> validationErrors = validateNewAccount(account);
        if (!validationErrors.isEmpty()) {
            throw new ValidationException(validationErrors);
        }
        final Account createdAccount = accountRepository.createAccount(account);
        return Results.with(createdAccount, Status.CREATED)
                .type(MediaType.json);
    }

    private List<ValidationError> validateNewAccount(final Account account) throws SQLException {
        final List<ValidationError> validationErrors = new ArrayList<>();
        if (account == null) {
            validationErrors.add(ValidationError.builder()
                    .message("Account should not be null")
                    .build());
        } else {
            if (account.getUserId() == null) {
                validationErrors.add(ValidationError.builder()
                        .field("userId")
                        .message("User id should not be null")
                        .build());
            } else if (userRepository.fetchUser(account.getUserId()).isEmpty()) {
                validationErrors.add(ValidationError.builder()
                        .field("userId")
                        .message("User should exist")
                        .build());
            }
            if (account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
                validationErrors.add(ValidationError.builder()
                        .field("balance")
                        .message("Balance should be non-negative")
                        .build());
            }
        }
        return validationErrors;
    }
}
