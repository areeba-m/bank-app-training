package com.redmath.transactions.service;

import com.redmath.account.entity.Account;
import com.redmath.account.exception.UserNotFoundException;
import com.redmath.account.repository.AccountRepository;
import com.redmath.balance.entity.Balance;
import com.redmath.balance.exception.BalanceNotFoundException;
import com.redmath.balance.repository.BalanceRepository;
import com.redmath.categorization.event.TransactionCreatedEvent;
import com.redmath.transactions.dto.CreateTransactionRequest;
import com.redmath.transactions.dto.TransactionResponse;
import com.redmath.transactions.entity.Indicator;
import com.redmath.transactions.entity.Transaction;
import com.redmath.transactions.exception.InsufficientBalanceException;
import com.redmath.transactions.mapper.TransactionMapper;
import com.redmath.transactions.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService
{
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;
    private final BalanceRepository balanceRepository;
    private final ApplicationEventPublisher applicationEventPublisher;


    @PreAuthorize("hasRole('USER')")
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
    @PreAuthorize("hasRole('USER')")
    public TransactionResponse createTransaction(@NonNull CreateTransactionRequest request,
                                                 String email, String idempotencyKey)
    {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("Account not found"));

        Optional<Transaction> existingTransaction =
                transactionRepository.findByAccountAndIdempotencyKey(account, idempotencyKey);
        if (existingTransaction.isPresent()) {
            return transactionMapper.toResponse(existingTransaction.get());
        }

        Balance balance = balanceRepository.findByAccountUserIdForUpdate(account.getUserId())
                .orElseThrow(() -> new BalanceNotFoundException("Balance not found for account"));

        updateBalance(balance, request.getAmount(), request.getIndicator());

        Transaction transaction = transactionMapper.toEntity(request);
        transaction.setAccount(account);
        transaction.setDate(Instant.now());
        transaction.setIdempotencyKey(idempotencyKey);

        Transaction saved = transactionRepository.save(transaction);

        log.info("User made a transaction. userId={}, transaction_id={}", account.getUserId(), saved.getId());

        if(request.getIndicator()==Indicator.DB)
        {
            applicationEventPublisher.publishEvent(new TransactionCreatedEvent(this, saved.getId()));
        }

        return transactionMapper.toResponse(saved);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Page<TransactionResponse> getTransactions(Long userId, int page, int size) {

        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Account not found"));

        Pageable pageable = PageRequest.of(page, size);

        return transactionRepository
                .findByAccountUserId(account.getUserId(), pageable)
                .map(transactionMapper::toResponse);
    }
}