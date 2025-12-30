package com.kanokna.shared.core;

import java.util.Objects;

/**
 * Runtime exception for domain-level errors when exception-based error handling is required.
 * <p>
 * This exception wraps a {@link DomainError} to provide both the exception mechanism
 * for exceptional cases and the structured error information for logging/handling.
 * <p>
 * Use {@link Result} for expected business failures; use this exception for truly
 * exceptional conditions or when integrating with frameworks that require exceptions.
 *
 * @see DomainError
 * @see Result
 */
public class DomainException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final DomainError error;

    /**
     * Creates a DomainException wrapping the given DomainError.
     *
     * @param error the domain error (must not be null)
     * @throws NullPointerException if error is null
     */
    public DomainException(DomainError error) {
        super(Objects.requireNonNull(error, "DomainError cannot be null").message());
        this.error = error;
    }

    /**
     * Creates a DomainException with code and message.
     *
     * @param code    the error code
     * @param message the error message
     */
    public DomainException(String code, String message) {
        this(DomainError.of(code, message));
    }

    /**
     * Creates a DomainException with code, message, and cause.
     *
     * @param code    the error code
     * @param message the error message
     * @param cause   the underlying cause
     */
    public DomainException(String code, String message, Throwable cause) {
        super(message, cause);
        this.error = DomainError.of(code, message);
    }

    /**
     * Returns the underlying domain error.
     *
     * @return the domain error
     */
    public DomainError getError() {
        return error;
    }

    /**
     * Returns the error code.
     *
     * @return the error code
     */
    public String getCode() {
        return error.code();
    }

    @Override
    public String toString() {
        return "DomainException{" + error.toLogString() + "}";
    }
}
