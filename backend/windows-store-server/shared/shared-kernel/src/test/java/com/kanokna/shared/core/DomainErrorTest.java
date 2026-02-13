package com.kanokna.shared.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

@DisplayName("DomainError")
class DomainErrorTest {

    @Test
    @DisplayName("canonical constructor requires non-null code")
    void canonicalConstructorRequiresCode() {
        assertThatNullPointerException()
            .isThrownBy(() -> new DomainError(null, "msg", Map.of()))
            .withMessage("Error code cannot be null");
    }

    @Test
    @DisplayName("canonical constructor requires non-null message")
    void canonicalConstructorRequiresMessage() {
        assertThatNullPointerException()
            .isThrownBy(() -> new DomainError("ERR", null, Map.of()))
            .withMessage("Error message cannot be null");
    }

    @Test
    @DisplayName("canonical constructor defaults null context to empty map")
    void canonicalConstructorDefaultsNullContext() {
        DomainError error = new DomainError("ERR", "Message", null);

        assertThat(error.context()).isEmpty();
    }

    @Test
    @DisplayName("constructor defensively copies and freezes context")
    void constructorDefensivelyCopiesContext() {
        Map<String, Object> mutable = new HashMap<>();
        mutable.put("field", "value");

        DomainError error = new DomainError("ERR", "Message", mutable);
        mutable.put("field", "changed");

        assertThat(error.context()).containsEntry("field", "value");
        assertThatExceptionOfType(UnsupportedOperationException.class)
            .isThrownBy(() -> error.context().put("new", "entry"));
    }

    @Test
    @DisplayName("factory of(code, message) creates error without context")
    void factoryWithoutContext() {
        DomainError error = DomainError.of("ERR-001", "Failed");

        assertThat(error.code()).isEqualTo("ERR-001");
        assertThat(error.message()).isEqualTo("Failed");
        assertThat(error.context()).isEmpty();
    }

    @Test
    @DisplayName("factory of with key/value creates one context entry")
    void factoryWithSingleContextEntry() {
        DomainError error = DomainError.of("ERR-002", "Invalid", "field", "email");

        assertThat(error.context())
            .hasSize(1)
            .containsEntry("field", "email");
    }

    @Test
    @DisplayName("factory of with full context uses provided map entries")
    void factoryWithFullContext() {
        DomainError error = DomainError.of("ERR-003", "Invalid", Map.of("field", "phone", "reason", "format"));

        assertThat(error.context())
            .containsEntry("field", "phone")
            .containsEntry("reason", "format");
    }

    @Test
    @DisplayName("toLogString omits context when empty")
    void toLogStringOmitsContextWhenEmpty() {
        DomainError error = DomainError.of("ERR-004", "Boom");

        assertThat(error.toLogString()).isEqualTo("[ERR-004] Boom");
    }

    @Test
    @DisplayName("toLogString includes context when present")
    void toLogStringIncludesContext() {
        DomainError error = DomainError.of("ERR-005", "Boom", "field", "name");

        assertThat(error.toLogString())
            .contains("[ERR-005]")
            .contains("Boom")
            .contains("context=")
            .contains("field=name");
    }
}
