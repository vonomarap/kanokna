package com.kanokna.shared.core;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A value object representing a validated phone number.
 * <p>
 * This class performs basic validation using a simple regex pattern.
 * For production use, consider integrating with a phone number library
 * like libphonenumber for carrier validation and formatting.
 *
 * <h2>Validation Rules:</h2>
 * <ul>
 *   <li>Must contain 7-15 digits</li>
 *   <li>May start with optional + prefix</li>
 *   <li>Spaces, dashes, and parentheses are stripped during normalization</li>
 * </ul>
 *
 * @see <a href="Handoff-20251230-02#SK-ASSUM-002">SK-ASSUM-002: Simple regex validation</a>
 */
public final class PhoneNumber implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Minimum number of digits required.
     */
    private static final int MIN_DIGITS = 7;

    /**
     * Maximum number of digits allowed (E.164 standard).
     */
    private static final int MAX_DIGITS = 15;

    /**
     * Pattern for extracting digits from input.
     */
    private static final Pattern NON_DIGIT_PATTERN = Pattern.compile("[^0-9]");

    /**
     * Pattern for valid input characters (digits, plus, spaces, dashes, parens, dots).
     */
    private static final Pattern VALID_INPUT_PATTERN = Pattern.compile("^\\+?[0-9\\s\\-().]+$");

    /**
     * The normalized phone number (digits only, possibly with leading +).
     */
    private final String number;

    /**
     * Private constructor - use factory method {@link #of(String)}.
     */
    private PhoneNumber(String number) {
        this.number = number;
    }

    /**
     * Creates a PhoneNumber from a raw string input.
     * <p>
     * The input is normalized by:
     * <ol>
     *   <li>Trimming whitespace</li>
     *   <li>Validating allowed characters</li>
     *   <li>Extracting digits (preserving leading +)</li>
     *   <li>Validating digit count (7-15)</li>
     * </ol>
     *
     * @param rawNumber the raw phone number string
     * @return a validated PhoneNumber instance
     * @throws NullPointerException     if rawNumber is null
     * @throws IllegalArgumentException if the phone number is invalid
     */
    public static PhoneNumber of(String rawNumber) {
        Objects.requireNonNull(rawNumber, "Phone number must not be null");

        String stripped = rawNumber.strip();
        if (stripped.isEmpty()) {
            throw new IllegalArgumentException("Phone number must not be empty");
        }

        // Validate input contains only allowed characters
        if (!VALID_INPUT_PATTERN.matcher(stripped).matches()) {
            throw new IllegalArgumentException("Phone number contains invalid characters");
        }

        // Check for leading plus
        boolean hasPlus = stripped.startsWith("+");

        // Extract only digits
        String digitsOnly = NON_DIGIT_PATTERN.matcher(stripped).replaceAll("");

        // Validate digit count
        if (digitsOnly.length() < MIN_DIGITS) {
            throw new IllegalArgumentException(
                "Phone number must have at least %d digits, got %d"
                    .formatted(MIN_DIGITS, digitsOnly.length()));
        }

        if (digitsOnly.length() > MAX_DIGITS) {
            throw new IllegalArgumentException(
                "Phone number must have at most %d digits, got %d"
                    .formatted(MAX_DIGITS, digitsOnly.length()));
        }

        // Build normalized number
        String normalized = hasPlus ? "+" + digitsOnly : digitsOnly;
        return new PhoneNumber(normalized);
    }

    /**
     * Returns the normalized phone number string.
     *
     * @return the normalized number (digits with optional + prefix)
     */
    public String value() {
        return number;
    }

    /**
     * Returns a masked version for safe display in logs or UIs.
     * <p>
     * Example: "+79123456789" becomes "+7912***6789"
     *
     * @return masked phone number
     */
    public String masked() {
        if (number.length() <= 6) {
            return "***";
        }

        int visibleStart = number.startsWith("+") ? 5 : 4;
        int visibleEnd = 4;

        if (number.length() <= visibleStart + visibleEnd) {
            return number.substring(0, 2) + "***" + number.substring(number.length() - 2);
        }

        return number.substring(0, visibleStart) + "***" + number.substring(number.length() - visibleEnd);
    }

    /**
     * Returns the phone number in E.164 format if it has a + prefix.
     *
     * @return E.164 formatted number or the normalized number if no + prefix
     */
    public String toE164() {
        return number.startsWith("+") ? number : "+" + number;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhoneNumber that = (PhoneNumber) o;
        return number.equals(that.number);
    }

    @Override
    public int hashCode() {
        return Objects.hash(number);
    }

    @Override
    public String toString() {
        return number;
    }
}
