package com.kanokna.shared.core;

import java.util.Objects;
import java.util.function.Function;

/* <FUNCTION_CONTRACT id="FC-shared-kernel-core-Result"
     LAYER="domain.core"
     INTENT="Represent success or failure of domain operations without exceptions"
     INPUT="T value (for Success) or DomainError (for Failure)"
     OUTPUT="Result<T>"
     SIDE_EFFECTS="None (immutable)"
     LINKS="ArchitecturalRecommendations.xml#PAT-001">
   <DEFINITION>
     sealed interface Result<T> permits Result.Success, Result.Failure
   </DEFINITION>

   <METHODS>
     <Method name="isSuccess">Returns true if Success</Method>
     <Method name="isFailure">Returns true if Failure</Method>
     <Method name="getValue">Returns value if Success, throws if Failure</Method>
     <Method name="getError">Returns error if Failure, throws if Success</Method>
     <Method name="getOrElse">Returns value if Success, default if Failure</Method>
     <Method name="map">Transform value if Success, propagate Failure</Method>
     <Method name="flatMap">Chain operations that return Result</Method>
   </METHODS>

   <INVARIANTS>
     <Item>Success contains non-null value</Item>
     <Item>Failure contains non-null DomainError</Item>
     <Item>Result is immutable</Item>
   </INVARIANTS>

   <TESTS>
     <Case id="TC-RESULT-001">Success.isSuccess returns true</Case>
     <Case id="TC-RESULT-002">Failure.isFailure returns true</Case>
     <Case id="TC-RESULT-003">map transforms Success value</Case>
     <Case id="TC-RESULT-004">map propagates Failure</Case>
     <Case id="TC-RESULT-005">flatMap chains operations</Case>
     <Case id="TC-RESULT-006">getOrElse returns default on Failure</Case>
   </TESTS>
 </FUNCTION_CONTRACT> */

/**
 * A sealed type representing the result of a domain operation that can either
 * succeed with a value or fail with a {@link DomainError}.
 * <p>
 * This pattern provides explicit error handling without exceptions for expected
 * business failures, enabling functional composition and forcing callers to handle
 * both success and failure cases.
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * Result<Order> result = orderService.createOrder(request);
 *
 * // Pattern matching (Java 21+)
 * return switch (result) {
 *     case Result.Success<Order>(var order) -> ResponseEntity.ok(order);
 *     case Result.Failure<Order>(var error) -> ResponseEntity.badRequest().body(error);
 * };
 *
 * // Functional style
 * result.map(Order::getId)
 *       .flatMap(id -> orderService.findById(id))
 *       .getOrElse(Order.empty());
 * }</pre>
 *
 * @param <T> the type of the success value
 * @see DomainError
 * @see DomainException
 */
public sealed interface Result<T> permits Result.Success, Result.Failure {

    /**
     * Returns {@code true} if this is a Success result.
     *
     * @return true if success, false otherwise
     */
    boolean isSuccess();

    /**
     * Returns {@code true} if this is a Failure result.
     *
     * @return true if failure, false otherwise
     */
    boolean isFailure();

    /**
     * Returns the success value or throws a {@link DomainException} if this is a Failure.
     *
     * @return the success value
     * @throws DomainException if this is a Failure
     */
    T getOrThrow();

    /**
     * Returns the success value or the provided default if this is a Failure.
     *
     * @param defaultValue the value to return if this is a Failure
     * @return the success value or the default value
     */
    T getOrElse(T defaultValue);

    /**
     * Returns the error if this is a Failure.
     *
     * @return the domain error
     * @throws IllegalStateException if this is a Success
     */
    DomainError getError();

    /**
     * Transforms the success value using the provided mapper function.
     * If this is a Failure, the failure is propagated unchanged.
     *
     * @param mapper the transformation function
     * @param <U>    the type of the transformed value
     * @return a new Result with the transformed value, or the same Failure
     */
    <U> Result<U> map(Function<T, U> mapper);

    /**
     * Chains another Result-returning operation.
     * If this is a Success, applies the mapper and returns its result.
     * If this is a Failure, the failure is propagated unchanged.
     *
     * @param mapper the function that returns a Result
     * @param <U>    the type of the new Result's value
     * @return the result of the mapper, or the propagated Failure
     */
    <U> Result<U> flatMap(Function<T, Result<U>> mapper);

    // ─────────────────────────────────────────────────────────────────────────────
    // Factory Methods
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Creates a Success result with the given value.
     * <!-- BLOCK_ANCHOR id="BA-SK-RESULT-01" purpose="Create Success result" -->
     *
     * @param value the success value (must not be null)
     * @param <T>   the type of the value
     * @return a Success result
     * @throws NullPointerException if value is null
     */
    static <T> Result<T> success(T value) {
        // BA-SK-RESULT-01: Create Success result
        return new Success<>(value);
    }

    /**
     * Creates a Failure result with the given error.
     * <!-- BLOCK_ANCHOR id="BA-SK-RESULT-02" purpose="Create Failure result" -->
     *
     * @param error the domain error (must not be null)
     * @param <T>   the type parameter (unused but required for type inference)
     * @return a Failure result
     * @throws NullPointerException if error is null
     */
    static <T> Result<T> failure(DomainError error) {
        // BA-SK-RESULT-02: Create Failure result
        return new Failure<>(error);
    }

    /**
     * Creates a Failure result with the given error code and message.
     *
     * @param code    the error code
     * @param message the error message
     * @param <T>     the type parameter
     * @return a Failure result
     */
    static <T> Result<T> failure(String code, String message) {
        return new Failure<>(DomainError.of(code, message));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Success Implementation
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Represents a successful result containing a value.
     *
     * @param value the success value (never null)
     * @param <T>   the type of the value
     */
    record Success<T>(T value) implements Result<T> {

        /**
         * Canonical constructor ensuring value is non-null.
         */
        public Success {
            Objects.requireNonNull(value, "Success value cannot be null");
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public boolean isFailure() {
            return false;
        }

        @Override
        public T getOrThrow() {
            return value;
        }

        @Override
        public T getOrElse(T defaultValue) {
            return value;
        }

        @Override
        public DomainError getError() {
            throw new IllegalStateException("Cannot get error from Success result");
        }

        @Override
        public <U> Result<U> map(Function<T, U> mapper) {
            Objects.requireNonNull(mapper, "Mapper function cannot be null");
            return new Success<>(mapper.apply(value));
        }

        @Override
        public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
            Objects.requireNonNull(mapper, "Mapper function cannot be null");
            return mapper.apply(value);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Failure Implementation
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Represents a failed result containing an error.
     *
     * @param error the domain error (never null)
     * @param <T>   the type parameter (for type compatibility)
     */
    record Failure<T>(DomainError error) implements Result<T> {

        /**
         * Canonical constructor ensuring error is non-null.
         */
        public Failure {
            Objects.requireNonNull(error, "Failure error cannot be null");
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public boolean isFailure() {
            return true;
        }

        @Override
        public T getOrThrow() {
            throw new DomainException(error);
        }

        @Override
        public T getOrElse(T defaultValue) {
            return defaultValue;
        }

        @Override
        public DomainError getError() {
            return error;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <U> Result<U> map(Function<T, U> mapper) {
            // Propagate failure without applying mapper
            return (Result<U>) this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <U> Result<U> flatMap(Function<T, Result<U>> mapper) {
            // Propagate failure without applying mapper
            return (Result<U>) this;
        }
    }
}
