package com.revolut.mtt.repository;

import com.revolut.mtt.model.Account;
import com.revolut.mtt.model.User;
import org.jooby.Router;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class AccountRepository {

    public Optional<Account> fetchAccount(final Long accountId) {
        return null;
    }

    public Account createAccount(final Account account) {
        return null;
    }
}
