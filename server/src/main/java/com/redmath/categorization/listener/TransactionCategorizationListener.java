package com.redmath.categorization.listener;

import com.redmath.transfer.transactions.event.TransactionCreatedEvent;
import com.redmath.categorization.service.CategorizationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Reacts to transaction creation by categorizing the new transaction. Kept as a
 * plain (synchronous) listener per the assignment's stated tolerance for a simpler
 * synchronous flow; swapping to {@code @Async} later is a one-annotation change if
 * this needs to stop blocking the request thread.
 */
@Component
@RequiredArgsConstructor
public class TransactionCategorizationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionCategorizationListener.class);

    private final CategorizationService categorizationService;

    @Async  // ADD THIS LINE
    @TransactionalEventListener
    public void onTransactionCreated(TransactionCreatedEvent event) {
        try {
            categorizationService.categorizeIfNeeded(event.getTransactionId());
        } catch (RuntimeException ex) {
            // Categorization must never break transaction creation - log and move on.
            LOGGER.warn("Failed to auto-categorize transaction {}", event.getTransactionId(), ex);
        }
    }
}
