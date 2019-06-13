package com.revolut.mtt.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Class representing money transfer between two accounts.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Transfer {

    /**
     * On behalf of who the transfer is actually being made.
     */
    private Long endUserId;

    private Long sourceAccountId;

    private Long destinationAccountId;

    private BigDecimal amount;
}
