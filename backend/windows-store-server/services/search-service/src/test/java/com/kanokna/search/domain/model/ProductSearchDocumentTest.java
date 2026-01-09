package com.kanokna.search.domain.model;

import com.kanokna.shared.i18n.Language;
import com.kanokna.shared.i18n.LocalizedString;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductSearchDocumentTest {
    @Test
    @DisplayName("TC-FUNC-INDEX-001: ProductSearchDocument builder maps fields correctly")
    void builder_setsFields() {
        ProductSearchDocument document = ProductSearchDocument.builder("prod-1")
            .name(LocalizedString.of(Language.RU, "Window"))
            .description(LocalizedString.of(Language.RU, "Description"))
            .family("WINDOW")
            .profileSystem("REHAU")
            .openingTypes(List.of("TILT"))
            .materials(List.of("PVC"))
            .colors(List.of("WHITE"))
            .minPrice(Money.ofMinor(100_00, Currency.RUB))
            .maxPrice(Money.ofMinor(200_00, Currency.RUB))
            .currency("RUB")
            .popularity(5)
            .status(ProductStatus.ACTIVE)
            .publishedAt(Instant.now())
            .thumbnailUrl("http://example.com/1.png")
            .optionCount(2)
            .suggestInputs(List.of("Window"))
            .score(1.2f)
            .highlights(Map.of("name", "<em>Window</em>"))
            .build();

        assertEquals("prod-1", document.getId());
        assertEquals("WINDOW", document.getFamily());
        assertEquals(ProductStatus.ACTIVE, document.getStatus());
        assertNotNull(document.getMinPrice());
        assertEquals(2, document.getOptionCount());
        assertTrue(document.getHighlights().containsKey("name"));
    }

    @Test
    @DisplayName("TC-FUNC-INDEX-002: ProductSearchDocument defaults to empty collections and UNSPECIFIED status")
    void defaults_whenNullCollections() {
        ProductSearchDocument document = ProductSearchDocument.builder("prod-2")
            .build();

        assertEquals(ProductStatus.UNSPECIFIED, document.getStatus());
        assertTrue(document.getOpeningTypes().isEmpty());
        assertTrue(document.getMaterials().isEmpty());
        assertTrue(document.getColors().isEmpty());
        assertTrue(document.getSuggestInputs().isEmpty());
    }
}
