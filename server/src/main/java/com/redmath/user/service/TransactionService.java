package com.redmath.user.service;


import com.redmath.account.Account;
import com.redmath.account.AccountRepository;
import com.redmath.user.dto.*;
import com.redmath.user.entity.*;
import com.redmath.user.mapper.TransactionMapper;
import com.redmath.user.repository.*;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final balanceRepository balanceRepository;
    private final TransactionMapper transactionMapper;

    @Transactional
    public TransactionResponse createTransaction(@NonNull CreateTransactionRequest request){
        if(request.getAmount().compareTo(BigDecimal.ZERO)<=0){
            throw new RuntimeException(
                    "Amount must be positive"
            );
        }
        Account account = accountRepository.findById(
                request.getAccountId())
                .orElseThrow(() -> new RuntimeException(
                                        "Account not found"
                ));
        balanceEntity balance = balanceRepository.findByAccountUserId(account.getUserId())
                .orElseThrow(() -> new RuntimeException(
                                        "Balance not found"
                ));

        if(request.getIndicator() == Indicator.CR){
            balance.setAmount(balance.getAmount()
                    .add(request.getAmount()));
        }
        else if(request.getIndicator()
                == Indicator.DB){
            if(balance.getAmount().compareTo(request.getAmount()) < 0){
                throw new RuntimeException(
                        "Insufficient balance");
            }
            balance.setAmount(balance.getAmount()
                    .subtract(
                            request.getAmount())
            );

        }

        TransactionEntity transaction = transactionMapper.toEntity(request);
        transaction.setAccount(account);
        transaction.setDate(Instant.now());
        TransactionEntity saved = transactionRepository.save(transaction);

        balanceRepository.save(balance);
        return transactionMapper.toResponse(saved);

    }

    public List<TransactionResponse> getTransactions(Long accountId) {
        accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException(
                        "Account with id " + accountId + " not found")
                );
        return transactionRepository
                .findByAccountUserId(accountId)
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }
}