//package com.redmath.user;
//
//import com.redmath.balance.dto.BalanceResponse;
//import com.redmath.transactions.Indicator;
//import com.redmath.balance.Balance;
//import com.redmath.balance.repository.BalanceRepository;
//import com.redmath.balance.service.BalanceService;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//
//@ExtendWith(MockitoExtension.class)
//class BalanceServiceTest {
//    @Mock
//    private BalanceRepository balanceRepository;
//    @InjectMocks
//    private BalanceService balanceService;
//    @Test
//    void shouldReturnBalanceSuccessfully() {
//
//
//        Long accountId = 1L;
//        Balance balance = new Balance();
//
//        balance.setDate(Instant.now());
//        balance.setAmount(BigDecimal.ZERO);
//        balance.setIndicator(Indicator.CR);
//
//        when(balanceRepository.findByAccountUserId(accountId))
//                .thenReturn(Optional.of(balance));
//
//        BalanceResponse response =
//                balanceService.getBalance(accountId);
//
//        assertNotNull(response);
//
//        assertEquals(
//                BigDecimal.ZERO,
//                response.getAmount()
//        );
//
//
//        assertEquals(
//                "CR",
//                response.getIndicator()
//        );
//
//
//        verify(balanceRepository)
//                .findByAccountUserId(accountId);
//
//    }
//
//
//
//    @Test
//    void shouldThrowExceptionWhenBalanceDoesNotExist() {
//
//
//        Long accountId = 1L;
//
//
//        when(balanceRepository.findByAccountUserId(accountId))
//                .thenReturn(Optional.empty());
//
//        RuntimeException exception =
//                assertThrows(
//                        RuntimeException.class,
//                        () -> balanceService.getBalance(accountId)
//                );
//
//
//        assertEquals(
//                "Balance not found for user 1",
//                exception.getMessage()
//        );
//    }
//}