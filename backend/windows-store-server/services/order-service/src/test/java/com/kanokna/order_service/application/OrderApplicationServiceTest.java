package com.kanokna.order_service.application;

import com.kanokna.order_service.application.dto.*;
import com.kanokna.order_service.application.port.out.*;
import com.kanokna.order_service.application.service.OrderApplicationService;
import com.kanokna.order_service.domain.model.*;
import com.kanokna.order_service.domain.service.OrderDomainService;
import com.kanokna.shared.core.Id;
import com.kanokna.shared.money.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class OrderApplicationServiceTest {

    private OrderRepository orderRepository;
    private PaymentRepository paymentRepository;
    private PaymentGatewayPort paymentGatewayPort;
    private NotificationPublisher notificationPublisher;
    private OutboxPublisher outboxPublisher;
    private CartPort cartPort;
    private PricingPort pricingPort;
    private IdempotencyStore idempotencyStore;

    private OrderApplicationService service;

    @BeforeEach
    void setup() {
        orderRepository = new InMemoryOrderRepo();
        paymentRepository = new InMemoryPaymentRepo();
        paymentGatewayPort = command -> new PaymentGatewaySession("http://redirect", "sess");
        notificationPublisher = event -> { };
        outboxPublisher = event -> { };
        cartPort = cartId -> new CartSnapshot(cartId, null, List.of(new CartSnapshot.CartItemSnapshot(Id.random(), 1)));
        pricingPort = cart -> new Totals(
            Money.of(BigDecimal.TEN, Currency.getInstance("USD")),
            Money.zero(Currency.getInstance("USD")),
            Money.zero(Currency.getInstance("USD")),
            Money.zero(Currency.getInstance("USD")),
            Money.zero(Currency.getInstance("USD")),
            Money.zero(Currency.getInstance("USD")),
            Money.of(BigDecimal.TEN, Currency.getInstance("USD"))
        );
        idempotencyStore = new InMemoryIdempotency();
        service = new OrderApplicationService(orderRepository, paymentRepository, paymentGatewayPort, notificationPublisher, outboxPublisher, cartPort, pricingPort, new OrderDomainService(), idempotencyStore);
    }

    @Test
    void placeOrder_persists_order() {
        PlaceOrderCommand command = new PlaceOrderCommand(Id.random(), Id.random(), Id.random(), null, null, "key1");
        var response = service.placeOrder(command);
        assertThat(response.orderId()).isNotNull();
        assertThat(orderRepository.findById(response.orderId())).isPresent();
    }

    @Test
    void processPaymentCallback_is_idempotent() {
        PlaceOrderCommand command = new PlaceOrderCommand(Id.random(), Id.random(), Id.random(), null, null, "key2");
        var orderResponse = service.placeOrder(command);
        var initResult = service.initiatePayment(new InitiatePaymentCommand(orderResponse.orderId(), "return", "cancel"));
        PaymentCallbackCommand cb = new PaymentCallbackCommand(orderResponse.orderId(), initResult.paymentId(), "CAPTURED", null, "msg-1", "idemp");

        var first = service.processPaymentCallback(cb);
        var second = service.processPaymentCallback(cb);
        assertThat(first.status()).isEqualTo("CAPTURED");
        assertThat(second.status()).isEqualTo("IGNORED_DUPLICATE");
    }

    // Simple in-memory fakes
    static class InMemoryOrderRepo implements OrderRepository {
        private Order stored;
        @Override public void save(Order order) { stored = order; }
        @Override public Optional<Order> findById(Id orderId) { return Optional.ofNullable(stored); }
    }

    static class InMemoryPaymentRepo implements PaymentRepository {
        private final Map<Id, Payment> storage = new HashMap<>();
        @Override 
        public void save(Payment payment, Id orderId) { storage.put(payment.id(), payment); }
        @Override 
        public Optional<Payment> findById(Id paymentId) { return Optional.ofNullable(storage.get(paymentId)); }
    }

    static class InMemoryIdempotency implements IdempotencyStore {
        private final Set<String> keys = new HashSet<>();
        @Override public boolean exists(String key) { return keys.contains(key); }
        @Override public void put(String key) { keys.add(key); }
    }
}
