package com.kanokna.cart.domain.exception;

/**
 * Exception thrown when checkout/snapshot operations fail.
 */
public class CheckoutException extends CartDomainException {

    private static final long serialVersionUID = 1L;

    public CheckoutException(String errorCode, String message) {
        super(errorCode, message);
    }

    public CheckoutException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public static CheckoutException emptyCart() {
        return new CheckoutException(
            "ERR-CART-EMPTY",
            "Cannot checkout an empty cart"
        );
    }

    public static CheckoutException invalidItems(int invalidCount) {
        return new CheckoutException(
            "ERR-CART-INVALID-ITEMS",
            "Cart contains " + invalidCount + " invalid item(s) that must be resolved before checkout"
        );
    }

    public static CheckoutException anonymousNotAllowed() {
        return new CheckoutException(
            "ERR-CART-ANONYMOUS",
            "Anonymous users must authenticate before checkout"
        );
    }

    public static CheckoutException priceChangeRequiresAcknowledgment(double changePercent) {
        return new CheckoutException(
            "ERR-CART-PRICE-CHANGE",
            String.format("Price changed by %.2f%%; acknowledgment required before checkout", changePercent)
        );
    }

    public static CheckoutException snapshotExpired(String snapshotId) {
        return new CheckoutException(
            "ERR-CART-SNAPSHOT-EXPIRED",
            "Cart snapshot has expired: " + snapshotId
        );
    }
}
