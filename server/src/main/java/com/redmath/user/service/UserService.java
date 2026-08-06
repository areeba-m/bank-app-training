package com.redmath.user.service;

import com.redmath.account.Account;
import com.redmath.account.AccountRepository;
import com.redmath.admin.dto.UserResponse;
import com.redmath.admin.mapper.UserMapper;
import com.redmath.transactions.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AccountRepository userRepository;
    private final UserMapper userMapper;

    public UserResponse getCurrentUser(String email)
    {
        Account account = userRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("User not found"));

        return userMapper.toResponse(account);
    }
}