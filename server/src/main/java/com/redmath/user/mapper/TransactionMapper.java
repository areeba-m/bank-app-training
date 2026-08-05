package com.redmath.user.mapper;


import com.redmath.user.dto.CreateTransactionRequest;
import com.redmath.user.dto.TransactionResponse;
import com.redmath.user.entity.TransactionEntity;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;


import java.time.Instant;


@Component
public class TransactionMapper {

    public TransactionEntity toEntity(@NonNull CreateTransactionRequest request) {

        TransactionEntity transaction = new TransactionEntity();
        transaction.setDescription(request.getDescription());
        transaction.setAmount(request.getAmount());
        transaction.setIndicator(request.getIndicator());
        transaction.setDate(Instant.now());

        return transaction;
    }

    public TransactionResponse toResponse(@NonNull TransactionEntity transaction) {

        return new TransactionResponse(
                transaction.getDate(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getIndicator().name()
        );

    }

}