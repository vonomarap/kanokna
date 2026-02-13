package com.kanokna.shared.i18n;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Language")
class LanguageTest {

    @Test
    @DisplayName("languages expose expected display names")
    void languagesExposeExpectedDisplayNames() {
        assertThat(Language.EN.getDisplayName()).isEqualTo("English");
        assertThat(Language.RU.getDisplayName()).isEqualTo("Russian");
        assertThat(Language.DE.getDisplayName()).isEqualTo("German");
        assertThat(Language.FR.getDisplayName()).isEqualTo("French");
    }

    @Test
    @DisplayName("languages expose expected locale tags")
    void languagesExposeExpectedLocaleTags() {
        assertThat(Language.EN.getLocaleTag()).isEqualTo("en");
        assertThat(Language.RU.getLocaleTag()).isEqualTo("ru");
        assertThat(Language.DE.getLocaleTag()).isEqualTo("de");
        assertThat(Language.FR.getLocaleTag()).isEqualTo("fr");
    }
}
