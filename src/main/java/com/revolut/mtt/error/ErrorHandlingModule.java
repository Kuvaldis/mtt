package com.revolut.mtt.error;

import com.google.inject.Binder;
import com.revolut.mtt.validation.ValidationException;
import com.typesafe.config.Config;
import org.jooby.*;

public class ErrorHandlingModule implements Jooby.Module {

    @Override
    public void configure(final Env env, final Config conf, final Binder binder) throws Throwable {
        final Router router = env.router();
        router.err(ValidationException.class, (req, rsp, err) -> {
            final ValidationException validationException = (ValidationException) err.getCause();
            rsp.send(Results.json(validationException.getErrors())
                    .status(Status.BAD_REQUEST));
        });
        router.err((req, rsp, err) -> {
            // handle any other exception
            final String message = err.getCause().getMessage();
            rsp.send(Results.json(message).status(Status.SERVER_ERROR));
        });
    }
}
