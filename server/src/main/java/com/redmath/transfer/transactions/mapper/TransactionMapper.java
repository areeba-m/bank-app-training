package com.redmath.transfer.transactions.mapper;

import com.redmath.transfer.transactions.entity.Transaction;
import com.redmath.transfer.transactions.dto.CreateTransactionRequest;
import com.redmath.transfer.transactions.dto.TransactionResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper
{
    public Transaction toEntity(@NonNull CreateTransactionRequest request)
    {
        Transaction transaction = new Transaction();

        transaction.setDescription(request.getDescription());
        transaction.setAmount(request.getAmount());
        transaction.setIndicator(request.getIndicator());

        return transaction;
    }

    public TransactionResponse toResponse(@NonNull Transaction transaction)
    {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getDate(),
                transaction.getDescription(),
                transaction.getAmount(),
                transaction.getIndicator().name(),
                transaction.getCounterpartyName(),
                transaction.getCounterpartyEmail()
        );
    }
}