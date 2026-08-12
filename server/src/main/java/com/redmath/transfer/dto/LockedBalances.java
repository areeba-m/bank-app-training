package com.redmath.transfer.dto;

import com.redmath.balance.entity.Balance;

public record LockedBalances(
        Balance sender,
        Balance receiver
) {}