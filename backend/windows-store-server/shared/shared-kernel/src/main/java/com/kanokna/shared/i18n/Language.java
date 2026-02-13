package com.kanokna.shared.i18n;

public enum Language {
    EN("English", "en"),
    RU("Russian", "ru"),
    DE("German", "de"),
    FR("French", "fr");

    private final String displayName;
    private final String localeTag;

    Language(String displayName, String localeTag) {
        this.displayName = displayName;
        this.localeTag = localeTag;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getLocaleTag() {
        return localeTag;
    }
}
