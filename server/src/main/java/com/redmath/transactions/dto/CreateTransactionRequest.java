package com.redmath.transactions.dto;

import com.redmath.transactions.entity.Indicator;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
//@RequiredArgsConstructor
@NoArgsConstructor
public class CreateTransactionRequest
{
    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(
            value = "0.01",
            message = "Amount must be greater than zero"
    )

    private BigDecimal amount;

    @NotNull(message = "Indicator is required")
    private Indicator indicator;

}