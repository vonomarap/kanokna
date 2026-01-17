package com.kanokna.cart.domain.exception;

/**
 * Exception thrown when an invalid quantity is provided.
 */
public class InvalidQuantityException extends CartDomainException {

    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "ERR-CART-INVALID-QUANTITY";

    private final int quantity;
    private final int minQuantity;
    private final int maxQuantity;

    public InvalidQuantityException(int quantity) {
        super(ERROR_CODE, "Quantity must be >= 1: " + quantity);
        this.quantity = quantity;
        this.minQuantity = 1;
        this.maxQuantity = Integer.MAX_VALUE;
    }

    public InvalidQuantityException(int quantity, int minQuantity, int maxQuantity) {
        super(ERROR_CODE, String.format("Quantity %d out of valid range [%d, %d]", quantity, minQuantity, maxQuantity));
        this.quantity = quantity;
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getMinQuantity() {
        return minQuantity;
    }

    public int getMaxQuantity() {
        return maxQuantity;
    }
}
