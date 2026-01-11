package com.kanokna.shared.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link Result}.
 * <p>
 * Test cases cover contracts from FC-shared-kernel-core-Result:
 * - TC-RESULT-001: Success.isSuccess returns true
 * - TC-RESULT-002: Failure.isFailure returns true
 * - TC-RESULT-003: map transforms Success value
 * - TC-RESULT-004: map propagates Failure
 * - TC-RESULT-005: flatMap chains operations
 * - TC-RESULT-006: getOrElse returns default on Failure
 */
@DisplayName("Result")
class ResultTest {

    @Nested
    @DisplayName("Success")
    class SuccessTests {

        @Test
        @DisplayName("TC-RESULT-001: Success.isSuccess returns true")
        void successIsSuccessReturnsTrue() {
            Result<String> result = Result.success("value");

            assertThat(result.isSuccess()).isTrue();
            assertThat(result.isFailure()).isFalse();
        }

        @Test
        @DisplayName("Success.getOrThrow returns value")
        void successGetOrThrowReturnsValue() {
            Result<String> result = Result.success("hello");

            assertThat(result.getOrThrow()).isEqualTo("hello");
        }

        @Test
        @DisplayName("Success.getOrElse returns value (ignores default)")
        void successGetOrElseReturnsValue() {
            Result<String> result = Result.success("hello");

            assertThat(result.getOrElse("default")).isEqualTo("hello");
        }

        @Test
        @DisplayName("Success.getError throws IllegalStateException")
        void successGetErrorThrows() {
            Result<String> result = Result.success("value");

            assertThatIllegalStateException()
                .isThrownBy(result::getError)
                .withMessageContaining("Success");
        }

        @Test
        @DisplayName("Success cannot be created with null value")
        void successCannotBeNull() {
            assertThatNullPointerException()
                .isThrownBy(() -> Result.success(null))
                .withMessageContaining("null");
        }
    }

    @Nested
    @DisplayName("Failure")
    class FailureTests {

        @Test
        @DisplayName("TC-RESULT-002: Failure.isFailure returns true")
        void failureIsFailureReturnsTrue() {
            Result<String> result = Result.failure(DomainError.of("ERR-001", "Error"));

            assertThat(result.isFailure()).isTrue();
            assertThat(result.isSuccess()).isFalse();
        }

        @Test
        @DisplayName("Failure.getOrThrow throws DomainException")
        void failureGetOrThrowThrows() {
            DomainError error = DomainError.of("ERR-001", "Something went wrong");
            Result<String> result = Result.failure(error);

            assertThatExceptionOfType(DomainException.class)
                .isThrownBy(result::getOrThrow)
                .satisfies(e -> {
                    assertThat(e.getError()).isEqualTo(error);
                    assertThat(e.getCode()).isEqualTo("ERR-001");
                });
        }

        @Test
        @DisplayName("TC-RESULT-006: getOrElse returns default on Failure")
        void failureGetOrElseReturnsDefault() {
            Result<String> result = Result.failure(DomainError.of("ERR", "Error"));

            assertThat(result.getOrElse("default")).isEqualTo("default");
        }

        @Test
        @DisplayName("Failure.getError returns error")
        void failureGetErrorReturnsError() {
            DomainError error = DomainError.of("ERR-002", "Error message");
            Result<String> result = Result.failure(error);

            assertThat(result.getError()).isEqualTo(error);
        }

        @Test
        @DisplayName("Failure cannot be created with null error")
        void failureCannotBeNull() {
            assertThatNullPointerException()
                .isThrownBy(() -> Result.failure(null))
                .withMessageContaining("null");
        }

        @Test
        @DisplayName("Failure can be created with code and message")
        void failureWithCodeAndMessage() {
            Result<String> result = Result.failure("ERR-CODE", "Error message");

            assertThat(result.isFailure()).isTrue();
            assertThat(result.getError().code()).isEqualTo("ERR-CODE");
            assertThat(result.getError().message()).isEqualTo("Error message");
        }
    }

    @Nested
    @DisplayName("Map Operation")
    class MapOperation {

        @Test
        @DisplayName("TC-RESULT-003: map transforms Success value")
        void mapTransformsSuccessValue() {
            Result<Integer> result = Result.success(5);

            Result<String> mapped = result.map(i -> "Value: " + i);

            assertThat(mapped.isSuccess()).isTrue();
            assertThat(mapped.getOrThrow()).isEqualTo("Value: 5");
        }

        @Test
        @DisplayName("TC-RESULT-004: map propagates Failure")
        void mapPropagatesFailure() {
            DomainError error = DomainError.of("ERR", "Error");
            Result<Integer> result = Result.failure(error);

            Result<String> mapped = result.map(i -> "Value: " + i);

            assertThat(mapped.isFailure()).isTrue();
            assertThat(mapped.getError()).isEqualTo(error);
        }

        @Test
        @DisplayName("map with null mapper throws")
        void mapWithNullMapperThrows() {
            Result<String> result = Result.success("value");

            assertThatNullPointerException()
                .isThrownBy(() -> result.map(null));
        }

        @Test
        @DisplayName("map chains correctly")
        void mapChainsCorrectly() {
            Result<Integer> result = Result.success(10);

            Result<Integer> doubled = result
                .map(i -> i * 2)
                .map(i -> i + 5);

            assertThat(doubled.getOrThrow()).isEqualTo(25);
        }
    }

    @Nested
    @DisplayName("FlatMap Operation")
    class FlatMapOperation {

        @Test
        @DisplayName("TC-RESULT-005: flatMap chains operations")
        void flatMapChainsOperations() {
            Result<Integer> result = Result.success(5);

            Result<Integer> chained = result.flatMap(i -> Result.success(i * 2));

            assertThat(chained.isSuccess()).isTrue();
            assertThat(chained.getOrThrow()).isEqualTo(10);
        }

        @Test
        @DisplayName("flatMap propagates Failure from first result")
        void flatMapPropagatesFirstFailure() {
            DomainError error = DomainError.of("ERR-1", "First error");
            Result<Integer> result = Result.failure(error);

            Result<Integer> chained = result.flatMap(i -> Result.success(i * 2));

            assertThat(chained.isFailure()).isTrue();
            assertThat(chained.getError()).isEqualTo(error);
        }

        @Test
        @DisplayName("flatMap propagates Failure from mapper")
        void flatMapPropagatesMapperFailure() {
            Result<Integer> result = Result.success(5);
            DomainError error = DomainError.of("ERR-2", "Mapper error");

            Result<Integer> chained = result.flatMap(i -> Result.failure(error));

            assertThat(chained.isFailure()).isTrue();
            assertThat(chained.getError()).isEqualTo(error);
        }

        @Test
        @DisplayName("flatMap chains multiple operations")
        void flatMapChainsMultipleOperations() {
            Result<Integer> result = Result.success(5);

            Result<String> chained = result
                .flatMap(i -> i > 0 ? Result.success(i * 2) : Result.failure("ERR", "Must be positive"))
                .flatMap(i -> Result.success("Result: " + i));

            assertThat(chained.isSuccess()).isTrue();
            assertThat(chained.getOrThrow()).isEqualTo("Result: 10");
        }

        @Test
        @DisplayName("flatMap short-circuits on failure")
        void flatMapShortCircuitsOnFailure() {
            Result<Integer> result = Result.success(5);

            Result<String> chained = result
                .flatMap(i -> Result.<Integer>failure("ERR", "Stop here"))
                .flatMap(i -> Result.success("Never reached: " + i));

            assertThat(chained.isFailure()).isTrue();
            assertThat(chained.getError().message()).isEqualTo("Stop here");
        }
    }

    @Nested
    @DisplayName("Pattern Matching")
    class PatternMatching {

        @Test
        @DisplayName("Success can be deconstructed")
        void successCanBeDeconstructed() {
            Result<String> result = Result.success("hello");

            String value = switch (result) {
                case Result.Success<String>(String v) -> v;
                case Result.Failure<String> f -> "failed";
            };

            assertThat(value).isEqualTo("hello");
        }

        @Test
        @DisplayName("Failure can be deconstructed")
        void failureCanBeDeconstructed() {
            Result<String> result = Result.failure(DomainError.of("ERR", "Error"));

            String errorCode = switch (result) {
                case Result.Success<String> s -> "success";
                case Result.Failure<String>(DomainError e) -> e.code();
            };

            assertThat(errorCode).isEqualTo("ERR");
        }
    }

    @Nested
    @DisplayName("DomainError")
    class DomainErrorTests {

        @Test
        @DisplayName("DomainError with context")
        void domainErrorWithContext() {
            DomainError error = DomainError.of("ERR-001", "Invalid value", "field", "amount");

            assertThat(error.code()).isEqualTo("ERR-001");
            assertThat(error.message()).isEqualTo("Invalid value");
            assertThat(error.context()).containsEntry("field", "amount");
        }

        @Test
        @DisplayName("DomainError context is immutable")
        void domainErrorContextIsImmutable() {
            DomainError error = DomainError.of("ERR", "Error", "key", "value");

            assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> error.context().put("new", "entry"));
        }

        @Test
        @DisplayName("DomainError toLogString formats correctly")
        void domainErrorToLogStringFormats() {
            DomainError error = DomainError.of("ERR-001", "Something failed");

            assertThat(error.toLogString()).isEqualTo("[ERR-001] Something failed");
        }

        @Test
        @DisplayName("DomainError toLogString includes context")
        void domainErrorToLogStringIncludesContext() {
            DomainError error = DomainError.of("ERR-001", "Failed", "field", "name");

            assertThat(error.toLogString()).contains("[ERR-001]").contains("Failed").contains("field");
        }
    }

    @Nested
    @DisplayName("DomainException")
    class DomainExceptionTests {

        @Test
        @DisplayName("DomainException wraps DomainError")
        void domainExceptionWrapsDomainError() {
            DomainError error = DomainError.of("ERR-001", "Error message");
            DomainException exception = new DomainException(error);

            assertThat(exception.getError()).isEqualTo(error);
            assertThat(exception.getCode()).isEqualTo("ERR-001");
            assertThat(exception.getMessage()).isEqualTo("Error message");
        }

        @Test
        @DisplayName("DomainException can be created with code and message")
        void domainExceptionWithCodeAndMessage() {
            DomainException exception = new DomainException("ERR-002", "Something went wrong");

            assertThat(exception.getCode()).isEqualTo("ERR-002");
            assertThat(exception.getMessage()).isEqualTo("Something went wrong");
        }

        @Test
        @DisplayName("DomainException toString is informative")
        void domainExceptionToString() {
            DomainException exception = new DomainException("ERR-003", "Test error");

            assertThat(exception.toString()).contains("ERR-003").contains("Test error");
        }
    }
}
