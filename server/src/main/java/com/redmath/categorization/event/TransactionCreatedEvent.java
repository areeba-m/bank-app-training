package com.redmath.categorization.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class TransactionCreatedEvent extends ApplicationEvent {

    private final Long transactionId;

    public TransactionCreatedEvent(Object source, Long transactionId) {
        super(source);
        this.transactionId = transactionId;
    }
}
