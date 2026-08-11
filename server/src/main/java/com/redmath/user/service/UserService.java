package com.redmath.user.service;

import com.redmath.account.dto.AccountResponse;
import com.redmath.account.entity.Account;
import com.redmath.account.exception.UserNotFoundException;
import com.redmath.account.mapper.AccountMapper;
import com.redmath.account.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AccountRepository userRepository;
    private final AccountMapper accountMapper;

    public AccountResponse getCurrentUser(String email)
    {
        Account account = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return accountMapper.toResponse(account);
    }
}