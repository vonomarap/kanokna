package com.kanokna.order_service.domain.service;

import com.kanokna.order_service.domain.event.OrderCreatedEvent;
import com.kanokna.order_service.domain.event.PaymentAppliedEvent;
import com.kanokna.order_service.domain.model.Order;
import com.kanokna.order_service.domain.model.OrderItem;
import com.kanokna.order_service.domain.model.Totals;
import com.kanokna.order_service.domain.model.Payment;
import com.kanokna.shared.core.Id;

import java.util.List;
import java.util.Objects;

/* <MODULE_CONTRACT
    id="mod.order.domain"
    ROLE="Domain service for order creation and payment application"
    SERVICE="order-service"
    LAYER="domain.service"
    BOUNDED_CONTEXT="order"
    LINKS="backend/windows-store-server/services/order-service/docs/DevelopmentPlan.xml#Flow-Checkout,backend/windows-store-server/services/order-service/docs/DevelopmentPlan.xml#Flow-Payment">
   <PURPOSE>
     Provide pure domain operations for creating orders and applying payments with belief-state traces and domain events.
   </PURPOSE>
   <LOGGING>
     <Pattern>[ORDER][domain][block={id}][state={state}]</Pattern>
     <Anchors>
       <Anchor id="ORDER-VALIDATE" purpose="Validate cart snapshot and totals" />
       <Anchor id="ORDER-EVENT" purpose="Emit order.created event" />
       <Anchor id="PAY-CALLBACK-IDEMP" purpose="Guard idempotency (caller)"/>
       <Anchor id="PAY-STATE" purpose="Apply payment state to order" />
       <Anchor id="PAY-NOTIFY" purpose="Emit payment applied event" />
     </Anchors>
   </LOGGING>
   <TESTS>
     <Case id="TC-ORDER-001">Create order emits OrderCreatedEvent and sets PENDING_PAYMENT.</Case>
     <Case id="TC-ORDER-002">Payment equal to grand total moves status to CONFIRMED.</Case>
     <Case id="TC-ORDER-003">Currency mismatch throws domain exception.</Case>
   </TESTS>
 </MODULE_CONTRACT> */
public final class OrderDomainService {

    /* <FUNCTION_CONTRACT
         id="createOrder"
         LAYER="domain.service"
         INTENT="Create a new order aggregate from cart snapshot and totals"
         INPUT="Id orderId, Id customerId, Id cartId, List<OrderItem> items, Totals totals"
         OUTPUT="OrderCreationResult"
         SIDE_EFFECTS="None; returns OrderCreatedEvent for outbox"
         LINKS="backend/windows-store-server/services/order-service/docs/DevelopmentPlan.xml#Flow-Checkout">
       <PRECONDITIONS>
         <Item>items non-empty and totals currency consistent.</Item>
       </PRECONDITIONS>
       <POSTCONDITIONS>
         <Item>Order status=PENDING_PAYMENT unless paidTotal matches totals.</Item>
       </POSTCONDITIONS>
       <LOGGING>
         - DecisionTrace ORDER-VALIDATE records totals check.
         - DecisionTrace ORDER-EVENT records event creation.
       </LOGGING>
     </FUNCTION_CONTRACT> */
    public OrderCreationResult createOrder(Id orderId, Id customerId, Id cartId, List<OrderItem> items, Totals totals) {
        Objects.requireNonNull(orderId, "orderId");
        Objects.requireNonNull(cartId, "cartId");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(totals, "totals");

        DecisionTrace.Collector traces = new DecisionTrace.Collector();
        traces.trace("ORDER-VALIDATE", "START", "[ORDER][domain][block=ORDER-VALIDATE][state=START]");

        Order order = Order.create(orderId, customerId, cartId, items, totals, null, null);
        traces.trace("ORDER-VALIDATE", "OK", "[ORDER][domain][block=ORDER-VALIDATE][state=OK]");

        OrderCreatedEvent event = OrderCreatedEvent.of(order.id(), order.customerId(), order.status().name());
        traces.trace("ORDER-EVENT", "CREATED", "[ORDER][domain][block=ORDER-EVENT][state=CREATED]");

        return new OrderCreationResult(order, traces.asImmutable(), event);
    }

    /* <FUNCTION_CONTRACT
         id="applyPayment"
         LAYER="domain.service"
         INTENT="Apply payment to order and emit event"
         INPUT="Order order, Payment payment"
         OUTPUT="PaymentApplicationResult"
         SIDE_EFFECTS="None; returns PaymentAppliedEvent for outbox"
         LINKS="backend/windows-store-server/services/order-service/docs/DevelopmentPlan.xml#Flow-Payment">
       <PRECONDITIONS>
         <Item>Order and Payment currencies match.</Item>
       </PRECONDITIONS>
       <POSTCONDITIONS>
         <Item>Order paidTotal increased; status may move to CONFIRMED.</Item>
       </POSTCONDITIONS>
       <LOGGING>
         - DecisionTrace PAY-STATE describes payment application.
         - DecisionTrace PAY-NOTIFY indicates event creation.
       </LOGGING>
     </FUNCTION_CONTRACT> */
    public PaymentApplicationResult applyPayment(Order order, Payment payment) {
        Objects.requireNonNull(order, "order");
        Objects.requireNonNull(payment, "payment");

        DecisionTrace.Collector traces = new DecisionTrace.Collector();
        Order updated = order.applyPayment(payment, traces);
        PaymentAppliedEvent event = PaymentAppliedEvent.of(order.id(), payment.id(), payment.status().name(), payment.amount());
        traces.trace("PAY-NOTIFY", "CREATED", "[ORDER][domain][block=PAY-NOTIFY][state=CREATED]");
        return new PaymentApplicationResult(updated, traces.asImmutable(), event);
    }

    public record OrderCreationResult(Order order, List<DecisionTrace> traces, OrderCreatedEvent event) { }
    public record PaymentApplicationResult(Order order, List<DecisionTrace> traces, PaymentAppliedEvent event) { }
}
