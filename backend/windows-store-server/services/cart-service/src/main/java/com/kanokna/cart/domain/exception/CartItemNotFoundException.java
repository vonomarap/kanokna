package com.kanokna.cart.domain.exception;

/**
 * Exception thrown when a cart item cannot be found.
 */
public class CartItemNotFoundException extends CartDomainException {

    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "ERR-CART-ITEM-NOT-FOUND";

    private final String itemId;

    public CartItemNotFoundException(String itemId) {
        super(ERROR_CODE, "Cart item not found: " + itemId);
        this.itemId = itemId;
    }

    public String getItemId() {
        return itemId;
    }
}
