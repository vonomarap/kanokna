package com.kanokna.cart.domain.exception;

import com.kanokna.cart.domain.model.CartStatus;

/**
 * Exception thrown when cart operation is not allowed in current state.
 */
public class CartStateException extends CartDomainException {

    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "ERR-CART-INVALID-STATE";

    private final CartStatus currentStatus;
    private final String attemptedOperation;

    public CartStateException(CartStatus currentStatus, String attemptedOperation) {
        super(ERROR_CODE, buildMessage(currentStatus, attemptedOperation));
        this.currentStatus = currentStatus;
        this.attemptedOperation = attemptedOperation;
    }

    private static String buildMessage(CartStatus currentStatus, String attemptedOperation) {
        return String.format("Cannot perform '%s' on cart in status %s", attemptedOperation, currentStatus);
    }

    public CartStatus getCurrentStatus() {
        return currentStatus;
    }

    public String getAttemptedOperation() {
        return attemptedOperation;
    }
}
