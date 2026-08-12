package com.redmath.transactions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
public class TransactionResponse {

    private Instant date;
    private String description;
    private BigDecimal amount;
    private String indicator;
    private String counterpartyName;
    private String counterpartyEmail;
}