package com.kanokna.shared.logging;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.owasp.encoder.Encode;

/**
 * Utility for sanitizing user-provided input before logging to prevent log injection.
 */
/* <MODULE_CONTRACT id="MC-shared-kernel-logging-LogSanitizer">
     <Purpose>
       Utility for sanitizing user-provided input before logging to prevent log injection
       attacks. Wraps OWASP Java Encoder for comprehensive output encoding.
     </Purpose>
     <Responsibilities>
       <Item>Sanitize strings to prevent newline injection (CRLF)</Item>
       <Item>Sanitize strings to prevent ANSI escape sequence injection</Item>
       <Item>Sanitize strings to prevent Unicode control character injection</Item>
       <Item>Provide bulk sanitization for maps/collections of log parameters</Item>
     </Responsibilities>
     <Invariants>
       <Item>Output is always safe to include in log statements</Item>
       <Item>Normal alphanumeric strings pass through unchanged</Item>
       <Item>Null input returns "[null]" string (never throws NPE)</Item>
       <Item>Empty input returns empty string</Item>
     </Invariants>
     <Dependencies>
       <Item>org.owasp.encoder:encoder:1.2.3</Item>
     </Dependencies>
     <LINKS>
       <Link ref="Technology.xml#DEC-SEC-LOG-SANITIZATION"/>
       <Link ref="DevelopmentPlan.xml#DP-SVC-shared-kernel"/>
     </LINKS>
   </MODULE_CONTRACT> */
public final class LogSanitizer {

    private static final Pattern ANSI_ESCAPE_PATTERN = Pattern.compile("\\x1B\\[[0-9;]*[a-zA-Z]");
    private static final String NULL_TOKEN = "[null]";

    private LogSanitizer() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Sanitizes a string for safe logging.
     *
     * @param input the input string (may be null)
     * @return sanitized string safe for logging, or "[null]" if input is null
     */
    /* <FUNCTION_CONTRACT id="FC-LogSanitizer-sanitize">
         <Method>public static String sanitize(String input)</Method>
         <Intent>Sanitize a single string for safe logging</Intent>
         <Input>String input - potentially malicious user input</Input>
         <Output>String - sanitized string safe for logging</Output>
         <SideEffects>None (pure function)</SideEffects>
         <Preconditions>None</Preconditions>
         <Postconditions>
           <Item>Return value contains no CR, LF, TAB, or ANSI escape sequences</Item>
           <Item>Return value is non-null</Item>
         </Postconditions>
         <ErrorHandling>
           <Item>Null input returns "[null]"</Item>
           <Item>Never throws exceptions</Item>
         </ErrorHandling>
         <TEST_CASES>
           <TestCase id="TC-LOG-SANITIZE-001">Input with newlines returns string with newlines replaced</TestCase>
           <TestCase id="TC-LOG-SANITIZE-002">Input with ANSI escapes returns string with escapes removed</TestCase>
           <TestCase id="TC-LOG-SANITIZE-003">Normal string passes through unchanged</TestCase>
           <TestCase id="TC-LOG-SANITIZE-004">Null input returns "[null]"</TestCase>
         </TEST_CASES>
         <LINKS>
           <Link ref="MC-shared-kernel-logging-LogSanitizer"/>
         </LINKS>
       </FUNCTION_CONTRACT> */
    public static String sanitize(String input) {
        if (input == null) {
            return NULL_TOKEN;
        }

        String encoded = Encode.forJava(input);
        return ANSI_ESCAPE_PATTERN.matcher(encoded).replaceAll("");
    }

    /**
     * Sanitizes all values in a map for safe structured logging.
     *
     * @param input the input map (may be null)
     * @return new map with sanitized values, or empty map if input is null
     */
    /* <FUNCTION_CONTRACT id="FC-LogSanitizer-sanitizeMap">
         <Method>public static Map&lt;String, String&gt; sanitizeMap(Map&lt;String, ?&gt; input)</Method>
         <Intent>Sanitize all values in a map for safe structured logging</Intent>
         <Input>Map with string keys and any values (toString() called on values)</Input>
         <Output>New map with all values sanitized</Output>
         <SideEffects>None (returns new map, does not modify input)</SideEffects>
         <Preconditions>None</Preconditions>
         <Postconditions>
           <Item>All values in returned map are sanitized</Item>
           <Item>Original map is not modified</Item>
         </Postconditions>
         <ErrorHandling>
           <Item>Null input returns empty map</Item>
           <Item>Null values in map converted to "[null]"</Item>
         </ErrorHandling>
         <LINKS>
           <Link ref="MC-shared-kernel-logging-LogSanitizer"/>
         </LINKS>
       </FUNCTION_CONTRACT> */
    public static Map<String, String> sanitizeMap(Map<String, ?> input) {
        if (input == null) {
            return Map.of();
        }

        Map<String, String> sanitized = new LinkedHashMap<>();
        for (Map.Entry<String, ?> entry : input.entrySet()) {
            String key = sanitize(entry.getKey());
            String value = entry.getValue() == null ? NULL_TOKEN : sanitize(entry.getValue().toString());
            sanitized.put(key, value);
        }
        return sanitized;
    }
}
