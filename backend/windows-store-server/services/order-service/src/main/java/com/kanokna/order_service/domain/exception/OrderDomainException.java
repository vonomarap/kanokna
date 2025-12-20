package com.kanokna.order_service.domain.exception;

/**
 * Domain-level exception for order invariants and state machine violations.
 */
public class OrderDomainException extends RuntimeException {
    public OrderDomainException(String message) {
        super(message);
    }

    public OrderDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
