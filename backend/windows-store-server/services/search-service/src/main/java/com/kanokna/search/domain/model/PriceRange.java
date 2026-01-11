package com.kanokna.search.domain.model;

import com.kanokna.shared.money.Money;

/**
 * Price range filter using Money values.
 */
public record PriceRange(Money minPrice, Money maxPrice) {
}
