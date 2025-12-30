package com.kanokna.shared.measure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for {@link DimensionsCm}.
 * <p>
 * Test cases cover contracts from FC-shared-kernel-measure-DimensionsCm-validate:
 * - TC-DIM-001: Valid dimensions (100x150) accepted
 * - TC-DIM-002: Minimum dimensions (50x50) accepted
 * - TC-DIM-003: Maximum dimensions (400x400) accepted
 * - TC-DIM-004: Width below 50 rejected
 * - TC-DIM-005: Height above 400 rejected
 * - TC-DIM-006: Area calculated correctly
 */
@DisplayName("DimensionsCm")
class DimensionsCmTest {

    @Nested
    @DisplayName("Valid Dimensions")
    class ValidDimensions {

        @Test
        @DisplayName("TC-DIM-001: Valid dimensions (100x150) accepted")
        void validDimensionsAccepted() {
            DimensionsCm dims = new DimensionsCm(100, 150);

            assertThat(dims.width()).isEqualTo(100);
            assertThat(dims.height()).isEqualTo(150);
        }

        @Test
        @DisplayName("TC-DIM-002: Minimum dimensions (50x50) accepted")
        void minimumDimensionsAccepted() {
            DimensionsCm dims = new DimensionsCm(50, 50);

            assertThat(dims.width()).isEqualTo(50);
            assertThat(dims.height()).isEqualTo(50);
        }

        @Test
        @DisplayName("TC-DIM-003: Maximum dimensions (400x400) accepted")
        void maximumDimensionsAccepted() {
            DimensionsCm dims = new DimensionsCm(400, 400);

            assertThat(dims.width()).isEqualTo(400);
            assertThat(dims.height()).isEqualTo(400);
        }

        @Test
        @DisplayName("Mixed min/max dimensions accepted")
        void mixedMinMaxAccepted() {
            DimensionsCm dims1 = new DimensionsCm(50, 400);
            DimensionsCm dims2 = new DimensionsCm(400, 50);

            assertThat(dims1.width()).isEqualTo(50);
            assertThat(dims1.height()).isEqualTo(400);
            assertThat(dims2.width()).isEqualTo(400);
            assertThat(dims2.height()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("Invalid Dimensions")
    class InvalidDimensions {

        @Test
        @DisplayName("TC-DIM-004: Width below 50 rejected")
        void widthBelowMinimumRejected() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> new DimensionsCm(49, 100))
                .withMessageContaining("ERR-DIM-TOO-SMALL")
                .withMessageContaining("width");
        }

        @Test
        @DisplayName("TC-DIM-005: Height above 400 rejected")
        void heightAboveMaximumRejected() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> new DimensionsCm(100, 401))
                .withMessageContaining("ERR-DIM-TOO-LARGE")
                .withMessageContaining("height");
        }

        @Test
        @DisplayName("Width above 400 rejected")
        void widthAboveMaximumRejected() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> new DimensionsCm(401, 100))
                .withMessageContaining("ERR-DIM-TOO-LARGE")
                .withMessageContaining("width");
        }

        @Test
        @DisplayName("Height below 50 rejected")
        void heightBelowMinimumRejected() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> new DimensionsCm(100, 49))
                .withMessageContaining("ERR-DIM-TOO-SMALL")
                .withMessageContaining("height");
        }

        @Test
        @DisplayName("Zero dimensions rejected")
        void zeroDimensionsRejected() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> new DimensionsCm(0, 100))
                .withMessageContaining("positive");
        }

        @Test
        @DisplayName("Negative dimensions rejected")
        void negativeDimensionsRejected() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> new DimensionsCm(-50, 100))
                .withMessageContaining("positive");
        }

        @Test
        @DisplayName("Multiple violations reported together")
        void multipleViolationsReportedTogether() {
            assertThatIllegalArgumentException()
                .isThrownBy(() -> new DimensionsCm(30, 500))
                .withMessageContaining("ERR-DIM-TOO-SMALL")
                .withMessageContaining("ERR-DIM-TOO-LARGE");
        }
    }

    @Nested
    @DisplayName("Derived Properties")
    class DerivedProperties {

        @Test
        @DisplayName("TC-DIM-006: Area calculated correctly")
        void areaCalculatedCorrectly() {
            DimensionsCm dims = new DimensionsCm(100, 150);

            assertThat(dims.areaCm2()).isEqualTo(15000L);
            assertThat(dims.areaM2()).isEqualTo(1.5);
        }

        @Test
        @DisplayName("Maximum area calculated without overflow")
        void maxAreaNoOverflow() {
            DimensionsCm dims = new DimensionsCm(400, 400);

            // 400 * 400 = 160000 cm² = 16 m²
            assertThat(dims.areaCm2()).isEqualTo(160000L);
            assertThat(dims.areaM2()).isEqualTo(16.0);
        }

        @Test
        @DisplayName("Perimeter calculated correctly")
        void perimeterCalculatedCorrectly() {
            DimensionsCm dims = new DimensionsCm(100, 150);

            // 2 * (100 + 150) = 500 cm = 5 m
            assertThat(dims.perimeterCm()).isEqualTo(500);
            assertThat(dims.perimeterM()).isEqualTo(5.0);
        }

        @Test
        @DisplayName("Minimum dimensions area and perimeter")
        void minimumDimensionsDerived() {
            DimensionsCm dims = new DimensionsCm(50, 50);

            assertThat(dims.areaCm2()).isEqualTo(2500L);
            assertThat(dims.areaM2()).isEqualTo(0.25);
            assertThat(dims.perimeterCm()).isEqualTo(200);
        }
    }

    @Nested
    @DisplayName("Orientation Queries")
    class OrientationQueries {

        @Test
        @DisplayName("isSquare returns true for equal dimensions")
        void isSquareReturnsTrue() {
            DimensionsCm dims = new DimensionsCm(100, 100);

            assertThat(dims.isSquare()).isTrue();
            assertThat(dims.isLandscape()).isFalse();
            assertThat(dims.isPortrait()).isFalse();
        }

        @Test
        @DisplayName("isLandscape returns true when width > height")
        void isLandscapeReturnsTrue() {
            DimensionsCm dims = new DimensionsCm(200, 100);

            assertThat(dims.isLandscape()).isTrue();
            assertThat(dims.isPortrait()).isFalse();
            assertThat(dims.isSquare()).isFalse();
        }

        @Test
        @DisplayName("isPortrait returns true when height > width")
        void isPortraitReturnsTrue() {
            DimensionsCm dims = new DimensionsCm(100, 200);

            assertThat(dims.isPortrait()).isTrue();
            assertThat(dims.isLandscape()).isFalse();
            assertThat(dims.isSquare()).isFalse();
        }
    }

    @Nested
    @DisplayName("Fitting and Rotation")
    class FittingAndRotation {

        @Test
        @DisplayName("fitsWithin returns true when smaller")
        void fitsWithinWhenSmaller() {
            DimensionsCm small = new DimensionsCm(100, 150);
            DimensionsCm large = new DimensionsCm(200, 200);

            assertThat(small.fitsWithin(large)).isTrue();
        }

        @Test
        @DisplayName("fitsWithin returns true when equal")
        void fitsWithinWhenEqual() {
            DimensionsCm dims1 = new DimensionsCm(100, 100);
            DimensionsCm dims2 = new DimensionsCm(100, 100);

            assertThat(dims1.fitsWithin(dims2)).isTrue();
        }

        @Test
        @DisplayName("fitsWithin returns false when larger")
        void doesNotFitWhenLarger() {
            DimensionsCm large = new DimensionsCm(200, 200);
            DimensionsCm small = new DimensionsCm(100, 100);

            assertThat(large.fitsWithin(small)).isFalse();
        }

        @Test
        @DisplayName("rotate swaps width and height")
        void rotateSwapsDimensions() {
            DimensionsCm original = new DimensionsCm(100, 200);

            DimensionsCm rotated = original.rotate();

            assertThat(rotated.width()).isEqualTo(200);
            assertThat(rotated.height()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("Factory Methods")
    class FactoryMethods {

        @Test
        @DisplayName("square creates square dimensions")
        void squareCreatesSquare() {
            DimensionsCm square = DimensionsCm.square(150);

            assertThat(square.width()).isEqualTo(150);
            assertThat(square.height()).isEqualTo(150);
            assertThat(square.isSquare()).isTrue();
        }
    }

    @Nested
    @DisplayName("Equality and ToString")
    class EqualityAndToString {

        @Test
        @DisplayName("Equal dimensions are equal")
        void equalDimensionsAreEqual() {
            DimensionsCm dims1 = new DimensionsCm(100, 150);
            DimensionsCm dims2 = new DimensionsCm(100, 150);

            assertThat(dims1).isEqualTo(dims2);
            assertThat(dims1.hashCode()).isEqualTo(dims2.hashCode());
        }

        @Test
        @DisplayName("Different dimensions are not equal")
        void differentDimensionsNotEqual() {
            DimensionsCm dims1 = new DimensionsCm(100, 150);
            DimensionsCm dims2 = new DimensionsCm(100, 151);

            assertThat(dims1).isNotEqualTo(dims2);
        }

        @Test
        @DisplayName("toString returns formatted string")
        void toStringReturnsFormatted() {
            DimensionsCm dims = new DimensionsCm(120, 180);

            assertThat(dims.toString()).isEqualTo("120x180 cm");
        }
    }

    @Nested
    @DisplayName("Constants")
    class Constants {

        @Test
        @DisplayName("MIN_DIMENSION_CM is 50")
        void minDimensionIs50() {
            assertThat(DimensionsCm.MIN_DIMENSION_CM).isEqualTo(50);
        }

        @Test
        @DisplayName("MAX_DIMENSION_CM is 400")
        void maxDimensionIs400() {
            assertThat(DimensionsCm.MAX_DIMENSION_CM).isEqualTo(400);
        }
    }
}
