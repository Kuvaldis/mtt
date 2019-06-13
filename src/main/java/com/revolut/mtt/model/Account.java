package com.revolut.mtt.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Class representing user account. Assume currency is always the same for sake of simplicity. Made immutable.
 */
@Data
@Builder(toBuilder = true, builderClassName = "AccountBuilder")
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = Account.AccountBuilder.class)
public class Account {

    private final Long id;

    private final Long userId;

    @Builder.Default
    private final BigDecimal balance = BigDecimal.ZERO;

    @JsonPOJOBuilder(withPrefix = "")
    public static class AccountBuilder {
    }
}
