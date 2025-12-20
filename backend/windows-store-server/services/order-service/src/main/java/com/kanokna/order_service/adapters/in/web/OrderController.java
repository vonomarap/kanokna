package com.kanokna.order_service.adapters.in.web;

import com.kanokna.order_service.adapters.in.web.dto.PlaceOrderRequest;
import com.kanokna.order_service.adapters.in.web.dto.PlaceOrderResponse;
import com.kanokna.order_service.application.dto.PlaceOrderCommand;
import com.kanokna.order_service.adapters.in.web.dto.OrderSummaryResponse;
import com.kanokna.order_service.application.port.in.CheckoutPort;
import com.kanokna.order_service.application.port.in.OrderQueryPort;
import com.kanokna.shared.core.Id;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final CheckoutPort checkoutPort;
    private final OrderQueryPort orderQueryPort;

    public OrderController(CheckoutPort checkoutPort, OrderQueryPort orderQueryPort) {
        this.checkoutPort = checkoutPort;
        this.orderQueryPort = orderQueryPort;
    }

    @PostMapping
    public ResponseEntity<PlaceOrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        logger.info("[HTTP][order][placeOrder][state=START] cartId={}", request.cartId());
        PlaceOrderCommand command = new PlaceOrderCommand(
            Id.of(request.orderId()),
            Id.of(request.cartId()),
            request.customerId() == null ? null : Id.of(request.customerId()),
            toShipping(request.shipping()),
            toInstallation(request.installation()),
            request.idempotencyKey()
        );
        var response = checkoutPort.placeOrder(command);
        logger.info("[HTTP][order][placeOrder][state=DONE] orderId={}", response.orderId().value());
        return ResponseEntity.ok(new PlaceOrderResponse(response.orderId().value(), response.status()));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<?> getOrder(@PathVariable String orderId, @RequestParam(required = false) String customerId) {
        Id oid = Id.of(orderId);
        Id cid = customerId == null ? null : Id.of(customerId);
        return orderQueryPort.getOrder(oid, cid)
            .map(order -> ResponseEntity.ok(new OrderSummaryResponse(
                order.id().value(),
                order.status().name(),
                order.totals().grandTotal().getCurrency().getCurrencyCode(),
                order.items().stream()
                    .map(item -> new OrderSummaryResponse.OrderItemDto(item.configurationId().value(), item.quantity()))
                    .toList()
            )))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private PlaceOrderCommand.ShippingDto toShipping(PlaceOrderRequest.ShippingDto dto) {
        if (dto == null) return null;
        return new PlaceOrderCommand.ShippingDto(dto.addressLine1(), dto.addressLine2(), dto.city(), dto.postalCode(), dto.country(), dto.method());
    }

    private PlaceOrderCommand.InstallationDto toInstallation(PlaceOrderRequest.InstallationDto dto) {
        if (dto == null) return null;
        return new PlaceOrderCommand.InstallationDto(dto.scheduledAt(), dto.installerId(), dto.notes());
    }
}
