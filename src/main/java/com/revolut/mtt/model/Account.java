package com.revolut.mtt.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Class representing user account. Assume currency is always the same for sake of simplicity.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Account {

    private Long id;

    private Long userId;

    private BigDecimal balance;

    public BigDecimal getBalance() {
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
        return balance;
    }
}
