package com.kanokna.shared.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

@DisplayName("PhoneNumber")
class PhoneNumberTest {

    @Test
    @DisplayName("of normalizes separators and preserves plus prefix")
    void ofNormalizesAndPreservesPlus() {
        PhoneNumber phone = PhoneNumber.of("+7 (912) 345-67-89");

        assertThat(phone.value()).isEqualTo("+79123456789");
    }

    @Test
    @DisplayName("of normalizes local format without plus")
    void ofNormalizesWithoutPlus() {
        PhoneNumber phone = PhoneNumber.of("7912 345 6789");

        assertThat(phone.value()).isEqualTo("79123456789");
    }

    @Test
    @DisplayName("of rejects null input")
    void ofRejectsNull() {
        assertThatNullPointerException()
            .isThrownBy(() -> PhoneNumber.of(null))
            .withMessage("Phone number must not be null");
    }

    @Test
    @DisplayName("of rejects empty input")
    void ofRejectsEmpty() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> PhoneNumber.of("   "))
            .withMessage("Phone number must not be empty");
    }

    @Test
    @DisplayName("of rejects invalid characters")
    void ofRejectsInvalidCharacters() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> PhoneNumber.of("+7-ABC-123"))
            .withMessage("Phone number contains invalid characters");
    }

    @Test
    @DisplayName("of rejects numbers shorter than 7 digits")
    void ofRejectsTooShortNumber() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> PhoneNumber.of("123456"))
            .withMessageContaining("at least 7 digits");
    }

    @Test
    @DisplayName("of rejects numbers longer than 15 digits")
    void ofRejectsTooLongNumber() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> PhoneNumber.of("1234567890123456"))
            .withMessageContaining("at most 15 digits");
    }

    @Test
    @DisplayName("masked hides center digits for long international numbers")
    void maskedHidesCenterDigitsForLongInternationalNumbers() {
        PhoneNumber phone = PhoneNumber.of("+7 (912) 345-67-89");

        assertThat(phone.masked()).isEqualTo("+7912***6789");
    }

    @Test
    @DisplayName("masked uses compact fallback for short numbers")
    void maskedUsesCompactFallbackForShortNumbers() {
        PhoneNumber phone = PhoneNumber.of("1234567");

        assertThat(phone.masked()).isEqualTo("12***67");
    }

    @Test
    @DisplayName("toE164 adds plus for local normalized number")
    void toE164AddsPlusWhenMissing() {
        PhoneNumber phone = PhoneNumber.of("79123456789");

        assertThat(phone.toE164()).isEqualTo("+79123456789");
    }

    @Test
    @DisplayName("equals and hashCode are based on normalized value")
    void equalsAndHashCodeBasedOnNormalizedValue() {
        PhoneNumber first = PhoneNumber.of("+7 (912) 345-67-89");
        PhoneNumber second = PhoneNumber.of("+79123456789");
        PhoneNumber third = PhoneNumber.of("79123456789");

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
        assertThat(first).isNotEqualTo(third);
    }
}
