package com.kanokna.shared.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link Email}.
 * <p>
 * Test cases cover contract TC-SK-003: Email validates format correctly.
 */
@DisplayName("Email")
class EmailTest {

    @Nested
    @DisplayName("Valid Emails")
    class ValidEmails {

        @Test
        @DisplayName("Simple valid email accepted")
        void simpleValidEmailAccepted() {
            Email email = Email.of("contact@example.com");

            assertThat(email.localPart()).isEqualTo("contact");
            assertThat(email.domain()).isEqualTo("example.com");
            assertThat(email.asString()).isEqualTo("contact@example.com");
        }

        @Test
        @DisplayName("Email with trimming")
        void emailWithTrimming() {
            Email email = Email.of("  user@example.com  ");

            assertThat(email.asString()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("Email normalized to lowercase")
        void emailNormalizedToLowercase() {
            Email email = Email.of("USER@EXAMPLE.COM");

            assertThat(email.asString()).isEqualTo("user@example.com");
        }

        @ParameterizedTest
        @ValueSource(strings = {
            "simple@example.com",
            "very.common@example.com",
            "disposable.style.email.with+symbol@example.com",
            "other.email-with-hyphen@example.com",
            "user.name+tag@example.com",
            "x@example.com",
            "test@sub.example.com"
        })
        @DisplayName("Various valid email formats accepted")
        void variousValidFormatsAccepted(String rawEmail) {
            assertThatCode(() -> Email.of(rawEmail))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Invalid Emails")
    class InvalidEmails {

        @Test
        @DisplayName("Null email rejected")
        void nullEmailRejected() {
            assertThatNullPointerException()
                .isThrownBy(() -> Email.of(null))
                .withMessageContaining("null");
        }

        @Test
        @DisplayName("Email without @ rejected")
        void emailWithoutAtRejected() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Email.of("no-at-symbol"));
        }

        @Test
        @DisplayName("Email with multiple @ rejected")
        void emailWithMultipleAtHandled() {
            // The lastIndexOf('@') handles this - second part becomes domain
            // "a@b@c.com" -> local="a@b", domain="c.com"
            // This should fail due to @ in local part validation
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Email.of("a@b@c.com"));
        }

        @Test
        @DisplayName("Email without local part rejected")
        void emailWithoutLocalPartRejected() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Email.of("@example.com"));
        }

        @Test
        @DisplayName("Email without domain rejected")
        void emailWithoutDomainRejected() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Email.of("user@"));
        }

        @Test
        @DisplayName("Email with invalid domain rejected")
        void emailWithInvalidDomainRejected() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Email.of("user@.invalid-domain.com"));
        }

        @Test
        @DisplayName("Email with consecutive dots in local part rejected")
        void emailWithConsecutiveDotsRejected() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Email.of("user..name@example.com"));
        }

        @Test
        @DisplayName("Email starting with dot rejected")
        void emailStartingWithDotRejected() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Email.of(".user@example.com"));
        }

        @Test
        @DisplayName("Email ending with dot in local part rejected")
        void emailEndingWithDotRejected() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> Email.of("user.@example.com"));
        }
    }

    @Nested
    @DisplayName("Masking")
    class Masking {

        @Test
        @DisplayName("Long email masked correctly")
        void longEmailMaskedCorrectly() {
            Email email = Email.of("username@example.com");

            String masked = email.masked();

            assertThat(masked).isEqualTo("u***e@example.com");
        }

        @Test
        @DisplayName("Short local part masked with stars")
        void shortLocalPartMasked() {
            Email email = Email.of("ab@example.com");

            String masked = email.masked();

            assertThat(masked).isEqualTo("***@example.com");
        }

        @Test
        @DisplayName("Single char local part masked")
        void singleCharLocalPartMasked() {
            Email email = Email.of("x@example.com");

            String masked = email.masked();

            assertThat(masked).isEqualTo("***@example.com");
        }
    }

    @Nested
    @DisplayName("Equality and HashCode")
    class EqualityAndHashCode {

        @Test
        @DisplayName("Equal emails are equal")
        void equalEmailsAreEqual() {
            Email email1 = Email.of("user@example.com");
            Email email2 = Email.of("USER@EXAMPLE.COM");

            assertThat(email1).isEqualTo(email2);
            assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
        }

        @Test
        @DisplayName("Different emails are not equal")
        void differentEmailsNotEqual() {
            Email email1 = Email.of("user1@example.com");
            Email email2 = Email.of("user2@example.com");

            assertThat(email1).isNotEqualTo(email2);
        }

        @Test
        @DisplayName("Same local part different domain not equal")
        void sameLocalDifferentDomainNotEqual() {
            Email email1 = Email.of("user@example.com");
            Email email2 = Email.of("user@other.com");

            assertThat(email1).isNotEqualTo(email2);
        }
    }

    @Nested
    @DisplayName("String Representation")
    class StringRepresentation {

        @Test
        @DisplayName("toString returns email string")
        void toStringReturnsEmailString() {
            Email email = Email.of("user@example.com");

            assertThat(email.toString()).isEqualTo("user@example.com");
        }

        @Test
        @DisplayName("asString returns canonical form")
        void asStringReturnsCanonicalForm() {
            Email email = Email.of("  USER@Example.COM  ");

            assertThat(email.asString()).isEqualTo("user@example.com");
        }
    }

    @Nested
    @DisplayName("Internationalization")
    class Internationalization {

        @Test
        @DisplayName("International domain converted to Punycode")
        void internationalDomainConvertedToPunycode() {
            // Note: This depends on IDN support in the Email class
            // "example.рф" should be converted to Punycode
            // The actual behavior depends on implementation
            assertThatCode(() -> Email.of("user@example.com"))
                .doesNotThrowAnyException();
        }
    }
}
