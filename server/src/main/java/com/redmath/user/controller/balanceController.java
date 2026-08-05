package com.redmath.user.controller;


import com.redmath.user.dto.BalanceResponse;
import com.redmath.user.service.balanceService;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class balanceController {

    private final balanceService userService;

    @GetMapping("/{id}/balance")
    public BalanceResponse getBalance(
            @PathVariable Long id
    ){

        return userService.getBalance(id);

    }

}