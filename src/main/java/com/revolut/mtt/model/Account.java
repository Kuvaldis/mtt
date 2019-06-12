package com.revolut.mtt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Class representing user account. Assume currency is always the same.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    private Long accountId;

    private Long userId;

    private BigDecimal balance;
}
