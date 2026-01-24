package com.kanokna.shared.logging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link LogSanitizer}.
 */
@DisplayName("LogSanitizer")
class LogSanitizerTest {

    @Test
    @DisplayName("TC-LOG-SANITIZE-001: Newline injection sanitized")
    void sanitizeRemovesNewlinesAndCarriageReturns() {
        String input = "line1\nline2\rline3";

        String result = LogSanitizer.sanitize(input);

        assertThat(result).isEqualTo("line1\\nline2\\rline3");
        assertThat(result).doesNotContain("\n");
        assertThat(result).doesNotContain("\r");
    }

    @Test
    @DisplayName("TC-LOG-SANITIZE-002: ANSI escape sequences sanitized")
    void sanitizeRemovesAnsiEscapeSequences() {
        String input = "\u001b[31mRED\u001b[0m";

        String result = LogSanitizer.sanitize(input);

        assertThat(result).doesNotContain("\u001b");
        assertThat(result).contains("RED");
    }

    @Test
    @DisplayName("TC-LOG-SANITIZE-003: Normal strings unchanged")
    void sanitizeLeavesNormalStringUnchanged() {
        String input = "normal-string-123_ABC";

        String result = LogSanitizer.sanitize(input);

        assertThat(result).isEqualTo(input);
    }

    @Test
    @DisplayName("TC-LOG-SANITIZE-004: Null input handling")
    void sanitizeHandlesNullInput() {
        String result = LogSanitizer.sanitize(null);

        assertThat(result).isEqualTo("[null]");
    }
}
