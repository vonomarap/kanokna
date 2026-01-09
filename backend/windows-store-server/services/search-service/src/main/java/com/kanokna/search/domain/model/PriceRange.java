package com.kanokna.search.domain.model;

import com.kanokna.shared.money.Money;

/**
 * Price range filter using Money values.
 */
public class PriceRange {
    private final Money minPrice;
    private final Money maxPrice;

    public PriceRange(Money minPrice, Money maxPrice) {
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    public Money getMinPrice() {
        return minPrice;
    }

    public Money getMaxPrice() {
        return maxPrice;
    }
}
