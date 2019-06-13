package com.revolut.mtt.validation;

import lombok.Builder;
import lombok.Data;

/**
 * Common validation error.
 */
@Data
@Builder
public class ValidationError {

    private String field;

    private String message;
}
