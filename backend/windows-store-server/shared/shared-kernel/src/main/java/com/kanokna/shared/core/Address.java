package com.kanokna.shared.core;

import java.io.Serializable;
import java.text.Normalizer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
/*
<MODULE_CONTRACT id="mod.shared.address-vo" name="Address" layer="domain" version="1.1.0">
<PURPOSE>To provide a canonical, immutable, and validated representation of a physical postal address.</PURPOSE>
  <RESPONSIBILITIES>
    <RESPONSIBILITY>1. Validate all address components for requiredness, length, and character set.</RESPONSIBILITY>
    <RESPONSIBILITY>2. Normalize string inputs to a canonical form (NFKC, trimmed, single-spaced).</RESPONSIBILITY>
    <RESPONSIBILITY>3. Ensure that an Address instance is always in a valid state upon creation.</RESPONSIBILITY>
    <RESPONSIBILITY>4. Provide a formatted single-line representation of the address.</RESPONSIBILITY>
    <RESPONSIBILITY>5. Treat the optional 'line2' field consistently, canonicalizing blank inputs to null.</RESPONSIBILITY>
  </RESPONSIBILITIES>
  <INVARIANTS>
<INVARIANT>Required fields (country, city, postalCode, line1) are never null or blank.</INVARIANT>
<INVARIANT>All fields, if present, conform to defined length and character constraints.</INVARIANT>
<INVARIANT>An Address instance is immutable after creation.</INVARIANT>
    <INVARIANT>The 'line2' field is null if it was not provided or was blank.</INVARIANT>
  </INVARIANTS>
  <SCENARIOS>
    <SCENARIO>Creation: An Address is created from its constituent parts, which are immediately validated and canonicalized.</SCENARIO>
    <SCENARIO>Formatting: The address can be requested as a single, comma-separated string for display.</SCENARIO>
  </SCENARIOS>
  <LINKS>
    <LINK rel="requirements" ref="req:common.address-vo"/>
    <LINK rel="plan" ref="plan:common-vo-impl"/>
    <LINK rel="owner" ref="team:core-services"/>
  </LINKS>
</MODULE_CONTRACT>
 */

/**
 * A value object representing a canonical and validated postal address.
 * <p>
 * Validation and normalization are performed upon construction to ensure that
 * any instance of this class is always in a valid state. Optional fields like
 * {@code line2} are canonicalized to {@code null} if they are blank.
 */
public record Address(
  String country,
  String city,
  String postalCode,
  String line1,
  String line2 // This field is optional and will be null if not provided.
) implements Serializable {

  private static final long serialVersionUID = 1L;
/*
  <FN_CONTRACT id="fn.address.constructor" name="Address(canonical constructor)">
  <INTENT>Creates and validates an Address instance. This is the sole entry point for creating an Address.</INTENT>
    <INPUT>
      <PARAM name="country">The country name or code.</PARAM>
      <PARAM name="city">The city or town name.</PARAM>
      <PARAM name="postalCode">The postal or ZIP code.</PARAM>
      <PARAM name="line1">The primary address line (street, number, P.O. box).</PARAM>
      <PARAM name="line2">The optional secondary address line (apartment, suite, etc.). Can be null.</PARAM>
    </INPUT>
  <OUTPUT>A valid, immutable Address instance.</OUTPUT>
    <PRECONDITIONS>
  <PRE>Required fields (country, city, postalCode, line1) must not be null.</PRE>
    </PRECONDITIONS>
    <POSTCONDITIONS>
  <POST>All fields are trimmed, normalized, and validated. An invalid input will result in an exception.</POST>
    </POSTCONDITIONS>
    <ERRORS>
      <ERROR type="IllegalArgumentException">If any validation rule is violated.</ERROR>
      <ERROR type="NullPointerException">If a required field is null.</ERROR>
    </ERRORS>
    <MENTAL_TESTS>
      <CASE name="valid address">Given valid inputs for all fields -> When new Address() -> Then success.</CASE>
      <CASE name="blank required field">Given a blank string for 'city' -> When new Address() -> Then throws IllegalArgumentException.</CASE>
      <CASE name="invalid characters">Given 'line1' with "<script>" -> When new Address() -> Then throws IllegalArgumentException.</CASE>
      <CASE name="field too long">Given a 200-char 'country' -> When new Address() -> Then throws IllegalArgumentException.</CASE>
      <CASE name="null line2">Given null for 'line2' -> When new Address() -> Then 'line2' field is null.</CASE>
      <CASE name="blank line2">Given "   " for 'line2' -> When new Address() -> Then 'line2' field is canonicalized to null.</CASE>
    </MENTAL_TESTS>
  </FN_CONTRACT>
 */
  public Address {
    // Delegate all validation and normalization to the dedicated validator class.
    country = AddressValidator.validateRequired("country", country);
    city = AddressValidator.validateRequired("city", city);
    postalCode = AddressValidator.validatePostalCode(postalCode);
    line1 = AddressValidator.validateRequired("line1", line1);
    line2 = AddressValidator.validateOptional("line2", line2);
  }
/*
  <FN_CONTRACT id="fn.address.toSingleLine" name="toSingleLine">
  <INTENT>Formats the address into a single, comma-separated line for display purposes.</INTENT>
    <INPUT>None.</INPUT>
  <OUTPUT>A single-line string representing the address. Empty or null parts are omitted.</OUTPUT>
    <MENTAL_TESTS>
      <CASE name="full address">Given all fields populated -> When toSingleLine() -> Then returns "line1, line2, city, postalCode, country".</CASE>
      <CASE name="address with no line2">Given line2 is null -> When toSingleLine() -> Then returns "line1, city, postalCode, country".</CASE>
    </MENTAL_TESTS>
  </FN_CONTRACT>
 */
  public String toSingleLine() {
    return Stream.of(line1, line2, city, postalCode, country)
      .filter(part -> part != null && !part.isBlank())
      .collect(Collectors.joining(", "));
  }

  @Override
  public String toString() {
    return "Address[" + toSingleLine() + "]";
  }

  /**
   * Inner class dedicated to handling the validation and normalization logic for an Address.
   * This encapsulates the rules and keeps the Address record clean.
   */
  private static final class AddressValidator {
    private static final int MAX_LENGTH_GENERAL = 120;
    private static final int MAX_LENGTH_POSTAL_CODE = 32;

    // Allows letters, numbers, marks, spaces, and a safe set of common punctuation.
    private static final Pattern GENERAL_FIELD_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\p{M}\\s\\-.,'#/()]+$");
    private static final Pattern POSTAL_CODE_PATTERN = Pattern.compile("^[\\p{L}\\p{N}\\s-]{2," + MAX_LENGTH_POSTAL_CODE + "}$");

    private static String normalize(String input) {
      if (input == null) {
        return null;
      }
      // NFKC normalization is important for security and consistency.
      String normalized = Normalizer.normalize(input.strip(), Normalizer.Form.NFKC);
      // Collapse multiple whitespace characters into a single space.
      return normalized.replaceAll("\\s+", " ");
    }

    static String validateRequired(String fieldName, String value) {
      String normalized = normalize(value);
      if (normalized == null || normalized.isBlank()) {
        throw new IllegalArgumentException(String.format("%s must not be null or blank.", fieldName));
      }
      if (normalized.length() > MAX_LENGTH_GENERAL) {
        throw new IllegalArgumentException(String.format("%s is too long (max %d characters).", fieldName, MAX_LENGTH_GENERAL));
      }
      if (!GENERAL_FIELD_PATTERN.matcher(normalized).matches()) {
        throw new IllegalArgumentException(String.format("%s contains invalid characters.", fieldName));
      }
      return normalized;
    }

    static String validateOptional(String fieldName, String value) {
      String normalized = normalize(value);
      if (normalized == null || normalized.isBlank()) {
        return null; // Canonical representation for an empty optional field is null.
      }
      if (normalized.length() > MAX_LENGTH_GENERAL) {
        throw new IllegalArgumentException(String.format("%s is too long (max %d characters).", fieldName, MAX_LENGTH_GENERAL));
      }
      if (!GENERAL_FIELD_PATTERN.matcher(normalized).matches()) {
        throw new IllegalArgumentException(String.format("%s contains invalid characters.", fieldName));
      }
      return normalized;
    }

    static String validatePostalCode(String value) {
      String normalized = normalize(value);
      if (normalized == null || normalized.isBlank()) {
        throw new IllegalArgumentException("postalCode must not be null or blank.");
      }
      if (!POSTAL_CODE_PATTERN.matcher(normalized).matches()) {
        throw new IllegalArgumentException(String.format("postalCode has an invalid format or length (must be 2-%d alphanumeric characters, spaces, or hyphens).", MAX_LENGTH_POSTAL_CODE));
      }
      return normalized;
    }
  }
}
