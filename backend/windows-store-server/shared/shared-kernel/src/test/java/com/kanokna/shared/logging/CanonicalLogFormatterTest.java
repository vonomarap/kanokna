package com.kanokna.shared.logging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

@DisplayName("CanonicalLogFormatter")
class CanonicalLogFormatterTest {

    @Test
    @DisplayName("format renders canonical envelope and key-values")
    void formatRendersCanonicalEnvelopeAndKeyValues() {
        Map<String, Object> keyValues = new LinkedHashMap<>();
        keyValues.put("orderId", "o-1");
        keyValues.put("attempt", 2);

        String rendered = CanonicalLogFormatter.format(
            "search-service",
            "IndexProduct",
            "B1",
            "DONE",
            "PRODUCT_UPDATED",
            "INDEXED",
            keyValues
        );

        assertThat(rendered).isEqualTo(
            "[SVC=search-service][UC=IndexProduct][BLOCK=B1][STATE=DONE] eventType=PRODUCT_UPDATED decision=INDEXED orderId=o-1 attempt=2"
        );
    }

    @Test
    @DisplayName("format omits trailing section when key-values are null")
    void formatOmitsKeyValuesWhenNull() {
        String rendered = CanonicalLogFormatter.format(
            "svc",
            "uc",
            "block",
            "state",
            "evt",
            "decision",
            null
        );

        assertThat(rendered).isEqualTo("[SVC=svc][UC=uc][BLOCK=block][STATE=state] eventType=evt decision=decision");
    }

    @Test
    @DisplayName("format omits trailing section when key-values are empty")
    void formatOmitsKeyValuesWhenEmpty() {
        String rendered = CanonicalLogFormatter.format(
            "svc",
            "uc",
            "block",
            "state",
            "evt",
            "decision",
            Map.of()
        );

        assertThat(rendered).isEqualTo("[SVC=svc][UC=uc][BLOCK=block][STATE=state] eventType=evt decision=decision");
    }

    @Test
    @DisplayName("format rejects blank base fields")
    void formatRejectsBlankBaseFields() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> CanonicalLogFormatter.format(" ", "uc", "block", "state", "evt", "decision", Map.of()))
            .withMessage("service must be non-blank");
    }

    @Test
    @DisplayName("format rejects blank key names in key-values")
    void formatRejectsBlankKeyNames() {
        Map<String, Object> keyValues = new LinkedHashMap<>();
        keyValues.put(" ", "value");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> CanonicalLogFormatter.format("svc", "uc", "block", "state", "evt", "decision", keyValues))
            .withMessage("key must be non-blank");
    }

    @Test
    @DisplayName("format renders null values as literal null")
    void formatRendersNullValues() {
        Map<String, Object> keyValues = new LinkedHashMap<>();
        keyValues.put("traceId", null);

        String rendered = CanonicalLogFormatter.format("svc", "uc", "block", "state", "evt", "decision", keyValues);

        assertThat(rendered).endsWith("traceId=null");
    }
}
