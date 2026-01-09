package com.kanokna.pricing_service.adapters.config;

import org.springframework.context.annotation.Configuration;

/**
 * Kafka producer configuration.
 * Configured via application.yml with Protobuf serialization.
 */
@Configuration
public class KafkaConfig {
    // Kafka configuration via spring-kafka
    // Producer configured automatically from application.yml
}
