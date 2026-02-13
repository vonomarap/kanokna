package com.kanokna.shared.i18n;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

@DisplayName("LocalizedString")
class LocalizedStringTest {

    @Test
    @DisplayName("resolve returns exact translation when language exists")
    void resolveReturnsExactTranslation() {
        LocalizedString text = LocalizedString.of(Map.of(Language.EN, "Hello", Language.RU, "Privet"));

        assertThat(text.resolve(Language.RU)).isEqualTo("Privet");
    }

    @Test
    @DisplayName("resolve falls back to EN when requested language is missing")
    void resolveFallsBackToEnglish() {
        LocalizedString text = LocalizedString.of(Map.of(Language.EN, "Hello", Language.RU, "Privet"));

        assertThat(text.resolve(Language.DE)).isEqualTo("Hello");
    }

    @Test
    @DisplayName("resolve with null language uses fallback")
    void resolveWithNullLanguageUsesFallback() {
        LocalizedString text = LocalizedString.of(Map.of(Language.EN, "Hello", Language.FR, "Bonjour"));

        assertThat(text.resolve(null)).isEqualTo("Hello");
    }

    @Test
    @DisplayName("resolve returns only available translation when EN is absent")
    void resolveReturnsOnlyAvailableTranslationWhenEnglishAbsent() {
        LocalizedString text = LocalizedString.of(Map.of(Language.DE, "Hallo"));

        assertThat(text.resolve(Language.RU)).isEqualTo("Hallo");
    }

    @Test
    @DisplayName("builder add and addAll build immutable translations")
    void builderAddAndAddAllBuildImmutableTranslations() {
        LocalizedString text = LocalizedString.builder()
            .add(Language.EN, "Hello")
            .addAll(Map.of(Language.FR, "Bonjour"))
            .build();

        assertThat(text.resolve(Language.EN)).isEqualTo("Hello");
        assertThat(text.resolve(Language.FR)).isEqualTo("Bonjour");
        assertThatExceptionOfType(UnsupportedOperationException.class)
            .isThrownBy(() -> text.asMap().put(Language.DE, "Hallo"));
    }

    @Test
    @DisplayName("with adds or replaces translation without mutating original")
    void withAddsOrReplacesTranslationWithoutMutatingOriginal() {
        LocalizedString original = LocalizedString.of(Language.EN, "Hello");

        LocalizedString updated = original.with(Language.DE, "Hallo");

        assertThat(original.hasLanguage(Language.DE)).isFalse();
        assertThat(updated.hasLanguage(Language.DE)).isTrue();
        assertThat(updated.resolve(Language.DE)).isEqualTo("Hallo");
    }

    @Test
    @DisplayName("without removes translation and keeps instance immutable")
    void withoutRemovesTranslationAndKeepsImmutable() {
        LocalizedString original = LocalizedString.of(Map.of(Language.EN, "Hello", Language.DE, "Hallo"));

        LocalizedString updated = original.without(Language.DE);

        assertThat(original.hasLanguage(Language.DE)).isTrue();
        assertThat(updated.hasLanguage(Language.DE)).isFalse();
        assertThat(updated.resolve(Language.RU)).isEqualTo("Hello");
    }

    @Test
    @DisplayName("without rejects removing last translation")
    void withoutRejectsRemovingLastTranslation() {
        LocalizedString onlyOne = LocalizedString.of(Language.EN, "Hello");

        assertThatIllegalArgumentException()
            .isThrownBy(() -> onlyOne.without(Language.EN))
            .withMessage("Cannot remove last translation");
    }

    @Test
    @DisplayName("factory rejects empty map")
    void factoryRejectsEmptyMap() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> LocalizedString.of(Map.of()))
            .withMessage("At least one translation required");
    }

    @Test
    @DisplayName("factory rejects blank text")
    void factoryRejectsBlankText() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> LocalizedString.of(Language.EN, "   "))
            .withMessageContaining("Text cannot be null or blank");
    }
}
