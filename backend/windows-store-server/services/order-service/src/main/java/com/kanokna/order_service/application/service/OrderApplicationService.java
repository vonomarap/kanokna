package com.kanokna.order_service.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.kanokna.order_service.application.dto.CartSnapshot;
import com.kanokna.order_service.application.dto.InitiatePaymentCommand;
import com.kanokna.order_service.application.dto.OrderCreatedResponse;
import com.kanokna.order_service.application.dto.PaymentCallbackCommand;
import com.kanokna.order_service.application.dto.PaymentGatewaySession;
import com.kanokna.order_service.application.dto.PaymentResult;
import com.kanokna.order_service.application.dto.PlaceOrderCommand;
import com.kanokna.order_service.application.dto.ScheduleInstallationCommand;
import com.kanokna.order_service.application.port.in.CheckoutPort;
import com.kanokna.order_service.application.port.in.InstallationPort;
import com.kanokna.order_service.application.port.in.PaymentPort;
import com.kanokna.order_service.application.port.out.CartPort;
import com.kanokna.order_service.application.port.out.IdempotencyStore;
import com.kanokna.order_service.application.port.out.NotificationPublisher;
import com.kanokna.order_service.application.port.out.OrderRepository;
import com.kanokna.order_service.application.port.out.OutboxPublisher;
import com.kanokna.order_service.application.port.out.PaymentGatewayPort;
import com.kanokna.order_service.application.port.out.PaymentRepository;
import com.kanokna.order_service.application.port.out.PricingPort;
import com.kanokna.order_service.domain.event.OrderCreatedEvent;
import com.kanokna.order_service.domain.event.PaymentAppliedEvent;
import com.kanokna.order_service.domain.model.InstallationInfo;
import com.kanokna.order_service.domain.model.Order;
import com.kanokna.order_service.domain.model.OrderItem;
import com.kanokna.order_service.domain.model.Payment;
import com.kanokna.order_service.domain.model.PaymentMethod;
import com.kanokna.order_service.domain.model.PaymentStatus;
import com.kanokna.order_service.domain.model.Totals;
import com.kanokna.order_service.domain.service.DecisionTrace;
import com.kanokna.order_service.domain.service.OrderDomainService;
import com.kanokna.shared.core.Id;
import com.kanokna.shared.money.Money;

/* <MODULE_CONTRACT
    id="mod.order.application"
    ROLE="Application service orchestrating checkout and payment flows"
    SERVICE="order-service"
    LAYER="application"
    BOUNDED_CONTEXT="order"
    LINKS="backend/windows-store-server/services/order-service/docs/DevelopmentPlan.xml#Flow-Checkout,backend/windows-store-server/services/order-service/docs/DevelopmentPlan.xml#Flow-Payment">
   <PURPOSE>
     Coordinate order creation, pricing revalidation, payment initiation, and payment callbacks, delegating pure logic to the domain layer and publishing events via outbox/notifications.
   </PURPOSE>
   <LOGGING>
     <Pattern>[APP][order][block={id}][state={state}]</Pattern>
     <Anchors>
       <Anchor id="ORDER-VALIDATE" purpose="Revalidate cart/pricing and idempotency" />
       <Anchor id="ORDER-PERSIST" purpose="Persist order/payment aggregates" />
       <Anchor id="ORDER-EVENT" purpose="Publish order created event" />
       <Anchor id="PAY-CALLBACK-IDEMP" purpose="Guard payment callback idempotency" />
       <Anchor id="PAY-STATE" purpose="Apply payment state" />
       <Anchor id="PAY-NOTIFY" purpose="Publish payment events" />
     </Anchors>
   </LOGGING>
   <TESTS>
     <Case id="TC-APP-ORDER-001">placeOrder saves order and publishes OrderCreatedEvent.</Case>
     <Case id="TC-APP-ORDER-002">processPaymentCallback is idempotent on same messageId/idempotencyKey.</Case>
   </TESTS>
 </MODULE_CONTRACT> */
public final class OrderApplicationService implements CheckoutPort, PaymentPort, InstallationPort {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayPort paymentGatewayPort;
    private final NotificationPublisher notificationPublisher;
    private final OutboxPublisher outboxPublisher;
    private final CartPort cartPort;
    private final PricingPort pricingPort;
    private final OrderDomainService domainService;
    private final IdempotencyStore idempotencyStore;

    public OrderApplicationService(OrderRepository orderRepository,
                                   PaymentRepository paymentRepository,
                                   PaymentGatewayPort paymentGatewayPort,
                                   NotificationPublisher notificationPublisher,
                                   OutboxPublisher outboxPublisher,
                                   CartPort cartPort,
                                   PricingPort pricingPort,
                                   OrderDomainService domainService,
                                   IdempotencyStore idempotencyStore) {
        this.orderRepository = Objects.requireNonNull(orderRepository);
        this.paymentRepository = Objects.requireNonNull(paymentRepository);
        this.paymentGatewayPort = Objects.requireNonNull(paymentGatewayPort);
        this.notificationPublisher = Objects.requireNonNull(notificationPublisher);
        this.outboxPublisher = Objects.requireNonNull(outboxPublisher);
        this.cartPort = Objects.requireNonNull(cartPort);
        this.pricingPort = Objects.requireNonNull(pricingPort);
        this.domainService = Objects.requireNonNull(domainService);
        this.idempotencyStore = Objects.requireNonNull(idempotencyStore);
    }

    /* <FUNCTION_CONTRACT
         id="placeOrder.app"
         LAYER="application.service"
         INTENT="Create order from cart with totals validation and event emission"
         INPUT="PlaceOrderCommand"
         OUTPUT="OrderCreatedResponse"
         SIDE_EFFECTS="Persist order; publish OrderCreatedEvent to outbox/notifications"
         LINKS="backend/windows-store-server/services/order-service/docs/DevelopmentPlan.xml#Flow-Checkout">
       <PRECONDITIONS>
         <Item>Cart exists; pricing validated.</Item>
       </PRECONDITIONS>
       <POSTCONDITIONS>
         <Item>Order saved with status PENDING_PAYMENT.</Item>
         <Item>OrderCreatedEvent published.</Item>
       </POSTCONDITIONS>
     </FUNCTION_CONTRACT> */
    @Override
    public OrderCreatedResponse placeOrder(PlaceOrderCommand command) {
        DecisionTrace.Collector traces = new DecisionTrace.Collector();
        traces.trace("ORDER-VALIDATE", "START", "[APP][order][block=ORDER-VALIDATE][state=START]");

        if (command.idempotencyKey() != null && !command.idempotencyKey().isBlank()) {
            if (idempotencyStore.exists(command.idempotencyKey())) {
                return new OrderCreatedResponse(command.orderId(), "PENDING_PAYMENT");
            }
            idempotencyStore.put(command.idempotencyKey());
        }

        CartSnapshot cart = cartPort.fetchCart(command.cartId());
        Totals totals = pricingPort.reprice(cart);
        Order order = domainService.createOrder(
            command.orderId(),
            command.customerId(),
            command.cartId(),
            toOrderItems(cart, totals),
            totals
        ).order();

        traces.trace("ORDER-PERSIST", "SAVE", "[APP][order][block=ORDER-PERSIST][state=SAVE]");
        orderRepository.save(order);

        OrderCreatedEvent event = OrderCreatedEvent.of(order.id(), order.customerId(), order.status().name());
        outboxPublisher.publish(event);
        notificationPublisher.publish(event);
        traces.trace("ORDER-EVENT", "PUBLISHED", "[APP][order][block=ORDER-EVENT][state=PUBLISHED]");

        return new OrderCreatedResponse(order.id(), order.status().name());
    }

    /* <FUNCTION_CONTRACT
         id="initiatePayment.app"
         LAYER="application.service"
         INTENT="Create payment session via gateway"
         INPUT="InitiatePaymentCommand"
         OUTPUT="PaymentResult"
         SIDE_EFFECTS="Persist payment intent; invoke gateway"
         LINKS="backend/windows-store-server/services/order-service/docs/DevelopmentPlan.xml#Flow-Payment">
     </FUNCTION_CONTRACT> */
    @Override
    public PaymentResult initiatePayment(InitiatePaymentCommand command) {
        Order order = orderRepository.findById(command.orderId())
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        Payment payment = new Payment(Id.random(), order.totals().grandTotal(), PaymentMethod.CARD, PaymentStatus.PENDING, "", command.orderId().value());
        paymentRepository.save(payment, order.id());

        PaymentGatewaySession session = paymentGatewayPort.createSession(command);
        return new PaymentResult(order.id(), payment.id(), payment.status().name(), session);
    }

    /* <FUNCTION_CONTRACT
         id="processPaymentCallback.app"
         LAYER="application.service"
         INTENT="Idempotently apply gateway callback to order/payment"
         INPUT="PaymentCallbackCommand"
         OUTPUT="PaymentResult"
         SIDE_EFFECTS="Persist payment/order changes; publish events"
         LINKS="backend/windows-store-server/services/order-service/docs/DevelopmentPlan.xml#Flow-Payment">
       <LOGGING>
         - Anchors PAY-CALLBACK-IDEMP, PAY-STATE, PAY-NOTIFY used for logs.
       </LOGGING>
     </FUNCTION_CONTRACT> */
    @Override
    public PaymentResult processPaymentCallback(PaymentCallbackCommand command) {
        DecisionTrace.Collector traces = new DecisionTrace.Collector();
        traces.trace("PAY-CALLBACK-IDEMP", "CHECK", "[APP][order][block=PAY-CALLBACK-IDEMP][state=CHECK]");

        if (idempotencyStore.exists(command.messageId())) {
            return new PaymentResult(command.orderId(), command.paymentId(), "IGNORED_DUPLICATE", null);
        }

        Order order = orderRepository.findById(command.orderId())
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        Payment payment = paymentRepository.findById(command.paymentId())
            .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        PaymentStatus newStatus = mapStatus(command.gatewayStatus());
        Payment updatedPayment = payment.applyStatus(newStatus);
        paymentRepository.save(updatedPayment, order.id());

        Order updatedOrder = domainService.applyPayment(order, updatedPayment).order();
        orderRepository.save(updatedOrder);
        PaymentAppliedEvent event = PaymentAppliedEvent.of(order.id(), payment.id(), updatedPayment.status().name(), payment.amount());
        outboxPublisher.publish(event);
        notificationPublisher.publish(event);
        traces.trace("PAY-NOTIFY", "PUBLISHED", "[APP][order][block=PAY-NOTIFY][state=PUBLISHED]");
        idempotencyStore.put(command.messageId());

        return new PaymentResult(order.id(), payment.id(), updatedPayment.status().name(), null);
    }

    /* <FUNCTION_CONTRACT
         id="scheduleInstallation.app"
         LAYER="application.service"
         INTENT="Update order installation info"
         INPUT="ScheduleInstallationCommand"
         OUTPUT="Order"
         SIDE_EFFECTS="Persist order changes"
     </FUNCTION_CONTRACT> */
    @Override
    public Order scheduleInstallation(ScheduleInstallationCommand command) {
        Order order = orderRepository.findById(command.orderId())
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        InstallationInfo info = new InstallationInfo(command.scheduledAt(), command.installerId(), command.notes());
        Order updated = order.scheduleInstallation(info);
        orderRepository.save(updated);
        return updated;
    }

    private List<OrderItem> toOrderItems(CartSnapshot cart, Totals totals) {
        int totalQty = cart.items().stream().mapToInt(CartSnapshot.CartItemSnapshot::quantity).sum();
        if (totalQty <= 0) {
            throw new IllegalArgumentException("Cart quantity must be positive");
        }
        BigDecimal unitAmount = totals.subtotal().getAmount().divide(java.math.BigDecimal.valueOf(totalQty));
        Money unit = Money.of(unitAmount, totals.subtotal().getCurrency());
        return cart.items().stream()
            .map(item -> {
                Money lineTotal = unit.multiplyBy(BigDecimal.valueOf(item.quantity()));
                return new OrderItem(item.configurationId(), item.quantity(), unit, lineTotal);
            })
            .collect(Collectors.toList());
    }

    private PaymentStatus mapStatus(String gatewayStatus) {
        return switch (gatewayStatus.toUpperCase()) {
            case "AUTHORIZED" -> PaymentStatus.AUTHORIZED;
            case "CAPTURED", "PAID" -> PaymentStatus.CAPTURED;
            case "REFUNDED" -> PaymentStatus.REFUNDED;
            default -> PaymentStatus.FAILED;
        };
    }
}
