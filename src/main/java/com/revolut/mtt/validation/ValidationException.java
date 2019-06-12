package com.revolut.mtt.validation;

import java.util.List;

public class ValidationException extends RuntimeException {

    private final List<ValidationError> errors;

    public ValidationException(final List<ValidationError> errors) {
        super("Validation errors: " + errors.toString());
        this.errors = errors;
    }

    public List<ValidationError> getErrors() {
        return errors;
    }
}
