package com.redmath.user.service;

import com.redmath.user.dto.BalanceResponse;
import com.redmath.user.entity.balanceEntity;
import com.redmath.user.repository.balanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class balanceService {
    private final balanceRepository balanceRepository;

    public BalanceResponse getBalance(Long accountId) {

        balanceEntity balance = balanceRepository.findByAccountUserId(accountId)
                .orElseThrow(() ->
                        new RuntimeException("Balance not found for user " + accountId));

        return BalanceResponse.builder()
                .date(balance.getDate())
                .amount(balance.getAmount())
                .indicator(balance.getIndicator().name())
                .build();
    }

}