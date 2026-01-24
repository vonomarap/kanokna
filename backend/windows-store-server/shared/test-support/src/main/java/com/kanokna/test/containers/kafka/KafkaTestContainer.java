package com.kanokna.test.containers.kafka;

import java.time.Duration;
import java.util.Collections;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared Kafka Testcontainers configuration using Confluent Platform.
 * Provides a singleton container and helper methods for topics and client props.
 */
public final class KafkaTestContainer {
    private static final DockerImageName KAFKA_IMAGE = DockerImageName
        .parse("confluentinc/cp-kafka:7.6.0");
    private static final String SPRING_KAFKA_BOOTSTRAP_SERVERS = "spring.kafka.bootstrap-servers";
    private static final String BOOTSTRAP_SERVERS = "bootstrap.servers";
    private static final String GROUP_ID = "group.id";
    private static final String AUTO_OFFSET_RESET = "auto.offset.reset";
    private static final String EARLIEST_OFFSET = "earliest";
    private static final String KEY_DESERIALIZER = "key.deserializer";
    private static final String VALUE_DESERIALIZER = "value.deserializer";
    private static final String KEY_SERIALIZER = "key.serializer";
    private static final String VALUE_SERIALIZER = "value.serializer";
    private static final String STRING_DESERIALIZER =
        "org.apache.kafka.common.serialization.StringDeserializer";
    private static final String BYTE_ARRAY_DESERIALIZER =
        "org.apache.kafka.common.serialization.ByteArrayDeserializer";
    private static final String STRING_SERIALIZER =
        "org.apache.kafka.common.serialization.StringSerializer";
    private static final String BYTE_ARRAY_SERIALIZER =
        "org.apache.kafka.common.serialization.ByteArraySerializer";
    private static final int DEFAULT_PARTITIONS = 1;
    private static final short DEFAULT_REPLICATION_FACTOR = 1;
    private static final Duration ADMIN_TIMEOUT = Duration.ofSeconds(10);

    private static final KafkaTestContainer INSTANCE = new KafkaTestContainer();

    private final KafkaContainer container;

    private KafkaTestContainer() {
        container = new KafkaContainer(KAFKA_IMAGE)
            .withKraft()
            .withReuse(true);
    }

    public static KafkaTestContainer instance() {
        return INSTANCE;
    }

    public KafkaContainer container() {
        return container;
    }

    public void startIfNeeded() {
        if (!container.isRunning()) {
            container.start();
        }
    }

    public String bootstrapServers() {
        startIfNeeded();
        return container.getBootstrapServers();
    }

    public void registerProperties(DynamicPropertyRegistry registry) {
        Objects.requireNonNull(registry, "registry");
        registry.add(SPRING_KAFKA_BOOTSTRAP_SERVERS, this::bootstrapServers);
    }

    public Properties producerProperties() {
        Properties props = new Properties();
        props.put(BOOTSTRAP_SERVERS, bootstrapServers());
        props.put(KEY_SERIALIZER, STRING_SERIALIZER);
        props.put(VALUE_SERIALIZER, BYTE_ARRAY_SERIALIZER);
        return props;
    }

    public Properties consumerProperties(String groupId) {
        Objects.requireNonNull(groupId, "groupId");
        Properties props = new Properties();
        props.put(BOOTSTRAP_SERVERS, bootstrapServers());
        props.put(GROUP_ID, groupId);
        props.put(AUTO_OFFSET_RESET, EARLIEST_OFFSET);
        props.put(KEY_DESERIALIZER, STRING_DESERIALIZER);
        props.put(VALUE_DESERIALIZER, BYTE_ARRAY_DESERIALIZER);
        return props;
    }

    public void createTopic(String topicName) {
        createTopic(topicName, DEFAULT_PARTITIONS, DEFAULT_REPLICATION_FACTOR);
    }

    public void createTopic(String topicName, int partitions, short replicationFactor) {
        Objects.requireNonNull(topicName, "topicName");
        NewTopic topic = new NewTopic(topicName, partitions, replicationFactor);
        try (Admin admin = AdminClient.create(adminProperties())) {
            admin.createTopics(Collections.singleton(topic))
                .all()
                .get(ADMIN_TIMEOUT.toSeconds(), TimeUnit.SECONDS);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create topic " + topicName, ex);
        }
    }

    private Properties adminProperties() {
        Properties props = new Properties();
        props.put(BOOTSTRAP_SERVERS, bootstrapServers());
        return props;
    }
}
