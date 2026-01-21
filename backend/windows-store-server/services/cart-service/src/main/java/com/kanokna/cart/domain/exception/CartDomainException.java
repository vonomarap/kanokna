package com.kanokna.cart.domain.exception;

import com.kanokna.shared.core.DomainException;

/**
 * MODULE_CONTRACT id="MC-cart-domain-errors"
 * LAYER="domain.exception"
 * INTENT="Base exception for all cart domain errors; provides structured error codes"
 * LINKS="RequirementsAnalysis.xml#UC-CART-MANAGE"
 *
 * Base exception for cart domain errors.
 * All cart-specific exceptions extend this class to provide
 * consistent error handling and structured error codes.
 */
public class CartDomainException extends DomainException {

    private static final long serialVersionUID = 1L;


    /**
     * Creates a CartDomainException with error code and message.
     *
     * @param errorCode structured error code (e.g., ERR-CART-NOT-FOUND)
     * @param message   human-readable error message
     */
    public CartDomainException(String errorCode, String message) {
        super(errorCode, message);
    }

    /**
     * Creates a CartDomainException with error code, message, and cause.
     *
     * @param errorCode structured error code
     * @param message   human-readable error message
     * @param cause     underlying cause
     */
    public CartDomainException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    /**
     * Returns the structured error code.
     *
     * @return error code string
     */
    public String getErrorCode() {
        return getCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{errorCode='" + getCode() + "', message='" + getMessage() + "'}";
    }
}
