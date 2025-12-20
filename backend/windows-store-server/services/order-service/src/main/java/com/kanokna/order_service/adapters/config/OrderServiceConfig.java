package com.kanokna.order_service.adapters.config;

import com.kanokna.order_service.adapters.out.kafka.KafkaOutboxPublisher;
import com.kanokna.order_service.adapters.out.kafka.NotificationPublisherKafka;
import com.kanokna.order_service.adapters.out.memory.InMemoryIdempotencyStore;
import com.kanokna.order_service.adapters.out.persistence.OrderJpaAdapter;
import com.kanokna.order_service.adapters.out.persistence.PaymentJpaAdapter;
import com.kanokna.order_service.application.port.out.*;
import com.kanokna.order_service.application.service.OrderApplicationService;
import com.kanokna.order_service.domain.service.OrderDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class OrderServiceConfig {

    @Bean
    public OrderDomainService orderDomainService() {
        return new OrderDomainService();
    }

    @Bean
    public OrderApplicationService orderApplicationService(
        OrderRepository orderRepository,
        PaymentRepository paymentRepository,
        PaymentGatewayPort paymentGatewayPort,
        NotificationPublisher notificationPublisher,
        OutboxPublisher outboxPublisher,
        CartPort cartPort,
        PricingPort pricingPort,
        OrderDomainService orderDomainService,
        IdempotencyStore idempotencyStore
    ) {
        return new OrderApplicationService(orderRepository, paymentRepository, paymentGatewayPort, notificationPublisher, outboxPublisher, cartPort, pricingPort, orderDomainService, idempotencyStore);
    }

    @Bean
    public OrderRepository orderRepository(OrderJpaAdapter repo) {
        return repo;
    }

    @Bean
    public PaymentRepository paymentRepository(PaymentJpaAdapter repo) {
        return repo;
    }

    @Bean
    public OutboxPublisher outboxPublisher(KafkaOutboxPublisher publisher) {
        return publisher;
    }

    @Bean
    public NotificationPublisher notificationPublisher(NotificationPublisherKafka publisher) {
        return publisher;
    }

    @Bean
    public IdempotencyStore idempotencyStore(InMemoryIdempotencyStore store) {
        return store;
    }

    @Bean
    @Primary
    public IdempotencyStore redisIdempotencyStore(com.kanokna.order_service.adapters.out.redis.RedisIdempotencyStore store) {
        return store;
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        return template;
    }
}
