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

    private Long fromAccountId;

    private Long toAccountId;

    private BigDecimal amount;
}
