package com.kanokna.cart.domain.exception;

/**
 * Exception thrown when a cart cannot be found.
 */
public class CartNotFoundException extends CartDomainException {

    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "ERR-CART-NOT-FOUND";

    public CartNotFoundException(String message) {
        super(ERROR_CODE, message);
    }

    public CartNotFoundException(String customerId, String sessionId) {
        super(ERROR_CODE, buildMessage(customerId, sessionId));
    }

    private static String buildMessage(String customerId, String sessionId) {
        if (customerId != null && !customerId.isBlank()) {
            return "Cart not found for customerId=" + customerId;
        }
        if (sessionId != null && !sessionId.isBlank()) {
            return "Cart not found for sessionId=" + sessionId;
        }
        return "Cart not found";
    }
}
