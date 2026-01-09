package com.kanokna.search.domain.model;

import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceRangeTest {
    @Test
    @DisplayName("TC-FUNC-SEARCH-004: Price range filter stores min/max values")
    void priceRange_storesMinAndMax() {
        Money min = Money.ofMinor(100_00, Currency.RUB);
        Money max = Money.ofMinor(500_00, Currency.RUB);

        PriceRange range = new PriceRange(min, max);

        assertEquals(min, range.getMinPrice());
        assertEquals(max, range.getMaxPrice());
    }
}
