package com.kanokna.shared.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link Address}.
 */
@DisplayName("Address")
class AddressTest {

    @Nested
    @DisplayName("Valid Addresses")
    class ValidAddresses {

        @Test
        @DisplayName("Full address with all fields")
        void fullAddressWithAllFields() {
            Address address = new Address(
                "Russia",
                "Moscow",
                "123456",
                "Tverskaya Street 1",
                "Apt 42"
            );

            assertThat(address.country()).isEqualTo("Russia");
            assertThat(address.city()).isEqualTo("Moscow");
            assertThat(address.postalCode()).isEqualTo("123456");
            assertThat(address.line1()).isEqualTo("Tverskaya Street 1");
            assertThat(address.line2()).isEqualTo("Apt 42");
        }

        @Test
        @DisplayName("Address without line2 (null)")
        void addressWithoutLine2Null() {
            Address address = new Address(
                "Germany",
                "Berlin",
                "10115",
                "Alexanderplatz 1",
                null
            );

            assertThat(address.line2()).isNull();
        }

        @Test
        @DisplayName("Address with blank line2 normalized to null")
        void addressWithBlankLine2NormalizedToNull() {
            Address address = new Address(
                "France",
                "Paris",
                "75001",
                "Champs-Elysees 100",
                "   "
            );

            assertThat(address.line2()).isNull();
        }

        @Test
        @DisplayName("Address fields are trimmed and normalized")
        void addressFieldsTrimmedAndNormalized() {
            Address address = new Address(
                "  USA  ",
                "  New York  ",
                " 10001 ",
                "  5th Avenue  ",
                null
            );

            assertThat(address.country()).isEqualTo("USA");
            assertThat(address.city()).isEqualTo("New York");
            assertThat(address.postalCode()).isEqualTo("10001");
            assertThat(address.line1()).isEqualTo("5th Avenue");
        }

        @Test
        @DisplayName("Address with special characters")
        void addressWithSpecialCharacters() {
            Address address = new Address(
                "Russia",
                "Saint-Petersburg",
                "190000",
                "Nevsky Prospekt, 28/31",
                "Office #5"
            );

            assertThat(address.line1()).isEqualTo("Nevsky Prospekt, 28/31");
            assertThat(address.line2()).isEqualTo("Office #5");
        }
    }

    @Nested
    @DisplayName("Invalid Addresses")
    class InvalidAddresses {

        @Test
        @DisplayName("Null country rejected")
        void nullCountryRejected() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> new Address(null, "City", "12345", "Street 1", null))
                .withMessageContaining("country");
        }

        @Test
        @DisplayName("Blank country rejected")
        void blankCountryRejected() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> new Address("   ", "City", "12345", "Street 1", null))
                .withMessageContaining("country");
        }

        @Test
        @DisplayName("Null city rejected")
        void nullCityRejected() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> new Address("Country", null, "12345", "Street 1", null))
                .withMessageContaining("city");
        }

        @Test
        @DisplayName("Blank city rejected")
        void blankCityRejected() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> new Address("Country", "", "12345", "Street 1", null))
                .withMessageContaining("city");
        }

        @Test
        @DisplayName("Null postalCode rejected")
        void nullPostalCodeRejected() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> new Address("Country", "City", null, "Street 1", null))
                .withMessageContaining("postalCode");
        }

        @Test
        @DisplayName("Null line1 rejected")
        void nullLine1Rejected() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> new Address("Country", "City", "12345", null, null))
                .withMessageContaining("line1");
        }

        @Test
        @DisplayName("Line1 with invalid characters rejected")
        void line1WithInvalidCharactersRejected() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> new Address("Country", "City", "12345", "<script>alert(1)</script>", null))
                .withMessageContaining("invalid characters");
        }

        @Test
        @DisplayName("Field too long rejected")
        void fieldTooLongRejected() {
            String tooLong = "A".repeat(200);
            assertThatIllegalArgumentException()
                .isThrownBy(() -> new Address(tooLong, "City", "12345", "Street 1", null))
                .withMessageContaining("too long");
        }
    }

    @Nested
    @DisplayName("toSingleLine")
    class ToSingleLine {

        @Test
        @DisplayName("Full address formatted as single line")
        void fullAddressFormattedAsSingleLine() {
            Address address = new Address(
                "Russia",
                "Moscow",
                "123456",
                "Tverskaya Street 1",
                "Apt 42"
            );

            String singleLine = address.toSingleLine();

            assertThat(singleLine).isEqualTo("Tverskaya Street 1, Apt 42, Moscow, 123456, Russia");
        }

        @Test
        @DisplayName("Address without line2 formatted correctly")
        void addressWithoutLine2FormattedCorrectly() {
            Address address = new Address(
                "Germany",
                "Berlin",
                "10115",
                "Alexanderplatz 1",
                null
            );

            String singleLine = address.toSingleLine();

            assertThat(singleLine).isEqualTo("Alexanderplatz 1, Berlin, 10115, Germany");
            assertThat(singleLine).doesNotContain("null");
        }
    }

    @Nested
    @DisplayName("Equality and HashCode")
    class EqualityAndHashCode {

        @Test
        @DisplayName("Equal addresses are equal")
        void equalAddressesAreEqual() {
            Address address1 = new Address("Russia", "Moscow", "123456", "Street 1", "Apt 1");
            Address address2 = new Address("Russia", "Moscow", "123456", "Street 1", "Apt 1");

            assertThat(address1).isEqualTo(address2);
            assertThat(address1.hashCode()).isEqualTo(address2.hashCode());
        }

        @Test
        @DisplayName("Different addresses are not equal")
        void differentAddressesNotEqual() {
            Address address1 = new Address("Russia", "Moscow", "123456", "Street 1", null);
            Address address2 = new Address("Russia", "Moscow", "123456", "Street 2", null);

            assertThat(address1).isNotEqualTo(address2);
        }

        @Test
        @DisplayName("Addresses with different line2 not equal")
        void addressesWithDifferentLine2NotEqual() {
            Address address1 = new Address("Russia", "Moscow", "123456", "Street 1", "Apt 1");
            Address address2 = new Address("Russia", "Moscow", "123456", "Street 1", "Apt 2");

            assertThat(address1).isNotEqualTo(address2);
        }

        @Test
        @DisplayName("Address with null line2 vs non-null not equal")
        void addressWithNullVsNonNullLine2NotEqual() {
            Address address1 = new Address("Russia", "Moscow", "123456", "Street 1", null);
            Address address2 = new Address("Russia", "Moscow", "123456", "Street 1", "Apt 1");

            assertThat(address1).isNotEqualTo(address2);
        }
    }

    @Nested
    @DisplayName("String Representation")
    class StringRepresentation {

        @Test
        @DisplayName("toString contains single line representation")
        void toStringContainsSingleLine() {
            Address address = new Address("Russia", "Moscow", "123456", "Street 1", null);

            String str = address.toString();

            assertThat(str).contains("Address[");
            assertThat(str).contains("Street 1");
            assertThat(str).contains("Moscow");
        }
    }

    @Nested
    @DisplayName("Normalization")
    class Normalization {

        @Test
        @DisplayName("Multiple spaces collapsed")
        void multipleSpacesCollapsed() {
            Address address = new Address(
                "Russia",
                "Moscow",
                "123456",
                "Street   Name   1",
                null
            );

            assertThat(address.line1()).isEqualTo("Street Name 1");
        }
    }
}
