package com.redmath.transactions.service;

import com.redmath.account.Account;
import com.redmath.account.AccountRepository;
import com.redmath.balance.Balance;
import com.redmath.balance.exception.BalanceNotFoundException;
import com.redmath.balance.repository.BalanceRepository;
import com.redmath.transactions.Indicator;
import com.redmath.transactions.Transaction;
import com.redmath.transactions.dto.CreateTransactionRequest;
import com.redmath.transactions.dto.TransactionResponse;
import com.redmath.transactions.exception.AccountNotFoundException;
import com.redmath.transactions.exception.InsufficientBalanceException;
import com.redmath.transactions.mapper.TransactionMapper;
import com.redmath.transactions.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService
{
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;
    private final BalanceRepository balanceRepository;

    private void updateBalance(Balance balance, BigDecimal amount, @NonNull Indicator indicator)
    {
        switch (indicator) {
            case CR -> balance.setAmount(balance.getAmount().add(amount));
            case DB ->
            {
                if (balance.getAmount().compareTo(amount) < 0)
                {
                    throw new InsufficientBalanceException("Insufficient balance");
                }
                balance.setAmount(balance.getAmount().subtract(amount));
            }
        }
    }


    @Transactional
    public TransactionResponse createTransaction(@NonNull CreateTransactionRequest request, String email)
    {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        Balance balance = balanceRepository.findByAccountUserId(account.getUserId())
                .orElseThrow(() -> new BalanceNotFoundException("Balance not found for account"));

        updateBalance(balance, request.getAmount(), request.getIndicator());

        Transaction transaction = transactionMapper.toEntity(request);

        transaction.setAccount(account);

        transaction.setDate(Instant.now());

        Transaction saved = transactionRepository.save(transaction);

        balanceRepository.save(balance);

        return transactionMapper.toResponse(saved);
    }

    public List<TransactionResponse> getTransactions(String email)
    {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        return transactionRepository
                .findByAccountUserId(account.getUserId())
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }
}