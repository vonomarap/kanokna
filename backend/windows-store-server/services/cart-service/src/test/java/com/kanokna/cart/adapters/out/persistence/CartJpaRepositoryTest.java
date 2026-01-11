package com.kanokna.cart.adapters.out.persistence;

import com.kanokna.cart.domain.model.CartStatus;
import com.kanokna.cart.domain.model.ValidationStatus;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class CartJpaRepositoryTest {
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
    private CartJpaRepository cartJpaRepository;

    @Test
    @DisplayName("TC-FUNC-CART-GET-001: findByCustomerId loads cart with items")
    void findByCustomerIdLoadsItems() {
        CartJpaEntity cart = buildCart("cust-jpa-1", null, "hash-jpa-1");
        cartJpaRepository.saveAndFlush(cart);

        CartJpaEntity loaded = cartJpaRepository.findByCustomerId("cust-jpa-1").orElse(null);

        assertNotNull(loaded);
        assertEquals(CartStatus.ACTIVE, loaded.getStatus());
        assertEquals(1, loaded.getItems().size());
        assertEquals("hash-jpa-1", loaded.getItems().get(0).getConfigurationHash());
    }

    @Test
    @DisplayName("TC-FUNC-CART-GET-003: findBySessionId loads cart with items")
    void findBySessionIdLoadsItems() {
        CartJpaEntity cart = buildCart(null, "sess-jpa-1", "hash-jpa-2");
        cartJpaRepository.saveAndFlush(cart);

        CartJpaEntity loaded = cartJpaRepository.findBySessionId("sess-jpa-1").orElse(null);

        assertNotNull(loaded);
        assertEquals("sess-jpa-1", loaded.getSessionId());
        assertTrue(loaded.getItems().stream().anyMatch(item -> "hash-jpa-2".equals(item.getConfigurationHash())));
    }

    private CartJpaEntity buildCart(String customerId, String sessionId, String hash) {
        Instant now = Instant.now();
        CartJpaEntity cart = new CartJpaEntity();
        cart.setCartId(UUID.randomUUID());
        cart.setCustomerId(customerId);
        cart.setSessionId(sessionId);
        cart.setStatus(CartStatus.ACTIVE);
        cart.setSubtotalAmount(new BigDecimal("1000.00"));
        cart.setSubtotalCurrency("RUB");
        cart.setDiscountAmount(BigDecimal.ZERO);
        cart.setTaxAmount(BigDecimal.ZERO);
        cart.setTotalAmount(new BigDecimal("1000.00"));
        cart.setCreatedAt(now);
        cart.setUpdatedAt(now);
        cart.setVersion(0);

        CartItemJpaEntity item = new CartItemJpaEntity();
        item.setItemId(UUID.randomUUID());
        item.setCart(cart);
        item.setProductTemplateId("T-100");
        item.setProductName("Window");
        item.setProductFamily("WINDOW");
        item.setThumbnailUrl("http://example.com/item.png");
        item.setConfigurationSnapshot("{\"productTemplateId\":\"T-100\",\"widthCm\":120,\"heightCm\":130,"
            + "\"selectedOptions\":[],\"resolvedBom\":[]}");
        item.setConfigurationHash(hash);
        item.setQuantity(1);
        item.setUnitPriceAmount(new BigDecimal("1000.00"));
        item.setUnitPriceCurrency("RUB");
        item.setLineTotalAmount(new BigDecimal("1000.00"));
        item.setQuoteId("QUOTE-100");
        item.setQuoteValidUntil(now.plusSeconds(3600));
        item.setValidationStatus(ValidationStatus.VALID);
        item.setValidationMessage(null);
        item.setCreatedAt(now);
        item.setUpdatedAt(now);
        cart.getItems().add(item);
        return cart;
    }
}
