package com.redmath.transfer.mapper;

import com.redmath.account.entity.Account;
import com.redmath.transfer.dto.TransferResponse;
import com.redmath.transfer.entity.Transfer;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class TransferMapper {

    public Transfer toEntity(@NonNull String description, Account sender, Account receiver, BigDecimal amount) {
        Transfer transfer = new Transfer();

        transfer.setDescription(description);
        transfer.setAmount(amount);
        transfer.setSenderAccount(sender);
        transfer.setReceiverAccount(receiver);

        return transfer;
    }

    public TransferResponse toResponse(@NonNull Transfer transfer) {
        return new TransferResponse(
                transfer.getDate(),
                transfer.getDescription(),
                transfer.getAmount(),
                transfer.getSenderAccount().getEmail(),
                transfer.getReceiverAccount().getEmail()
        );
    }
}
