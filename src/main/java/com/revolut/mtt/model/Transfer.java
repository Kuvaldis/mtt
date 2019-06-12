package com.revolut.mtt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Class representing money transfer between two accounts
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Transfer {

    /**
     * User making actual transfer
     */
    private Long endUserId;

    private Long sourceAccountId;

    private Long destinationAccountId;

    private BigDecimal amount;
}
