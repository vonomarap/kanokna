package com.kanokna.search.adapters.in.kafka;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Parser;
import com.google.protobuf.Timestamp;
import com.kanokna.catalog.v1.ProductTemplatePublishedEvent;
import com.kanokna.catalog.v1.ProductTemplateUnpublishedEvent;
import com.kanokna.catalog.v1.ProductTemplateUpdatedEvent;
import com.kanokna.common.v1.EventMetadata;
import com.kanokna.search.application.dto.CatalogProductDeleteEvent;
import com.kanokna.search.application.dto.CatalogProductEvent;
import com.kanokna.search.application.port.in.DeleteProductUseCase;
import com.kanokna.search.application.port.in.IndexProductUseCase;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(
    classes = SearchCatalogEventListenerIT.TestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    properties = {
        "spring.cloud.config.enabled=false",
        "spring.cloud.config.import-check.enabled=false",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.group-id=search-service-it",
        "spring.kafka.consumer.auto-offset-reset=earliest"
    }
)
@EmbeddedKafka(partitions = 1, topics = {
    "catalog.product.published",
    "catalog.product.updated",
    "catalog.product.unpublished"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class SearchCatalogEventListenerIT {

    @Autowired
    private KafkaTemplate<String, byte[]> kafkaTemplate;

    @MockitoBean
    private IndexProductUseCase indexProductUseCase;

    @MockitoBean
    private DeleteProductUseCase deleteProductUseCase;

    @Value("${kafka.topics.product-published}")
    private String productPublishedTopic;

    @Value("${kafka.topics.product-unpublished}")
    private String productUnpublishedTopic;

    @BeforeEach
    void setUp() {
        reset(indexProductUseCase, deleteProductUseCase);
    }

    @Test
    @DisplayName("Kafka published event is consumed and mapped to index use case")
    void publishedEvent_IsConsumedAndMapped() {
        String productId = "it-pub-" + UUID.randomUUID();
        ProductTemplatePublishedEvent event = ProductTemplatePublishedEvent.newBuilder()
            .setMetadata(eventMetadata("evt-" + productId))
            .setProductTemplateId(productId)
            .setName("Window " + productId)
            .setDescription("Integration test event")
            .setProductFamily("WINDOW")
            .setProfileSystem("REHAU")
            .addAllOpeningTypes(List.of("TILT"))
            .addAllMaterials(List.of("PVC"))
            .addAllColors(List.of("WHITE"))
            .setBasePrice(protoMoney(100_00))
            .setMaxPrice(protoMoney(500_00))
            .setStatus(com.kanokna.catalog.v1.ProductStatus.PRODUCT_STATUS_ACTIVE)
            .setThumbnailUrl("https://example.com/" + productId + ".png")
            .setPopularity(10)
            .setOptionGroupCount(2)
            .setPublishedAt(timestampNow())
            .build();

        kafkaTemplate.send(productPublishedTopic, event.toByteArray());
        kafkaTemplate.flush();

        ArgumentCaptor<CatalogProductEvent> captor = ArgumentCaptor.forClass(CatalogProductEvent.class);
        verify(indexProductUseCase, timeout(10_000).atLeastOnce()).indexProduct(captor.capture());

        boolean found = captor.getAllValues().stream().anyMatch(payload ->
            productId.equals(payload.productId())
                && "PRODUCT_TEMPLATE_PUBLISHED".equals(payload.eventType())
                && "WINDOW".equals(payload.family())
        );
        assertTrue(found, "Expected published Kafka event to be mapped to CatalogProductEvent");
    }

    @Test
    @DisplayName("Kafka unpublished event is consumed and mapped to delete use case")
    void unpublishedEvent_IsConsumedAndMapped() {
        String productId = "it-unpub-" + UUID.randomUUID();
        ProductTemplateUnpublishedEvent event = ProductTemplateUnpublishedEvent.newBuilder()
            .setMetadata(eventMetadata("evt-" + productId + "-del"))
            .setProductTemplateId(productId)
            .setReason("integration-test")
            .build();

        kafkaTemplate.send(productUnpublishedTopic, event.toByteArray());
        kafkaTemplate.flush();

        ArgumentCaptor<CatalogProductDeleteEvent> captor =
            ArgumentCaptor.forClass(CatalogProductDeleteEvent.class);
        verify(deleteProductUseCase, timeout(10_000).atLeastOnce()).deleteProduct(captor.capture());

        boolean found = captor.getAllValues().stream().anyMatch(payload ->
            productId.equals(payload.productId())
        );
        assertTrue(found, "Expected unpublished Kafka event to be mapped to CatalogProductDeleteEvent");
    }

    private EventMetadata eventMetadata(String eventId) {
        return EventMetadata.newBuilder()
            .setEventId(eventId)
            .setOccurredAt(timestampNow())
            .setAggregateId(UUID.randomUUID().toString())
            .setAggregateType("ProductTemplate")
            .setCorrelationId(UUID.randomUUID().toString())
            .build();
    }

    private com.kanokna.common.v1.Money protoMoney(long amountMinor) {
        return com.kanokna.common.v1.Money.newBuilder()
            .setAmountMinor(amountMinor)
            .setCurrency(com.kanokna.common.v1.Currency.CURRENCY_RUB)
            .build();
    }

    private Timestamp timestampNow() {
        Instant now = Instant.now();
        return Timestamp.newBuilder()
            .setSeconds(now.getEpochSecond())
            .setNanos(now.getNano())
            .build();
    }

    @SpringBootConfiguration
    @EnableKafka
    @EnableAutoConfiguration(excludeName = {
        "org.redisson.spring.starter.RedissonAutoConfigurationV4",
        "org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration",
        "org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration",
        "org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration"
    })
    @Import({SearchCatalogEventListener.class, KafkaTestConfig.class})
    static class TestApplication {
    }

    @Configuration
    static class KafkaTestConfig {
        @Bean
        ProducerFactory<String, byte[]> producerFactory(
                @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers
        ) {
            Map<String, Object> props = new HashMap<>();
            props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
            return new DefaultKafkaProducerFactory<>(props);
        }

        @Bean
        KafkaTemplate<String, byte[]> kafkaTemplate(ProducerFactory<String, byte[]> producerFactory) {
            return new KafkaTemplate<>(producerFactory);
        }

        @Bean
        ConsumerFactory<String, ProductTemplatePublishedEvent> productPublishedConsumerFactory(
                @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
                @Value("${spring.kafka.consumer.group-id}") String groupId
        ) {
            return consumerFactory(
                    bootstrapServers,
                    groupId,
                    ProductTemplatePublishedEvent.parser()
            );
        }

        @Bean
        ConsumerFactory<String, ProductTemplateUpdatedEvent> productUpdatedConsumerFactory(
                @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
                @Value("${spring.kafka.consumer.group-id}") String groupId
        ) {
            return consumerFactory(
                    bootstrapServers,
                    groupId,
                    ProductTemplateUpdatedEvent.parser()
            );
        }

        @Bean
        ConsumerFactory<String, ProductTemplateUnpublishedEvent> productUnpublishedConsumerFactory(
                @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
                @Value("${spring.kafka.consumer.group-id}") String groupId
        ) {
            return consumerFactory(
                    bootstrapServers,
                    groupId,
                    ProductTemplateUnpublishedEvent.parser()
            );
        }

        @Bean
        ConcurrentKafkaListenerContainerFactory<String, ProductTemplatePublishedEvent>
                productPublishedKafkaListenerContainerFactory(
                        ConsumerFactory<String, ProductTemplatePublishedEvent> consumerFactory
                ) {
            return containerFactory(consumerFactory);
        }

        @Bean
        ConcurrentKafkaListenerContainerFactory<String, ProductTemplateUpdatedEvent>
                productUpdatedKafkaListenerContainerFactory(
                        ConsumerFactory<String, ProductTemplateUpdatedEvent> consumerFactory
                ) {
            return containerFactory(consumerFactory);
        }

        @Bean
        ConcurrentKafkaListenerContainerFactory<String, ProductTemplateUnpublishedEvent>
                productUnpublishedKafkaListenerContainerFactory(
                        ConsumerFactory<String, ProductTemplateUnpublishedEvent> consumerFactory
                ) {
            return containerFactory(consumerFactory);
        }

        private <T extends Message> ConsumerFactory<String, T> consumerFactory(
                String bootstrapServers,
                String groupId,
                Parser<T> parser
        ) {
            Map<String, Object> props = new HashMap<>();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
            props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
            return new DefaultKafkaConsumerFactory<>(
                    props,
                    new StringDeserializer(),
                    new ProtobufParserDeserializer<>(parser)
            );
        }

        private <T extends Message> ConcurrentKafkaListenerContainerFactory<String, T> containerFactory(
                ConsumerFactory<String, T> consumerFactory
        ) {
            ConcurrentKafkaListenerContainerFactory<String, T> factory =
                    new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory);
            factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
            return factory;
        }
    }

    static final class ProtobufParserDeserializer<T extends Message> implements Deserializer<T> {
        private final Parser<T> parser;

        ProtobufParserDeserializer(Parser<T> parser) {
            this.parser = parser;
        }

        @Override
        public T deserialize(String topic, byte[] data) {
            if (data == null) {
                return null;
            }
            try {
                return parser.parseFrom(data);
            } catch (InvalidProtocolBufferException ex) {
                throw new SerializationException("Failed to deserialize protobuf payload", ex);
            }
        }
    }
}
