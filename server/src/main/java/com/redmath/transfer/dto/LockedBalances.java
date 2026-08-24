package com.redmath.transfer.dto;

import com.redmath.account.balance.entity.Balance;

public record LockedBalances(
        Balance sender,
        Balance receiver
) {}