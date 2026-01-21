package com.kanokna.test.fixtures;

import java.time.Instant;

/**
 * Shared default values for search test fixtures.
 */
public final class SearchFixtureDefaults {
    public static final String EVENT_ID_PREFIX = "event-";
    public static final String EVENT_TYPE_PUBLISHED = "PRODUCT_TEMPLATE_PUBLISHED";
    public static final String PRODUCT_NAME_PREFIX = "Window ";
    public static final String PRODUCT_DESCRIPTION_PREFIX = "Description ";
    public static final String PRODUCT_FAMILY = "WINDOW";
    public static final String PROFILE_SYSTEM = "REHAU";
    public static final String OPENING_TYPE = "TILT";
    public static final String MATERIAL = "PVC";
    public static final String COLOR = "WHITE";
    public static final String CURRENCY_CODE = "RUB";
    public static final String THUMBNAIL_BASE_URL = "http://example.com/";
    public static final long MIN_PRICE_MINOR = 100_00L;
    public static final long MAX_PRICE_MINOR = 250_00L;
    public static final int POPULARITY = 5;
    public static final int OPTION_GROUP_COUNT = 3;
    public static final int DOCUMENT_POPULARITY = 10;
    public static final int OPTION_COUNT = 2;
    public static final float SCORE = 1.0f;
    public static final String HIGHLIGHT_NAME = "<em>Window</em>";
    public static final int TOTAL_PAGES = 1;
    public static final int QUERY_TIME_MS = 12;
    public static final int SUGGESTION_WEIGHT = 1;

    private SearchFixtureDefaults() {
    }

    public static Instant now() {
        return Instant.now();
    }
}
