package com.kanokna.cart.adapters.out.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.kanokna.test.containers.postgres.PostgresTestContainer;
import com.kanokna.test.containers.postgres.PostgresTestContainer.PostgresSettings;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EnabledIf(
    value = "com.kanokna.cart.support.DockerAvailability#isDockerAvailable",
    disabledReason = "Docker is not available, skipping Testcontainers integration tests"
)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class CartSnapshotJpaRepositoryTest {
    private static final String CART_SCHEMA = "cart";

    @Container
    static final PostgreSQLContainer<?> postgres = PostgresTestContainer.instance().container();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        PostgresTestContainer.instance()
            .registerProperties(registry, PostgresSettings.withSchema(CART_SCHEMA));
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
