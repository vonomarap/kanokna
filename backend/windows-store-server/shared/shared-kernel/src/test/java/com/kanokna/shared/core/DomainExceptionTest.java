package com.kanokna.shared.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

@DisplayName("DomainException")
class DomainExceptionTest {

    @Test
    @DisplayName("constructor with DomainError stores error and message")
    void constructorWithDomainErrorStoresErrorAndMessage() {
        DomainError error = DomainError.of("ERR-100", "Validation failed");

        DomainException exception = new DomainException(error);

        assertThat(exception.getError()).isEqualTo(error);
        assertThat(exception.getCode()).isEqualTo("ERR-100");
        assertThat(exception.getMessage()).isEqualTo("Validation failed");
    }

    @Test
    @DisplayName("constructor with DomainError rejects null")
    void constructorWithDomainErrorRejectsNull() {
        assertThatNullPointerException()
            .isThrownBy(() -> new DomainException((DomainError) null))
            .withMessage("DomainError cannot be null");
    }

    @Test
    @DisplayName("constructor with code and message creates wrapped DomainError")
    void constructorWithCodeAndMessageCreatesWrappedError() {
        DomainException exception = new DomainException("ERR-101", "Business rule failed");

        assertThat(exception.getCode()).isEqualTo("ERR-101");
        assertThat(exception.getError().message()).isEqualTo("Business rule failed");
    }

    @Test
    @DisplayName("constructor with cause preserves cause and code")
    void constructorWithCausePreservesCauseAndCode() {
        IllegalStateException cause = new IllegalStateException("io");

        DomainException exception = new DomainException("ERR-102", "Unexpected failure", cause);

        assertThat(exception.getCause()).isSameAs(cause);
        assertThat(exception.getCode()).isEqualTo("ERR-102");
        assertThat(exception.getMessage()).isEqualTo("Unexpected failure");
    }

    @Test
    @DisplayName("toString contains formatted log view of DomainError")
    void toStringContainsFormattedLogView() {
        DomainException exception = new DomainException("ERR-103", "Invalid state");

        assertThat(exception.toString())
            .contains("DomainException")
            .contains("ERR-103")
            .contains("Invalid state");
    }
}
