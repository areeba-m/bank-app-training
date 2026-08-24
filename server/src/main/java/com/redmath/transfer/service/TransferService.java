package com.redmath.transfer.service;

import com.redmath.account.entity.Account;
import com.redmath.account.repository.AccountRepository;
import com.redmath.account.balance.entity.Balance;
import com.redmath.account.balance.exception.BalanceNotFoundException;
import com.redmath.account.balance.repository.BalanceRepository;
import com.redmath.transfer.transactions.event.TransactionCreatedEvent;
import com.redmath.enums.Indicator;
import com.redmath.transfer.transactions.entity.Transaction;
import com.redmath.transfer.transactions.exception.InsufficientBalanceException;
import com.redmath.transfer.transactions.repository.TransactionRepository;
import com.redmath.transfer.dto.CreateTransferRequest;
import com.redmath.transfer.dto.LockedBalances;
import com.redmath.transfer.dto.TransferResponse;
import com.redmath.transfer.entity.Transfer;
import com.redmath.transfer.exception.RecipientNotFoundException;
import com.redmath.transfer.exception.SelfTransferException;
import com.redmath.transfer.exception.SenderAccountNotFoundException;
import com.redmath.transfer.mapper.TransferMapper;
import com.redmath.transfer.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final BalanceRepository balanceRepository;
    private final TransactionRepository transactionRepository;
    private final TransferMapper transferMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<TransferResponse> getTransfers(String email, int page, int size) {

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new SenderAccountNotFoundException("Account not found"));

        Pageable pageable = PageRequest.of(page, size);

        return transferRepository
                .findAllByAccountUserId(account.getUserId(), pageable)
                .map(transferMapper::toResponse);
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    @PreAuthorize("hasRole('USER')")
    public TransferResponse createTransfer(@NonNull CreateTransferRequest request,
                                           String senderEmail, String idempotencyKey) {
        validateTransferRequest(request, senderEmail);

        Account sender = findSender(senderEmail);
        Optional<Transfer> existingTransfer = transferRepository.findBySenderAccountAndIdempotencyKey(sender, idempotencyKey);
        if (existingTransfer.isPresent()) {
            return transferMapper.toResponse(existingTransfer.get());
        }

        Account receiver = findReceiver(request.getRecipientEmail());

        LockedBalances balances = lockBalancesConcurrently(sender, receiver);

        validateSufficientBalance(balances.sender(), request.getAmount());
        updateBalances(balances.sender(), balances.receiver(), request.getAmount());

        Instant now = Instant.now();
        String description = resolveDescription(request.getDescription());

        Transfer transfer = createTransferEntity(sender, receiver, request.getAmount(), description, now, idempotencyKey);

        createTransactions(sender, receiver, transfer, request.getAmount(), now, idempotencyKey, description);

        log.info("User made a transfer. senderId={}, receiverId={}, amount={}",
                sender.getUserId(), receiver.getUserId(), request.getAmount());

        return transferMapper.toResponse(transfer);
    }

    private void validateTransferRequest(CreateTransferRequest request, String senderEmail) {
        if (senderEmail != null && senderEmail.equalsIgnoreCase(request.getRecipientEmail())) {
            throw new SelfTransferException("Cannot transfer money to your own account");
        }
    }

    private Account findSender(String senderEmail) {
        return accountRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new SenderAccountNotFoundException("Sender account not found"));
    }

    private Account findReceiver(String recipientEmail) {
        return accountRepository.findByEmail(recipientEmail)
                .orElseThrow(() -> new RecipientNotFoundException("Recipient account not found"));
    }

    private LockedBalances lockBalancesConcurrently(Account sender, Account receiver) {
        Long senderId = sender.getUserId();
        Long receiverId = receiver.getUserId();

        if (senderId < receiverId) {
            Balance senderBalance = findBalanceForUpdate(senderId);
            Balance receiverBalance = findBalanceForUpdate(receiverId);

            return new LockedBalances(senderBalance, receiverBalance);
        }

        Balance receiverBalance = findBalanceForUpdate(receiverId);
        Balance senderBalance = findBalanceForUpdate(senderId);

        return new LockedBalances(senderBalance, receiverBalance);
    }

    private Balance findBalanceForUpdate(Long userId) {
        return balanceRepository.findByAccountUserIdForUpdate(userId)
                .orElseThrow(() -> new BalanceNotFoundException("Balance not found for account: " + userId));
    }

    private void validateSufficientBalance(Balance senderBalance, BigDecimal amount) {
        if (senderBalance.getAmount().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }
    }

    private void updateBalances(Balance senderBalance, Balance receiverBalance, BigDecimal amount) {
        senderBalance.setAmount(senderBalance.getAmount().subtract(amount));
        receiverBalance.setAmount(receiverBalance.getAmount().add(amount));
    }

    private Transfer createTransferEntity(Account sender, Account receiver, BigDecimal amount,
                                          String description, Instant date, String idempotencyKey) {
        Transfer transfer = transferMapper.toEntity(description, sender, receiver, amount);
        transfer.setDate(date);
        transfer.setIdempotencyKey(idempotencyKey);

        return transferRepository.save(transfer);
    }

    private void createTransactions(Account sender, Account receiver, Transfer transfer,
                                    BigDecimal amount, Instant date, String idempotencyKey, String description) {
        Transaction debit = Transaction.builder()
                .date(date)
                .description(description)
                .amount(amount)
                .indicator(Indicator.DB)
                .account(sender)
                .counterpartyAccount(receiver)
                .counterpartyName(receiver.getName())
                .counterpartyEmail(receiver.getEmail())
                .idempotencyKey(idempotencyKey)
                .transfer(transfer)
                .build();

        Transaction credit = Transaction.builder()
                .date(date)
                .description(description)
                .amount(amount)
                .indicator(Indicator.CR)
                .account(receiver)
                .counterpartyAccount(sender)
                .counterpartyName(sender.getName())
                .counterpartyEmail(sender.getEmail())
                .idempotencyKey(idempotencyKey)
                .transfer(transfer)
                .build();

        Transaction savedDebit = transactionRepository.save(debit);
        transactionRepository.save(credit);

        applicationEventPublisher.publishEvent(
                new TransactionCreatedEvent(this, savedDebit.getId())
        );
    }

    private String resolveDescription(String description) {
        return description == null || description.isBlank() ? "Transfer" : description;
    }
}
