package com.kanokna.cart.adapters.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.kanokna.pricing.v1.QuoteCalculatedEvent;

/**
 * Kafka producer configuration for cart events.
 * Spring Boot 4.0 / Spring Kafka 4.0 compatible configuration.
 * 
 * Uses Spring Boot's KafkaProperties to read configuration from application.yml
 * including bootstrap-servers, serializers, and schema registry settings.
 * 
 * @see
 */
@Configuration
@EnableKafka
public class KafkaConfig {

    private final KafkaProperties kafkaProperties;

    /**
     * Constructor injection for KafkaProperties.
     *
     * @param kafkaProperties Kafka configuration properties from application.yml
     */
    public KafkaConfig(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>(kafkaProperties.buildProducerProperties());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // Value serializer from application.yml (KafkaProtobufSerializer)
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, QuoteCalculatedEvent> kafkaTemplate(
            ProducerFactory<String, QuoteCalculatedEvent> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }
}
