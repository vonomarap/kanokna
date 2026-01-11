package com.kanokna.cart.adapters.out.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class CartSnapshotJpaRepositoryTest {
    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("cart")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "cart");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.schemas", () -> "cart");
        registry.add("spring.cloud.config.enabled", () -> "false");
    }

    @Autowired
    private CartSnapshotJpaRepository cartSnapshotJpaRepository;

    @Test
    @DisplayName("TC-FUNC-CART-SNAP-001: save and load snapshot entity")
    void saveAndLoadSnapshot() {
        CartSnapshotJpaEntity snapshot = buildSnapshot();
        cartSnapshotJpaRepository.saveAndFlush(snapshot);

        CartSnapshotJpaEntity loaded = cartSnapshotJpaRepository.findById(snapshot.getSnapshotId())
            .orElse(null);

        assertNotNull(loaded);
        assertEquals(snapshot.getCartId(), loaded.getCartId());
        assertEquals(snapshot.getCustomerId(), loaded.getCustomerId());
        assertEquals(snapshot.getValidUntil(), loaded.getValidUntil());
    }

    private CartSnapshotJpaEntity buildSnapshot() {
        Instant now = Instant.now();
        CartSnapshotJpaEntity entity = new CartSnapshotJpaEntity();
        entity.setSnapshotId(UUID.randomUUID());
        entity.setCartId(UUID.randomUUID());
        entity.setCustomerId("cust-snap-jpa-1");
        entity.setSnapshotData("{\"items\":[],\"appliedPromoCode\":null}");
        entity.setSubtotalAmount(new BigDecimal("1000.00"));
        entity.setDiscountAmount(BigDecimal.ZERO);
        entity.setTaxAmount(BigDecimal.ZERO);
        entity.setTotalAmount(new BigDecimal("1000.00"));
        entity.setCurrency("RUB");
        entity.setAppliedPromoCode(null);
        entity.setItemCount(0);
        entity.setCreatedAt(now);
        entity.setValidUntil(now.plusSeconds(900));
        return entity;
    }
}
