package com.kanokna.shared.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

@DisplayName("Id")
class IdTest {
    private interface UserTag {}
    private interface OrderTag {}

    @Test
    @DisplayName("of creates typed id for non-blank value")
    void ofCreatesId() {
        Id<UserTag> id = Id.of("abc-123");

        assertThat(id.value()).isEqualTo("abc-123");
        assertThat(id.toString()).isEqualTo("abc-123");
    }

    @Test
    @DisplayName("of rejects null value")
    void ofRejectsNull() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> Id.of(null))
            .withMessage("ID must be non-empty");
    }

    @Test
    @DisplayName("of rejects blank value")
    void ofRejectsBlank() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> Id.of("   "))
            .withMessage("ID must be non-empty");
    }

    @Test
    @DisplayName("constructor accepts non-blank value")
    void constructorAcceptsNonBlank() {
        Id<UserTag> id = new Id<>("ctor-123");

        assertThat(id.value()).isEqualTo("ctor-123");
    }

    @Test
    @DisplayName("constructor rejects blank value")
    void constructorRejectsBlank() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new Id<UserTag>(" "))
            .withMessage("ID must be non-empty");
    }

    @Test
    @DisplayName("random creates non-blank unique values")
    void randomCreatesNonBlankUniqueValues() {
        Id<UserTag> id1 = Id.random();
        Id<UserTag> id2 = Id.random();

        assertThat(id1.value()).isNotBlank();
        assertThat(id2.value()).isNotBlank();
        assertThat(id1).isNotEqualTo(id2);
    }

    @Test
    @DisplayName("typed ids keep domain separation at compile time")
    void typedIdsKeepDomainSeparationAtCompileTime() {
        Id<UserTag> userId = Id.of("user-123");
        Id<OrderTag> orderId = Id.of("order-456");

        assertThat(userId.value()).isEqualTo("user-123");
        assertThat(orderId.value()).isEqualTo("order-456");
    }

    @Test
    @DisplayName("equal values have equal hashCode")
    void equalValuesHaveEqualHashCode() {
        Id<UserTag> left = Id.of("same");
        Id<UserTag> right = Id.of("same");
        Id<UserTag> different = Id.of("other");

        assertThat(left).isEqualTo(right);
        assertThat(left.hashCode()).isEqualTo(right.hashCode());
        assertThat(left).isNotEqualTo(different);
    }
}
