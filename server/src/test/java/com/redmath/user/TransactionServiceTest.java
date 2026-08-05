package com.redmath.user;

import com.redmath.account.Account;
import com.redmath.account.AccountRepository;
import com.redmath.user.dto.TransactionResponse;
import com.redmath.user.entity.Indicator;
import com.redmath.user.entity.TransactionEntity;
import com.redmath.user.mapper.TransactionMapper;
import com.redmath.user.repository.TransactionRepository;
import com.redmath.user.repository.balanceRepository;
import com.redmath.user.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {


    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void shouldReturnTransactionsWhenAccountExists() {

        Long accountId = 1L;

        Account account = new Account();
        account.setUserId(accountId);

        TransactionEntity transaction =
                new TransactionEntity();

        transaction.setDate(
                Instant.now()
        );

        transaction.setDescription(
                "Salary"
        );

        transaction.setAmount(
                BigDecimal.valueOf(5000)
        );

        transaction.setIndicator(
                Indicator.CR
        );

        TransactionResponse response =
                new TransactionResponse(
                        transaction.getDate(),
                        transaction.getDescription(),
                        transaction.getAmount(),
                        transaction.getIndicator().name()
                );

        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        when(transactionRepository
                .findByAccountUserId(accountId))
                .thenReturn(List.of(transaction));

        when(transactionMapper.toResponse(transaction))
                .thenReturn(response);

        List<TransactionResponse> result =
                transactionService.getTransactions(accountId);

        assertNotNull(result);
        assertEquals(
                1,
                result.size()
        );
        assertEquals(
                "Salary",
                result.getFirst().getDescription()
        );
        verify(accountRepository).findById(accountId);
        verify(transactionRepository).findByAccountUserId(accountId);

    }

    @Test
    void shouldReturnEmptyListWhenAccountHasNoTransactions() {

        Long accountId = 1L;
        Account account = new Account();
        account.setUserId(accountId);
        when(accountRepository.findById(accountId))
                .thenReturn(Optional.of(account));

        when(transactionRepository
                .findByAccountUserId(accountId))
                .thenReturn(List.of());

        List<TransactionResponse> result = transactionService.getTransactions(accountId);

        assertNotNull(result);
        assertTrue(
                result.isEmpty()
        );
        verify(transactionRepository)
                .findByAccountUserId(accountId);
    }





    @Test
    void shouldThrowExceptionWhenAccountDoesNotExist() {

        Long accountId = 2L;
        when(accountRepository.findById(accountId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                        RuntimeException.class,
                        () ->
                                transactionService
                                        .getTransactions(accountId)
        );

        assertEquals(
                "Account with id 2 not found",
                exception.getMessage()
        );

        verify(transactionRepository, never())
                .findByAccountUserId(accountId);
    }

}