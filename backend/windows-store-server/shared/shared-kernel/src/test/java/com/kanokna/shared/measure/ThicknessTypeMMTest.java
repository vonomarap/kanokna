package com.kanokna.shared.measure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

@DisplayName("ThicknessTypeMM")
class ThicknessTypeMMTest {

    @Test
    @DisplayName("fromValue returns enum for known value")
    void fromValueReturnsEnumForKnownValue() {
        assertThat(ThicknessTypeMM.fromValue(70)).contains(ThicknessTypeMM.MM_70);
    }

    @Test
    @DisplayName("fromValue returns empty for unknown value")
    void fromValueReturnsEmptyForUnknownValue() {
        assertThat(ThicknessTypeMM.fromValue(65)).isEmpty();
    }

    @Test
    @DisplayName("fromValueOrThrow returns enum for known value")
    void fromValueOrThrowReturnsEnumForKnownValue() {
        assertThat(ThicknessTypeMM.fromValueOrThrow(86)).isEqualTo(ThicknessTypeMM.MM_86);
    }

    @Test
    @DisplayName("fromValueOrThrow throws for unknown value")
    void fromValueOrThrowThrowsForUnknownValue() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> ThicknessTypeMM.fromValueOrThrow(65))
            .withMessage("No ThicknessTypeMM defined for value: 65");
    }
}
