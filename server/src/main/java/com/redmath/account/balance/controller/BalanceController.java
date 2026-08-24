package com.redmath.account.balance.controller;

import com.redmath.account.entity.Account;
import com.redmath.account.repository.AccountRepository;
import com.redmath.account.balance.dto.BalanceResponse;
import com.redmath.account.balance.service.BalanceService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user/balance")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;
    private final AccountRepository accountRepository;

    @GetMapping
    public BalanceResponse getBalance(@NonNull Authentication auth) {

        String email = auth.getName();
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        Long id = account.getUserId();

        return balanceService.getBalance(id);
    }
}