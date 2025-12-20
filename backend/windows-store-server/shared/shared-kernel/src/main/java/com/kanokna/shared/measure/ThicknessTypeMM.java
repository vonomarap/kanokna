package com.kanokna.shared.measure;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ThicknessTypeMM {
  MM_58(58),
  MM_60(60),
  MM_70(70),
  MM_76(76),
  MM_80(80),
  MM_86(86);

  private final int valueMm;

  ThicknessTypeMM(Integer valueMm) {
    this.valueMm = valueMm;
  }

  public Integer getValue() {
    return valueMm;
  }

  // A static map for highly efficient lookups.
  private static final Map<Integer, ThicknessTypeMM> LOOKUP = Stream.of(values())
    .collect(Collectors.toMap(ThicknessTypeMM::getValue, Function.identity()));


  /**
   * Safely finds a ThicknessTypeMM from a raw integer value.
   * This is the recommended, null-safe way to perform a reverse lookup.
   *
   * @param valueMm The thickness in millimeters (e.g., 70).
   * @return an Optional containing the matching enum, or an empty Optional if not found.
   */
  public static Optional<ThicknessTypeMM> fromValue(int valueMm) {
    return Optional.ofNullable(LOOKUP.get(valueMm));
  }

  /**
   * Finds a ThicknessTypeMM from a raw integer value, throwing an exception if not found.
   * Use this when the value is expected to be valid and a mismatch is a critical error.
   *
   * @param valueMm The thickness in millimeters.
   * @return the matching enum constant.
   * @throws IllegalArgumentException if no matching thickness is found.
   */
  public static ThicknessTypeMM fromValueOrThrow(int valueMm) {
    return fromValue(valueMm)
      .orElseThrow(() -> new IllegalArgumentException("No ThicknessTypeMM defined for value: " + valueMm));
  }

}
