package com.kanokna.shared.measure;

import java.util.StringJoiner;

public record DimensionsCm(
  int width,
  int height
) {

  public static final int MAX_DIMENSION_CM = 400;
  public static final int MIN_DIMENSION_CM = 50;

  public DimensionsCm {
    if (width <= 0 || height <= 0)
      throw new IllegalArgumentException("Dimensions must be positive. Got: width=%d, height=%d".formatted(width, height));

    StringJoiner errorMessages = new StringJoiner(", ");

    if (width < MIN_DIMENSION_CM)
      errorMessages.add("width %dcm is below minimum of %dcm".formatted(width, MIN_DIMENSION_CM));
    if (height < MIN_DIMENSION_CM)
      errorMessages.add("height %dcm is below minimum of %dcm".formatted(height, MIN_DIMENSION_CM));
    if (width > MAX_DIMENSION_CM)
      errorMessages.add("width %dcm exceeds maximum of %dcm".formatted(width, MAX_DIMENSION_CM));
    if (height > MAX_DIMENSION_CM)
      errorMessages.add("height %dcm exceeds maximum of %dcm".formatted(height, MAX_DIMENSION_CM));

    if (errorMessages.length() > 0)
      throw new IllegalArgumentException("Invalid dimensions: " + errorMessages);
  }

  /**
   * Calculates the surface area of the width and height.
   *
   * @return The area in square centimeters (cm²).
   */
  public long area() {
    // Use Math.multiplyExact to prevent silent integer overflow.
    return (long) width * height;
  }

  /**
   * Checks if these dimensions can fit entirely within a given bounding box.
   *
   * @param container the bounding box or container to check against.
   * @return {@code true} if this object's width and height are both less than or equal to the container's.
   */
  public boolean fitsWithin(DimensionsCm container) {
    return this.width <= container.width && this.height <= container.height;
  }
}
