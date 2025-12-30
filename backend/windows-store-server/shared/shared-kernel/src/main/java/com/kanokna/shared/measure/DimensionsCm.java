package com.kanokna.shared.measure;

import java.util.StringJoiner;

/* <FUNCTION_CONTRACT id="FC-shared-kernel-measure-DimensionsCm-validate"
     LAYER="domain.value"
     INTENT="Validate window/door dimensions are within allowed range"
     INPUT="int widthCm, int heightCm"
     OUTPUT="DimensionsCm (if valid) or throws IllegalArgumentException"
     SIDE_EFFECTS="None"
     LINKS="RequirementsAnalysis.xml#UC-CATALOG-CONFIGURE-ITEM;RequirementsAnalysis.xml#BR-CFG-001">
   <PRECONDITIONS>
     <Item>widthCm and heightCm are positive integers</Item>
   </PRECONDITIONS>

   <POSTCONDITIONS>
     <Item>Returned DimensionsCm has width and height within [50, 400] cm</Item>
   </POSTCONDITIONS>

   <INVARIANTS>
     <Item>MIN_DIMENSION_CM = 50</Item>
     <Item>MAX_DIMENSION_CM = 400</Item>
     <Item>Both width and height must satisfy: MIN <= value <= MAX</Item>
   </INVARIANTS>

   <ERROR_HANDLING>
     <Item type="BUSINESS" code="ERR-DIM-TOO-SMALL">Dimension below 50cm</Item>
     <Item type="BUSINESS" code="ERR-DIM-TOO-LARGE">Dimension above 400cm</Item>
   </ERROR_HANDLING>

   <DERIVED_PROPERTIES>
     <Property name="areaCm2">width * height</Property>
     <Property name="areaM2">areaCm2 / 10000.0</Property>
     <Property name="perimeterCm">2 * (width + height)</Property>
   </DERIVED_PROPERTIES>

   <TESTS>
     <Case id="TC-DIM-001">Valid dimensions (100x150) accepted</Case>
     <Case id="TC-DIM-002">Minimum dimensions (50x50) accepted</Case>
     <Case id="TC-DIM-003">Maximum dimensions (400x400) accepted</Case>
     <Case id="TC-DIM-004">Width below 50 rejected</Case>
     <Case id="TC-DIM-005">Height above 400 rejected</Case>
     <Case id="TC-DIM-006">Area calculated correctly</Case>
   </TESTS>
 </FUNCTION_CONTRACT> */

/**
 * A value object representing window/door dimensions in centimeters.
 * <p>
 * Dimensions are validated to be within the allowed range of 50-400 cm
 * for both width and height, as specified by business rule BR-CFG-001.
 *
 * <h2>Usage Example:</h2>
 * <pre>{@code
 * DimensionsCm dims = new DimensionsCm(120, 180);
 * double areaM2 = dims.areaM2();  // 2.16 m²
 * int perimeter = dims.perimeterCm();  // 600 cm
 * }</pre>
 *
 * @param width  the width in centimeters (50-400)
 * @param height the height in centimeters (50-400)
 * @see <a href="RequirementsAnalysis.xml#UC-CATALOG-CONFIGURE-ITEM">UC-CATALOG-CONFIGURE-ITEM</a>
 */
public record DimensionsCm(
    int width,
    int height
) {

    /**
     * Maximum allowed dimension in centimeters.
     */
    public static final int MAX_DIMENSION_CM = 400;

    /**
     * Minimum allowed dimension in centimeters.
     */
    public static final int MIN_DIMENSION_CM = 50;

    /**
     * Canonical constructor with validation.
     * <!-- BLOCK_ANCHOR id="BA-SK-DIM-01" purpose="Validate dimension range" -->
     *
     * @param width  the width in centimeters
     * @param height the height in centimeters
     * @throws IllegalArgumentException if dimensions are outside allowed range
     */
    public DimensionsCm {
        // BA-SK-DIM-01: Validate dimension range
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                "Dimensions must be positive. Got: width=%d, height=%d".formatted(width, height));
        }

        StringJoiner errorMessages = new StringJoiner(", ");

        if (width < MIN_DIMENSION_CM) {
            errorMessages.add("width %dcm is below minimum of %dcm [ERR-DIM-TOO-SMALL]"
                .formatted(width, MIN_DIMENSION_CM));
        }
        if (height < MIN_DIMENSION_CM) {
            errorMessages.add("height %dcm is below minimum of %dcm [ERR-DIM-TOO-SMALL]"
                .formatted(height, MIN_DIMENSION_CM));
        }
        if (width > MAX_DIMENSION_CM) {
            errorMessages.add("width %dcm exceeds maximum of %dcm [ERR-DIM-TOO-LARGE]"
                .formatted(width, MAX_DIMENSION_CM));
        }
        if (height > MAX_DIMENSION_CM) {
            errorMessages.add("height %dcm exceeds maximum of %dcm [ERR-DIM-TOO-LARGE]"
                .formatted(height, MAX_DIMENSION_CM));
        }

        if (errorMessages.length() > 0) {
            throw new IllegalArgumentException("Invalid dimensions: " + errorMessages);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Derived Properties
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Calculates the surface area in square centimeters.
     *
     * @return the area in cm² (width * height)
     */
    public long areaCm2() {
        return (long) width * height;
    }

    /**
     * Calculates the surface area in square meters.
     *
     * @return the area in m² (areaCm2 / 10000)
     */
    public double areaM2() {
        return areaCm2() / 10000.0;
    }

    /**
     * Calculates the perimeter in centimeters.
     *
     * @return the perimeter in cm (2 * (width + height))
     */
    public int perimeterCm() {
        return 2 * (width + height);
    }

    /**
     * Calculates the perimeter in meters.
     *
     * @return the perimeter in m
     */
    public double perimeterM() {
        return perimeterCm() / 100.0;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Queries
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Checks if this dimension represents a square (width equals height).
     *
     * @return true if width == height
     */
    public boolean isSquare() {
        return width == height;
    }

    /**
     * Checks if this dimension is landscape orientation (width > height).
     *
     * @return true if width > height
     */
    public boolean isLandscape() {
        return width > height;
    }

    /**
     * Checks if this dimension is portrait orientation (height > width).
     *
     * @return true if height > width
     */
    public boolean isPortrait() {
        return height > width;
    }

    /**
     * Checks if these dimensions can fit entirely within a given bounding box.
     *
     * @param container the bounding box or container to check against
     * @return true if this object's width and height are both less than or equal to the container's
     */
    public boolean fitsWithin(DimensionsCm container) {
        return this.width <= container.width && this.height <= container.height;
    }

    /**
     * Returns a new DimensionsCm with swapped width and height.
     *
     * @return rotated dimensions
     */
    public DimensionsCm rotate() {
        return new DimensionsCm(height, width);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Factory Methods
    // ─────────────────────────────────────────────────────────────────────────────

    /**
     * Creates a square dimension with the given side length.
     *
     * @param sideCm the side length in centimeters
     * @return a square DimensionsCm
     */
    public static DimensionsCm square(int sideCm) {
        return new DimensionsCm(sideCm, sideCm);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Object Methods
    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return "%dx%d cm".formatted(width, height);
    }
}
