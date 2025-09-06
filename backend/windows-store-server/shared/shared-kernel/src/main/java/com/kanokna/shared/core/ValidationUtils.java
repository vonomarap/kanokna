package com.kanokna.shared.core;

import java.util.Arrays;

public class ValidationUtils {

    private ValidationUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean hasNonPositiveValues(Integer... values) {
        return Arrays.stream(values).allMatch(val -> val <= 0);
    }
}
