package com.redmath.admin.service;

import com.redmath.account.dto.AccountResponse;
import com.redmath.account.entity.Account;
import com.redmath.account.exception.UserNotFoundException;
import com.redmath.account.mapper.AccountMapper;
import com.redmath.account.repository.AccountRepository;
import com.redmath.admin.dto.CreateUserRequest;
import com.redmath.admin.dto.UpdateUserRequest;
import com.redmath.authentication.exception.EmailAlreadyExistsException;
import com.redmath.balance.entity.Balance;
import com.redmath.transactions.entity.Indicator;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Slf4j
@Service
@PreAuthorize("hasRole('ADMIN')")
public class AdminService {

    private final AccountRepository userRepository;
    private final AccountMapper accountMapper;

    public AdminService(AccountRepository userRepository,
                        AccountMapper accountMapper) {
        this.userRepository = userRepository;
        this.accountMapper = accountMapper;
    }

    @Transactional
    public AccountResponse createUser(@NonNull CreateUserRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Unable to register with the provided details");
        }

        Account user = accountMapper.toEntity(request);

        Balance balance = new Balance();
        balance.setAmount(BigDecimal.ZERO);
        balance.setDate(Instant.now());
        balance.setIndicator(Indicator.CR);

        balance.setAccount(user);
        user.setBalance(balance);

        Account savedUser = userRepository.save(user);
        log.info("New account created. userId={}, email={}, role={}",
                savedUser.getUserId(),savedUser.getEmail(), savedUser.getRole());

        return accountMapper.toResponse(savedUser);
    }

    public Page<AccountResponse> getAllUsers(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return userRepository.findAll(pageable)
                .map(accountMapper::toResponse);
    }

    public AccountResponse getUserById(Long id)
    {

        Account user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User with id " + id + " not found."));

        return accountMapper.toResponse(user);
    }

    public AccountResponse updateUser(Long id, @NonNull UpdateUserRequest request) {

        Account user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User with id " + id + " not found."));

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }

        if (request.address() != null && !request.address().isBlank()) {
            user.setAddress(request.address());
        }
        user.setUpdatedAt(Instant.now());
        Account updatedUser = userRepository.save(user);

        log.info("User {} updated by admin", updatedUser.getEmail());
        return accountMapper.toResponse(updatedUser);
    }

    public void deleteUser(Long id) {

        Account user = userRepository.findById(id)
                .orElseThrow(() ->
                        new UserNotFoundException("User with id " + id + " not found."));

        userRepository.delete(user);

        log.info("User {} deleted by admin.", user.getEmail());
    }
}
