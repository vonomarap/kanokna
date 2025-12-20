package com.kanokna.shared.core;

import java.io.Serializable;
import java.net.IDN;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/*<MODULE_CONTRACT id="mod.common.email-vo" name="Email" layer="domain" version="1.2.0">
<PURPOSE>To provide a canonical, immutable, and validated representation of an email address using a traditional final class for maximum encapsulation.</PURPOSE>
  <RESPONSIBILITIES>
    <RESPONSIBILITY>1. Encapsulate all creation logic (parsing, normalization, validation) within a single static factory method `of()`.</RESPONSIBILITY>
    <RESPONSIBILITY>2. Guarantee instance validity by making the constructor private, forcing all creation through the factory method.</RESPONSIBILITY>
    <RESPONSIBILITY>3. Canonicalize inputs to a consistent format (lowercase, NFKC Unicode normalization, Punycode for domains).</RESPONSIBILITY>
    <RESPONSIBILITY>4. Enforce pragmatic length and format constraints based on common internet standards.</RESPONSIBILITY>
    <RESPONSIBILITY>5. Ensure value-object semantics with correct `equals`, `hashCode`, and `toString` implementations.</RESPONSIBILITY>
  </RESPONSIBILITIES>
  <INVARIANTS>
<INVARIANT>An Email can only be instantiated through the `Email.of()` factory method.</INVARIANT>
<INVARIANT>All internal state (`localPart`, `domain`) is guaranteed to be non-null, canonicalized, and validated before the constructor is called.</INVARIANT>
    <INVARIANT>The class is immutable; its state cannot change after construction.</INVARIANT>
  </INVARIANTS>
  <SCENARIOS>
    <SCENARIO>Creation: A raw string is passed to the `Email.of()` factory, which returns a valid Email instance or throws an exception.</SCENARIO>
    <SCENARIO>Equality: Two Email objects are considered equal if their canonical local parts and domains are equal.</SCENARIO>
  </SCENARIOS>
  <LINKS>
    <LINK rel="requirements" ref="req:common.value-object.email"/>
    <LINK rel="plan" ref="plan:common-vo-impl"/>
    <LINK rel="owner" ref="team:platform-engineering"/>
  </LINKS>
</MODULE_CONTRACT>
*/
/**
 * A value object representing a canonical email address.
 * <p>
 * This class ensures that any instance is always valid by centralizing all parsing,
 * normalization, and validation logic within the static factory method {@link #of(String)}.
 * The constructor is private to prevent unvalidated instantiation.
 */
public final class Email implements Serializable {

  private static final long serialVersionUID = 1L;
  private static final int MAX_TOTAL_LENGTH = 254;
  private static final int MAX_LOCAL_PART_LENGTH = 64;

  // A pragmatic pattern for the local part. It is a strict subset of RFC 5322 for security and simplicity.
  private static final Pattern LOCAL_PART_PATTERN =
    Pattern.compile("^(?!\\.)(?!.*\\.{2})[a-z0-9!#$%&'*+/=?^_`{|}~.-]{1," + MAX_LOCAL_PART_LENGTH + "}(?<!\\.)$");

  // A pattern for a valid domain name, designed to check the final ASCII/Punycode form.
  // The positive lookahead (?=...) checks total domain length before matching segments.
  private static final Pattern DOMAIN_PATTERN =
    Pattern.compile("^(?=.{1,253}$)(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z0-9][a-z0-9-]{0,61}[a-z0-9]$");

  private final String localPart;
  private final String domain;

  /*<FN_CONTRACT id="fn.email.constructor.private" name="Email">
  <INTENT>Private constructor for internal use by the factory method. Assumes all inputs are pre-validated and canonical.</INTENT>
    <INPUT>
      <PARAM name="localPart">The validated and canonical local part.</PARAM>
      <PARAM name="domain">The validated and canonical (ASCII/Punycode) domain.</PARAM>
    </INPUT>
    <POSTCONDITIONS>
      <POST>A new immutable Email instance is created.</POST>
    </POSTCONDITIONS>
  </FN_CONTRACT>
   */
  private Email(String localPart, String domain) {
    this.localPart = localPart;
    this.domain = domain;
  }
/*
  <FN_CONTRACT id="fn.email.of" name="of">
  <INTENT>The sole factory method for creating an Email instance from a raw string.</INTENT>
    <INPUT>
      <PARAM name="rawEmail">The raw string representation of the email address.</PARAM>
    </INPUT>
  <OUTPUT>A valid, immutable Email instance.</OUTPUT>
    <PRECONDITIONS>
  <PRE>rawEmail must not be null.</PRE>
    </PRECONDITIONS>
    <POSTCONDITIONS>
  <POST>The returned Email object is guaranteed to be in a valid and canonical state.</POST>
    </POSTCONDITIONS>
    <ERRORS>
      <ERROR type="NullPointerException">If rawEmail is null.</ERROR>
      <ERROR type="IllegalArgumentException">If rawEmail is malformed or fails validation checks.</ERROR>
    </ERRORS>
    <MENTAL_TESTS>
      <CASE name="simple valid email">Given " contact@example.com " -> When of() -> Then returns Email("contact", "example.com").</CASE>
      <CASE name="internationalized email">Given "JÖRN@räksmörgås.org" -> When of() -> Then returns Email("jörn", "xn--rksmrgs-5wao1o.org").</CASE>
      <CASE name="invalid format">Given "no-at-symbol" -> When of() -> Then throws IllegalArgumentException.</CASE>
      <CASE name="invalid local part">Given "..dots@test.com" -> When of() -> Then throws IllegalArgumentException.</CASE>
      <CASE name="invalid domain">Given "test@.invalid-domain.com" -> When of() -> Then throws IllegalArgumentException.</CASE>
    </MENTAL_TESTS>
    <LINKS>
      <LINK rel="spec" ref="https://tools.ietf.org/html/rfc5322"/>
      <LINK rel="spec" ref="https://tools.ietf.org/html/rfc5890"/>
    </LINKS>
  </FN_CONTRACT>
 */
  public static Email of(String rawEmail) {
    Objects.requireNonNull(rawEmail, "Email string must not be null");
    String strippedEmail = rawEmail.strip();
    int atIndex = strippedEmail.lastIndexOf('@');

    if (atIndex <= 0 || atIndex == strippedEmail.length() - 1) {
      throw new IllegalArgumentException("Email must contain a single '@' separating a non-empty local part and domain.");
    }

    // 1. Parse into raw parts.
    String rawLocalPart = strippedEmail.substring(0, atIndex);
    String rawDomain = strippedEmail.substring(atIndex + 1);

    // 2. Canonicalize parts: Normalize, lowercase, and convert domain to Punycode.
    String canonicalLocalPart = Normalizer.normalize(rawLocalPart, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
    String normalizedDomain = Normalizer.normalize(rawDomain, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT);
    String asciiDomain = IDN.toASCII(normalizedDomain, IDN.ALLOW_UNASSIGNED);

    // 3. Validate the canonical parts.
    if (!LOCAL_PART_PATTERN.matcher(canonicalLocalPart).matches()) {
      throw new IllegalArgumentException("Invalid email local part.");
    }

    if (!DOMAIN_PATTERN.matcher(asciiDomain).matches()) {
      throw new IllegalArgumentException("Invalid email domain.");
    }

    if ((canonicalLocalPart.length() + 1 + asciiDomain.length()) > MAX_TOTAL_LENGTH) {
      throw new IllegalArgumentException("Email exceeds maximum total length of " + MAX_TOTAL_LENGTH + " characters.");
    }

    // 4. If all checks pass, create the instance.
    return new Email(canonicalLocalPart, asciiDomain);
  }

  public String localPart() {
    return localPart;
  }

  public String domain() {
    return domain;
  }
/*
  <FN_CONTRACT id="fn.email.asString" name="asString">
  <INTENT>Returns the full, canonical string representation of the email address.</INTENT>
  <OUTPUT>The canonical email string (e.g., "user@example.com").</OUTPUT>
  </FN_CONTRACT>
 */
  public String asString() {
    return localPart + "@" + domain;
  }
/*
  <FN_CONTRACT id="fn.email.masked" name="masked">
  <INTENT>Provides a masked version of the email for safe display in logs or UIs.</INTENT>
  <OUTPUT>A masked email string (e.g., "j***n@example.com").</OUTPUT>
  </FN_CONTRACT>
 */
  public String masked() {
    if (localPart.length() <= 2) {
      return "***@" + domain;
    }
    return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1) + "@" + domain;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Email email = (Email) o;
    return localPart.equals(email.localPart) && domain.equals(email.domain);
  }

  @Override
  public int hashCode() {
    return Objects.hash(localPart, domain);
  }

  @Override
  public String toString() {
    return asString();
  }
}

