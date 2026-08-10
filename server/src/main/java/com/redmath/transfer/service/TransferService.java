package com.redmath.transfer.service;

import com.redmath.account.entity.Account;
import com.redmath.account.repository.AccountRepository;
import com.redmath.balance.entity.Balance;
import com.redmath.balance.exception.BalanceNotFoundException;
import com.redmath.balance.repository.BalanceRepository;
import com.redmath.transactions.entity.Indicator;
import com.redmath.transactions.entity.Transaction;
import com.redmath.transactions.repository.TransactionRepository;
import com.redmath.transfer.dto.CreateTransferRequest;
import com.redmath.transfer.dto.TransferResponse;
import com.redmath.transfer.entity.Transfer;
import com.redmath.transfer.exception.InsufficientBalanceException;
import com.redmath.transfer.exception.RecipientNotFoundException;
import com.redmath.transfer.exception.SelfTransferException;
import com.redmath.transfer.exception.SenderAccountNotFoundException;
import com.redmath.transfer.mapper.TransferMapper;
import com.redmath.transfer.repository.TransferRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final TransferRepository transferRepository;
    private final AccountRepository accountRepository;
    private final BalanceRepository balanceRepository;
    private final TransactionRepository transactionRepository;
    private final TransferMapper transferMapper;

    @Transactional
    public TransferResponse createTransfer(@NonNull CreateTransferRequest request, String senderEmail) {

        if (senderEmail != null && senderEmail.equalsIgnoreCase(request.getRecipientEmail())) {
            throw new SelfTransferException("Cannot transfer money to your own account");
        }

        Account sender = accountRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new SenderAccountNotFoundException("Sender account not found"));

        Account receiver = accountRepository.findByEmail(request.getRecipientEmail())
                .orElseThrow(() -> new RecipientNotFoundException("Recipient account not found"));

        Balance senderBalance = balanceRepository.findByAccountUserId(sender.getUserId())
                .orElseThrow(() -> new BalanceNotFoundException("Balance not found for sender account"));

        Balance receiverBalance = balanceRepository.findByAccountUserId(receiver.getUserId())
                .orElseThrow(() -> new BalanceNotFoundException("Balance not found for recipient account"));

        BigDecimal amount = request.getAmount();

        if (senderBalance.getAmount().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        senderBalance.setAmount(senderBalance.getAmount().subtract(amount));
        receiverBalance.setAmount(receiverBalance.getAmount().add(amount));

        Instant now = Instant.now();

        String description = (request.getDescription() == null || request.getDescription().isBlank())
                ? "Transfer" : request.getDescription();

        Transfer transfer = transferMapper.toEntity(description, sender, receiver, amount);
        transfer.setDate(now);

        Transfer savedTransfer = transferRepository.save(transfer);

        balanceRepository.save(senderBalance);
        balanceRepository.save(receiverBalance);

        Transaction debit = Transaction.builder()
                .date(now)
                .description(description + " to " + receiver.getEmail())
                .amount(amount)
                .indicator(Indicator.DB)
                .account(sender)
                .build();

        Transaction credit = Transaction.builder()
                .date(now)
                .description(description + " from " + sender.getEmail())
                .amount(amount)
                .indicator(Indicator.CR)
                .account(receiver)
                .build();

        transactionRepository.save(debit);
        transactionRepository.save(credit);

        return transferMapper.toResponse(savedTransfer);
    }

    public Page<TransferResponse> getTransfers(String email, int page, int size) {

        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new SenderAccountNotFoundException("Account not found"));

        Pageable pageable = PageRequest.of(page, size);

        return transferRepository
                .findAllByAccountUserId(account.getUserId(), pageable)
                .map(transferMapper::toResponse);
    }
}
