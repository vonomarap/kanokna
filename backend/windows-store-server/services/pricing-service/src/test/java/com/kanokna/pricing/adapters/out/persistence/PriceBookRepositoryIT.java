package com.kanokna.pricing.adapters.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import com.kanokna.pricing.domain.model.BasePriceEntry;
import com.kanokna.pricing.domain.model.Money;
import com.kanokna.pricing.domain.model.OptionPremium;
import com.kanokna.pricing.domain.model.PriceBook;
import com.kanokna.pricing.domain.model.PriceBookId;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PriceBookRepositoryIT {

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("pricing")
            .withUsername("pricing")
            .withPassword("pricing");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        if (!postgres.isRunning()) {
            postgres.start();
        }
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.schemas", () -> "pricing");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "pricing");
    }

    @AfterAll
    static void stopContainer() {
        if (postgres.isRunning()) {
            postgres.stop();
        }
    }

    @Autowired
    private PriceBookJpaRepository repository;

    private PriceBookRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new PriceBookRepositoryAdapter(repository, new PriceBookMapper());
    }

    @Test
    @DisplayName("Save and find active price book")
    void saveAndFindActivePriceBook() {
        PriceBook priceBook = priceBook();
        adapter.save(priceBook);

        Optional<PriceBook> found = adapter.findActiveByProductTemplateId("WINDOW-STD");

        assertTrue(found.isPresent());
        assertEquals("WINDOW-STD", found.get().getProductTemplateId());
        assertEquals("RUB", found.get().getCurrency());
        assertEquals(1, found.get().getOptionPremiums().size());
        assertEquals(1, found.get().getVersion());
    }

    private PriceBook priceBook() {
        BasePriceEntry basePriceEntry = BasePriceEntry.of(
                "WINDOW-STD",
                new BigDecimal("1000"),
                new BigDecimal("0.25"),
                null);
        PriceBook priceBook = PriceBook.create(
                PriceBookId.generate(),
                "WINDOW-STD",
                "RUB",
                basePriceEntry,
                "tester");
        priceBook.addOptionPremium(
                OptionPremium.absolute("OPT-A", "Handle", Money.of(new BigDecimal("50"), "RUB")));
        priceBook.publish();
        return priceBook;
    }
}
