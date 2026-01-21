package com.kanokna.cart;

import com.kanokna.cart.application.dto.AddItemCommand;
import com.kanokna.cart.application.dto.AddItemResult;
import com.kanokna.cart.application.dto.BomLineDto;
import com.kanokna.cart.application.dto.CreateSnapshotCommand;
import com.kanokna.cart.application.dto.CreateSnapshotResult;
import com.kanokna.cart.application.dto.DimensionsDto;
import com.kanokna.cart.application.dto.SelectedOptionDto;
import com.kanokna.cart.adapters.out.persistence.CartJpaRepository;
import com.kanokna.cart.adapters.out.persistence.CartSnapshotJpaRepository;
import com.kanokna.cart.application.port.out.CatalogConfigurationPort;
import com.kanokna.cart.application.port.out.PricingPort;
import com.kanokna.cart.application.service.CartApplicationService;
import com.kanokna.cart.domain.model.CartStatus;
import com.kanokna.cart.domain.model.ConfigurationSnapshot;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import com.kanokna.test.containers.kafka.KafkaTestContainer;
import com.kanokna.test.containers.postgres.PostgresTestContainer;
import com.kanokna.test.containers.postgres.PostgresTestContainer.PostgresSettings;
import com.kanokna.test.containers.redis.RedisTestContainer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@EnabledIf(
    value = "com.kanokna.cart.support.DockerAvailability#isDockerAvailable",
    disabledReason = "Docker is not available, skipping Testcontainers integration tests"
)
@SpringBootTest
@Testcontainers
class CartServiceIT {
    private static final String CART_SCHEMA = "cart";
    private static final String SCHEMA_REGISTRY_URL = "mock://cart-service";
    private static final String SPRING_KAFKA_SCHEMA_REGISTRY =
        "spring.kafka.properties.schema.registry.url";

    @Container
    static final PostgreSQLContainer<?> postgres = PostgresTestContainer.instance().container();

    @Container
    static final KafkaContainer kafka = KafkaTestContainer.instance().container();

    @Container
    static final GenericContainer<?> redis = RedisTestContainer.instance().container();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        PostgresTestContainer.instance()
            .registerProperties(registry, PostgresSettings.withSchema(CART_SCHEMA));
        KafkaTestContainer.instance().registerProperties(registry);
        RedisTestContainer.instance().registerProperties(registry);
        registry.add(SPRING_KAFKA_SCHEMA_REGISTRY, () -> SCHEMA_REGISTRY_URL);
    }

    @Autowired
    private CartApplicationService service;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private CartJpaRepository cartJpaRepository;

    @Autowired
    private CartSnapshotJpaRepository cartSnapshotJpaRepository;

    @MockitoBean
    private CatalogConfigurationPort catalogConfigurationPort;

    @MockitoBean
    private PricingPort pricingPort;

    @Test
    @DisplayName("TC-FUNC-CART-ADD-001: integration addItem persists and publishes event")
    void addItemPersistsAndPublishesEvent() {
        Mockito.when(catalogConfigurationPort.validateConfiguration(any()))
            .thenReturn(new CatalogConfigurationPort.ValidationResult(
                true,
                true,
                List.of(),
                List.of()
            ));
        Mockito.when(pricingPort.calculateQuote(any(ConfigurationSnapshot.class), any()))
            .thenReturn(new PricingPort.PriceQuote(
                true,
                "QUOTE-IT-1",
                Money.of(new BigDecimal("1000.00"), Currency.RUB),
                Instant.now().plusSeconds(3600)
            ));

        String sessionId = "sess-it-1";
        AddItemResult result = service.addItem(addItemCommand(null, sessionId, "T-300"));

        assertNotNull(result.cart().cartId());
        assertEquals(sessionId, result.cart().sessionId());
        assertTrue(cartJpaRepository.findBySessionId(sessionId).isPresent());
        assertEquals(result.cart().cartId(),
            redisTemplate.opsForValue().get("cart:session:" + sessionId));

        assertTrue(awaitKafkaEvent("cart.item.added"));
    }

    @Test
    @DisplayName("TC-FUNC-CART-SNAP-001: integration createSnapshot persists snapshot")
    void createSnapshotPersistsSnapshot() {
        Mockito.when(catalogConfigurationPort.validateConfiguration(any()))
            .thenReturn(new CatalogConfigurationPort.ValidationResult(
                true,
                true,
                List.of(),
                List.of()
            ));
        Mockito.when(pricingPort.calculateQuote(any(ConfigurationSnapshot.class), any()))
            .thenReturn(new PricingPort.PriceQuote(
                true,
                "QUOTE-IT-2",
                Money.of(new BigDecimal("1000.00"), Currency.RUB),
                Instant.now().plusSeconds(3600)
            ));

        String customerId = "cust-it-2";
        AddItemResult addResult = service.addItem(addItemCommand(customerId, null, "T-301"));
        CreateSnapshotResult snapshotResult = service.createSnapshot(
            new CreateSnapshotCommand(customerId, true)
        );

        assertNotNull(snapshotResult.snapshotId());
        assertTrue(cartSnapshotJpaRepository.findById(
            UUID.fromString(snapshotResult.snapshotId())).isPresent());
        assertEquals(CartStatus.CHECKED_OUT,
            cartJpaRepository.findByCustomerId(customerId).orElseThrow().getStatus());
        assertEquals(addResult.cart().cartId(),
            cartJpaRepository.findByCustomerId(customerId).orElseThrow().getCartId().toString());
    }

    private AddItemCommand addItemCommand(String customerId, String sessionId, String templateId) {
        return new AddItemCommand(
            customerId,
            sessionId,
            templateId,
            "Window",
            "WINDOW",
            "http://example.com/thumb.png",
            new DimensionsDto(120, 130),
            List.of(new SelectedOptionDto("OPT-GROUP", "OPT-1")),
            1,
            null,
            List.of(new BomLineDto("SKU-1", "Line 1", 1))
        );
    }

    private boolean awaitKafkaEvent(String topic) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "cart-it-" + topic);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());

        try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(List.of(topic));
            long deadline = System.currentTimeMillis() + 5000L;
            while (System.currentTimeMillis() < deadline) {
                ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(200));
                if (!records.isEmpty()) {
                    return true;
                }
            }
            return false;
        }
    }
}
