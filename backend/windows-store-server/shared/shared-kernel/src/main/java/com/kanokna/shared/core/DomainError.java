package com.kanokna.shared.core;

import java.util.Map;
import java.util.Objects;

/**
 * Represents a domain-level error with a code, message, and optional context.
 * <p>
 * Used with {@link Result.Failure} to provide explicit error handling
 * without exceptions for expected business failures.
 *
 * @param code    A unique error code (e.g., "ERR-DIM-TOO-SMALL")
 * @param message A human-readable error message
 * @param context Additional key-value pairs providing error context
 * @see Result
 * @see <a href="ArchitecturalRecommendations.xml#PAT-001">PAT-001</a>
 */
public record DomainError(
    String code,
    String message,
    Map<String, Object> context
) {

    /**
     * Canonical constructor with validation.
     */
    public DomainError {
        Objects.requireNonNull(code, "Error code cannot be null");
        Objects.requireNonNull(message, "Error message cannot be null");
        // Defensive copy to ensure immutability
        context = context != null ? Map.copyOf(context) : Map.of();
    }

    /**
     * Creates a DomainError with code and message only (no context).
     *
     * @param code    the error code
     * @param message the error message
     */
    public DomainError(String code, String message) {
        this(code, message, Map.of());
    }

    /**
     * Factory method to create a DomainError with code and message.
     *
     * @param code    the error code
     * @param message the error message
     * @return a new DomainError instance
     */
    public static DomainError of(String code, String message) {
        return new DomainError(code, message);
    }

    /**
     * Factory method to create a DomainError with a single context entry.
     *
     * @param code    the error code
     * @param message the error message
     * @param key     the context key
     * @param value   the context value
     * @return a new DomainError instance
     */
    public static DomainError of(String code, String message, String key, Object value) {
        return new DomainError(code, message, Map.of(key, value));
    }

    /**
     * Factory method to create a DomainError with full context.
     *
     * @param code    the error code
     * @param message the error message
     * @param context the error context map
     * @return a new DomainError instance
     */
    public static DomainError of(String code, String message, Map<String, Object> context) {
        return new DomainError(code, message, context);
    }

    /**
     * Returns a formatted string representation suitable for logging.
     *
     * @return formatted error string
     */
    public String toLogString() {
        if (context.isEmpty()) {
            return "[%s] %s".formatted(code, message);
        }
        return "[%s] %s context=%s".formatted(code, message, context);
    }
}
