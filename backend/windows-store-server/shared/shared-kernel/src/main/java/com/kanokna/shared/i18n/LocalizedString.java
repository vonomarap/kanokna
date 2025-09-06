package com.kanokna.shared.i18n;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Immutable language-tagged string container with intelligent fallback resolution.
 * 
 * Example usage:
 * var text = LocalizedString.builder()
 *     .add(Language.EN, "Hello World")
 *     .add(Language.RU, "Привет Мир")
 *     .build();
 * 
 * String greeting = text.resolve(Language.DE); // Falls back to "Hello World"
 */
public final class LocalizedString {
    
    private static final List<Language> FALLBACK_ORDER = List.of(Language.EN);
    
    private final EnumMap<Language, String> translations;
    
    private LocalizedString(Map<Language, String> values) {
        this.translations = validateAndCopy(values);
    }
    
    // Factory methods
    public static LocalizedString of(Map<Language, String> values) {
        return new LocalizedString(values);
    }
    
    public static LocalizedString of(Language language, String text) {
        return new LocalizedString(Map.of(language, text));
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    // Core methods
    public String resolve(Language language) {
        return Optional.ofNullable(language)
                .map(translations::get)
                .or(this::findFallback)
                .orElseThrow(() -> new IllegalStateException("No translations available"));
    }
    
    public LocalizedString with(Language language, String text) {
        validateEntry(language, text);
        var newTranslations = new EnumMap<>(translations);
        newTranslations.put(language, text);
        return new LocalizedString(newTranslations);
    }
    
    public LocalizedString without(Language language) {
        if (translations.size() <= 1) {
            throw new IllegalArgumentException("Cannot remove last translation");
        }
        var newTranslations = new EnumMap<>(translations);
        newTranslations.remove(language);
        return new LocalizedString(newTranslations);
    }
    
    public Set<Language> availableLanguages() {
        return EnumSet.copyOf(translations.keySet());
    }
    
    public boolean hasLanguage(Language language) {
        return translations.containsKey(language);
    }
    
    public Map<Language, String> asMap() {
        return Collections.unmodifiableMap(translations);
    }
    
    // Private helper methods
    private Optional<String> findFallback() {
        return FALLBACK_ORDER.stream()
                .map(translations::get)
                .filter(Objects::nonNull)
                .findFirst()
                .or(() -> translations.values().stream().findFirst());
    }
    
    private static EnumMap<Language, String> validateAndCopy(Map<Language, String> values) {
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("At least one translation required");
        }
        
        var result = new EnumMap<Language, String>(Language.class);
        values.entrySet().stream()
                .peek(entry -> validateEntry(entry.getKey(), entry.getValue()))
                .forEach(entry -> result.put(entry.getKey(), entry.getValue()));
        
        return result;
    }
    
    private static void validateEntry(Language language, String text) {
        if (language == null) {
            throw new IllegalArgumentException("Language cannot be null");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text cannot be null or blank for language: " + language);
        }
    }
    
    // Builder pattern
    public static final class Builder {
        private final Map<Language, String> values = new EnumMap<>(Language.class);
        
        public Builder add(Language language, String text) {
            validateEntry(language, text);
            values.put(language, text);
            return this;
        }
        
        public Builder addAll(Map<Language, String> translations) {
            translations.forEach(this::add);
            return this;
        }
        
        public LocalizedString build() {
            return new LocalizedString(values);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof LocalizedString other && 
            Objects.equals(this.translations, other.translations);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(translations);
    }
    
    @Override
    public String toString() {
        return translations.entrySet().stream()
                .map(entry -> entry.getKey() + "='" + entry.getValue() + "'")
                .collect(Collectors.joining(", ", "LocalizedString{", "}"));
    }
}