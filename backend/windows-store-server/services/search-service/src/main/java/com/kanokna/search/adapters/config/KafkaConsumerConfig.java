package com.kanokna.search.adapters.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import com.google.protobuf.Message;
import com.kanokna.catalog.v1.ProductTemplatePublishedEvent;
import com.kanokna.catalog.v1.ProductTemplateUnpublishedEvent;
import com.kanokna.catalog.v1.ProductTemplateUpdatedEvent;

import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer;
import io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializerConfig;

/**
 * Kafka consumer configuration for catalog event ingestion.
 */
@Configuration
public class KafkaConsumerConfig {
    @Bean
    public ConsumerFactory<String, ProductTemplatePublishedEvent> productPublishedConsumerFactory(
        KafkaProperties kafkaProperties
    ) {
        return buildConsumerFactory(kafkaProperties, ProductTemplatePublishedEvent.class);
    }

    @Bean
    public ConsumerFactory<String, ProductTemplateUpdatedEvent> productUpdatedConsumerFactory(
        KafkaProperties kafkaProperties
    ) {
        return buildConsumerFactory(kafkaProperties, ProductTemplateUpdatedEvent.class);
    }

    @Bean
    public ConsumerFactory<String, ProductTemplateUnpublishedEvent> productUnpublishedConsumerFactory(
        KafkaProperties kafkaProperties
    ) {
        return buildConsumerFactory(kafkaProperties, ProductTemplateUnpublishedEvent.class);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductTemplatePublishedEvent>
        productPublishedKafkaListenerContainerFactory(
        ConsumerFactory<String, ProductTemplatePublishedEvent> consumerFactory,
        CommonErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, ProductTemplatePublishedEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductTemplateUpdatedEvent>
        productUpdatedKafkaListenerContainerFactory(
        ConsumerFactory<String, ProductTemplateUpdatedEvent> consumerFactory,
        CommonErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, ProductTemplateUpdatedEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ProductTemplateUnpublishedEvent>
        productUnpublishedKafkaListenerContainerFactory(
        ConsumerFactory<String, ProductTemplateUnpublishedEvent> consumerFactory,
        CommonErrorHandler errorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, ProductTemplateUnpublishedEvent> factory =
            new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
        KafkaTemplate<Object, Object> kafkaTemplate
    ) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate,
            (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition()));
    }

    @Bean
    public CommonErrorHandler kafkaErrorHandler(DeadLetterPublishingRecoverer recoverer) {
        ExponentialBackOffWithMaxRetries backOff = new ExponentialBackOffWithMaxRetries(3);
        backOff.setInitialInterval(1000L);
        backOff.setMultiplier(2.0d);
        backOff.setMaxInterval(10_000L);
        return new DefaultErrorHandler(recoverer, backOff);
    }

    private <T extends Message> ConsumerFactory<String, T> buildConsumerFactory(
        KafkaProperties kafkaProperties,
        Class<T> valueType
    ) {
        Map<String, Object> properties = new HashMap<>(kafkaProperties.buildConsumerProperties());
        properties.put(KafkaProtobufDeserializerConfig.SPECIFIC_PROTOBUF_VALUE_TYPE, valueType.getName());

        KafkaProtobufDeserializer<T> deserializer = new KafkaProtobufDeserializer<>();
        deserializer.configure(properties, false); // false = value deserializer

        return new DefaultKafkaConsumerFactory<>(
            properties,
            new StringDeserializer(),
            deserializer
        );
    }
}
