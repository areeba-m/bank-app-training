package com.redmath.admin.service;


import com.redmath.account.Account;
import com.redmath.account.AccountRepository;
import com.redmath.account.exception.UserAlreadyExistsException;
import com.redmath.account.exception.UserNotFoundException;
import com.redmath.admin.dto.CreateUserRequest;
import com.redmath.admin.dto.UpdateUserRequest;
import com.redmath.admin.dto.UserResponse;
import com.redmath.admin.mapper.UserMapper;
import com.redmath.user.entity.Indicator;
import com.redmath.user.entity.balanceEntity;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class AdminService {

    private final AccountRepository userRepository;
    private final UserMapper userMapper;

    public AdminService(AccountRepository userRepository,
                        UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponse createUser(@NonNull CreateUserRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "User with email '" + request.getEmail() + "' already exists.");
        }

        Account user = userMapper.toEntity(request);

        balanceEntity balance = new balanceEntity();
        balance.setAmount(BigDecimal.ZERO);
        balance.setDate(Instant.now());
        balance.setIndicator(Indicator.CR);

        balance.setAccount(user);
        user.setBalance(balance);

        Account savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    public List<UserResponse> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {

        Account user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User with id " + id + " not found."));

        return userMapper.toResponse(user);
    }

    public UserResponse updateUser(Long id, @NonNull UpdateUserRequest request) {

        Account user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User with id " + id + " not found."));

        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }

        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            user.setAddress(request.getAddress());
        }

        Account updatedUser = userRepository.save(user);

        return userMapper.toResponse(updatedUser);
    }

    public void deleteUser(Long id) {

        Account user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User with id " + id + " not found."));

        userRepository.delete(user);
    }
}
