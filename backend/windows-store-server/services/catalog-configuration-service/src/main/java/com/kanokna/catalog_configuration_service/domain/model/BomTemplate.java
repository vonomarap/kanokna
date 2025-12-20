package com.kanokna.catalog_configuration_service.domain.model;

import com.kanokna.catalog_configuration_service.domain.event.BomResolvedEvent;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class BomTemplate {
    private final String code;
    private final Map<String, String> optionMatchers;
    private final List<BomItem> items;

    public BomTemplate(String code, Map<String, String> optionMatchers, List<BomItem> items) {
        this.code = Objects.requireNonNull(code, "code");
        this.optionMatchers = Collections.unmodifiableMap(normalize(optionMatchers));
        this.items = Collections.unmodifiableList(List.copyOf(items));
    }

    public String code() {
        return code;
    }

    public boolean matches(ConfigurationSelection selection) {
        return optionMatchers.entrySet().stream()
            .allMatch(entry -> entry.getValue().equals(selection.optionSelections().get(entry.getKey())));
    }

    public List<BomItem> items() {
        return items;
    }

    private static Map<String, String> normalize(Map<String, String> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        return source.entrySet().stream()
            .filter(entry -> entry.getKey() != null && !entry.getKey().isBlank() && entry.getValue() != null && !entry.getValue().isBlank())
            .collect(Collectors.toUnmodifiableMap(
                entry -> entry.getKey().trim(),
                entry -> entry.getValue().trim()
            ));
    }

    public record BomItem(String sku, int quantity, String unit) {
        public BomItem {
            if (sku == null || sku.isBlank()) {
                throw new IllegalArgumentException("sku is required");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("quantity must be positive");
            }
            unit = unit == null || unit.isBlank() ? "pcs" : unit.trim();
        }
    }

    public static final class BomResolutionResult {
        private final List<BomItem> items;
        private final BomResolvedEvent bomResolvedEvent;

        public BomResolutionResult(List<BomItem> items, BomResolvedEvent bomResolvedEvent) {
            this.items = Collections.unmodifiableList(List.copyOf(items));
            this.bomResolvedEvent = bomResolvedEvent;
        }

        public List<BomItem> items() {
            return items;
        }

        public Optional<BomResolvedEvent> bomResolvedEvent() {
            return Optional.ofNullable(bomResolvedEvent);
        }

        public static BomResolutionResult merged(List<BomItem> items, BomResolvedEvent event) {
            Map<String, BomItem> merged = new LinkedHashMap<>();
            for (BomItem item : items) {
                BomItem existing = merged.get(item.sku());
                if (existing == null) {
                    merged.put(item.sku(), item);
                } else {
                    merged.put(item.sku(), new BomItem(item.sku(), existing.quantity() + item.quantity(), item.unit()));
                }
            }
            return new BomResolutionResult(List.copyOf(merged.values()), event);
        }
    }
}
