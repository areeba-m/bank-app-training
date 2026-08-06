package com.redmath.user.controller;

import com.redmath.account.Account;
import com.redmath.admin.dto.UserResponse;
import com.redmath.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public UserResponse getMyProfile(@NonNull Authentication authentication)
    {
        String email = authentication.getName();
        return userService.getCurrentUser(email);
    }
}
