package com.kanokna.cart.domain.exception;

/**
 * Exception thrown when cart merge operations fail.
 */
public class CartMergeException extends CartDomainException {

    private static final long serialVersionUID = 1L;
    private static final String ERROR_CODE = "ERR-CART-MERGE-FAILED";

    private final String sourceCartId;
    private final String targetCartId;

    public CartMergeException(String message) {
        super(ERROR_CODE, message);
        this.sourceCartId = null;
        this.targetCartId = null;
    }

    public CartMergeException(String sourceCartId, String targetCartId, String message) {
        super(ERROR_CODE, message);
        this.sourceCartId = sourceCartId;
        this.targetCartId = targetCartId;
    }

    public CartMergeException(String message, Throwable cause) {
        super(ERROR_CODE, message, cause);
        this.sourceCartId = null;
        this.targetCartId = null;
    }

    public static CartMergeException anonymousCartNotFound(String sessionId) {
        return new CartMergeException(
            null,
            null,
            "Anonymous cart not found for session: " + sessionId
        );
    }

    public static CartMergeException sameCartMerge(String cartId) {
        return new CartMergeException(
            cartId,
            cartId,
            "Cannot merge cart into itself: " + cartId
        );
    }

    public String getSourceCartId() {
        return sourceCartId;
    }

    public String getTargetCartId() {
        return targetCartId;
    }
}
