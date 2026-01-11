package com.kanokna.search.application.dto;

import com.kanokna.shared.i18n.Language;

/**
 * Query for fetching a product by id.
 */
public record GetProductByIdQuery(String productId, Language language) {
}
