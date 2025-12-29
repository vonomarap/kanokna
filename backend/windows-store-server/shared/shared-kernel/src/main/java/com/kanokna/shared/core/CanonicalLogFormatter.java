package com.kanokna.shared.core;

import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public final class CanonicalLogFormatter {
    private CanonicalLogFormatter() {
        throw new IllegalStateException("Utility class");
    }

    public static String format(String service, String useCase, String block, String state,
                                String eventType, String decision, Map<String, ?> keyValues) {
        StringBuilder builder = new StringBuilder();
        builder.append("[SVC=").append(requireNonBlank(service, "service")).append("]")
          .append("[UC=").append(requireNonBlank(useCase, "useCase")).append("]")
          .append("[BLOCK=").append(requireNonBlank(block, "block")).append("]")
          .append("[STATE=").append(requireNonBlank(state, "state")).append("]")
          .append(" eventType=").append(requireNonBlank(eventType, "eventType"))
          .append(" decision=").append(requireNonBlank(decision, "decision"));

        String rendered = renderKeyValues(keyValues);
        if (!rendered.isEmpty()) {
            builder.append(" ").append(rendered);
        }

        return builder.toString();
    }

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must be non-blank");
        }
        return value;
    }

    private static String renderKeyValues(Map<String, ?> keyValues) {
        if (keyValues == null || keyValues.isEmpty()) {
            return "";
        }

        StringJoiner joiner = new StringJoiner(" ");
        for (Map.Entry<String, ?> entry : keyValues.entrySet()) {
            String key = requireNonBlank(entry.getKey(), "key");
            joiner.add(key + "=" + Objects.toString(entry.getValue()));
        }
        return joiner.toString();
    }
}
