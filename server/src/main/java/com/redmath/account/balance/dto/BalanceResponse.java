package com.redmath.account.balance.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class BalanceResponse {

    private Instant date;
    private BigDecimal amount;
    private String indicator;
}
