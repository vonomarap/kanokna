package com.kanokna.cart.adapters.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.domain.service.CartTotalsCalculator;
import com.kanokna.cart.support.CartServiceTestFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIf(
    value = "com.kanokna.cart.support.DockerAvailability#isDockerAvailable",
    disabledReason = "Docker is not available, skipping Testcontainers integration tests"
)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@Import({CartRepositoryAdapter.class, CartPersistenceMapper.class, CartRepositoryAdapterTest.MapperConfig.class})
class CartRepositoryAdapterTest {
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
    private CartRepositoryAdapter cartRepositoryAdapter;

    @Test
    @DisplayName("TC-FUNC-CART-ADD-005: save and load cart via adapter")
    void saveAndLoadCartViaAdapter() {
        CartTotalsCalculator calculator = new CartTotalsCalculator();
        Cart cart = CartServiceTestFixture.cartWithItems(
            "cust-adapter-1",
            calculator,
            CartServiceTestFixture.item(
                "T-200",
                "Window",
                "WINDOW",
                2,
                CartServiceTestFixture.money("1000.00"),
                "hash-200",
                Instant.now().plusSeconds(3600)
            )
        );
        cart.applyPromoCode(
            CartServiceTestFixture.promo("PROMO10", CartServiceTestFixture.money("100.00")),
            calculator,
            null
        );

        cartRepositoryAdapter.save(cart);
        Cart loaded = cartRepositoryAdapter.findByCustomerId("cust-adapter-1").orElse(null);

        assertNotNull(loaded);
        assertEquals("PROMO10", loaded.appliedPromoCode().code());
        assertEquals(1, loaded.items().size());
        assertEquals("WINDOW", loaded.items().get(0).productFamily());
    }

    @TestConfiguration
    static class MapperConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper().findAndRegisterModules();
        }
    }
}
