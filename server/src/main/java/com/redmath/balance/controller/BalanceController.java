package com.redmath.balance.controller;

import com.redmath.account.entity.Account;
import com.redmath.account.repository.AccountRepository;
import com.redmath.balance.dto.BalanceResponse;
import com.redmath.balance.service.BalanceService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user/balance")
@RequiredArgsConstructor
public class BalanceController {

    private final BalanceService balanceService;
    private final AccountRepository accountRepository;

    public BalanceResponse getBalance(@NonNull Authentication auth) {

        String email = auth.getName();
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        Long id = account.getUserId();

        return balanceService.getBalance(id);
    }
}