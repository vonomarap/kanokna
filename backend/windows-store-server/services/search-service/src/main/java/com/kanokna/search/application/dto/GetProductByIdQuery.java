package com.kanokna.search.application.dto;

import com.kanokna.shared.i18n.Language;

/**
 * Query for fetching a product by id.
 */
public class GetProductByIdQuery {
    private final String productId;
    private final Language language;

    public GetProductByIdQuery(String productId, Language language) {
        this.productId = productId;
        this.language = language;
    }

    public String getProductId() {
        return productId;
    }

    public Language getLanguage() {
        return language;
    }
}
