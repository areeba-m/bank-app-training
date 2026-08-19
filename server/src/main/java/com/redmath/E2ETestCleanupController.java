package com.redmath;

import com.redmath.account.entity.Role;
import com.redmath.account.repository.AccountRepository;
import com.redmath.balance.repository.BalanceRepository;
import com.redmath.categorization.repository.TransactionCategoryRepository;
import com.redmath.transactions.repository.TransactionRepository;
import com.redmath.transfer.repository.TransferRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@Profile("e2e")
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class E2ETestCleanupController {

    private final TransactionCategoryRepository transactionCategoryRepository;
    private final TransactionRepository transactionRepository;
    private final BalanceRepository balanceRepository;
    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;

    @Transactional
    @DeleteMapping("/cleanup")
    public ResponseEntity<Void> cleanup() {

        transactionCategoryRepository.deleteAll();
        transactionRepository.deleteAll();
        transferRepository.deleteAll();
        balanceRepository.deleteAll();
        accountRepository.deleteByRole(Role.USER);

        return ResponseEntity.noContent().build();
    }
}