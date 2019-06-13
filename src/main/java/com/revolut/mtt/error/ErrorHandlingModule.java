package com.revolut.mtt.error;

import com.google.inject.Binder;
import com.revolut.mtt.validation.ValidationException;
import com.typesafe.config.Config;
import lombok.extern.slf4j.Slf4j;
import org.jooby.*;

/**
 * Common errors handling to convert exceptions to http response.
 */
@Slf4j
public class ErrorHandlingModule implements Jooby.Module {

    @Override
    public void configure(final Env env, final Config conf, final Binder binder) throws Throwable {
        final Router router = env.router();

        // validation errors
        router.err(ValidationException.class, (req, rsp, err) -> {
            final ValidationException validationException = (ValidationException) err.getCause();
            if (log.isDebugEnabled()) {
                log.debug("Validation exception appeared", validationException);
            }
            rsp.send(Results.json(validationException.getErrors())
                    .status(Status.BAD_REQUEST));
        });

        // other errors
        router.err((req, rsp, err) -> {
            // handle any other exception
            final Throwable cause = err.getCause();
            log.error("Unexpected error appeared", cause);
            final String message = cause.getMessage();
            rsp.send(Results.json(message).status(Status.SERVER_ERROR));
        });
    }
}
