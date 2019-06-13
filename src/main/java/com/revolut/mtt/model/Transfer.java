package com.revolut.mtt.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;

import java.math.BigDecimal;

/**
 * Class representing money transfer between two accounts. Made immutable.
 */
@Value
@Builder(toBuilder = true, builderClassName = "TransferBuilder")
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(builder = Transfer.TransferBuilder.class)
public class Transfer {

    /**
     * On behalf of who the transfer is actually being made.
     */
    private final Long endUserId;

    private final Long sourceAccountId;

    private final Long destinationAccountId;

    private final BigDecimal amount;

    @JsonPOJOBuilder(withPrefix = "")
    public static class TransferBuilder {
    }
}
