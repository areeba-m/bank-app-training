package com.redmath.balance.service;

import com.redmath.account.entity.Account;
import com.redmath.account.repository.AccountRepository;
import com.redmath.balance.entity.Balance;
import com.redmath.balance.dto.BalanceResponse;
import com.redmath.balance.exception.BalanceNotFoundException;
import com.redmath.transactions.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final AccountRepository accountRepository;

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public BalanceResponse getBalance(String email) {

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("Account not found for email: "));

        Balance balance = account.getBalance();
        if (balance == null)
        {
            throw new BalanceNotFoundException("Balance not found for account");
        }

        return BalanceResponse.builder()
                .date(balance.getDate())
                .amount(balance.getAmount())
                .indicator(balance.getIndicator().name())
                .build();
    }
}