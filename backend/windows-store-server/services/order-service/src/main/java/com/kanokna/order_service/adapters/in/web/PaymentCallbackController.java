package com.kanokna.order_service.adapters.in.web;

import com.kanokna.order_service.application.dto.PaymentCallbackCommand;
import com.kanokna.order_service.application.dto.PaymentResult;
import com.kanokna.order_service.application.port.in.PaymentPort;
import com.kanokna.shared.core.Id;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders/payment-callback")
public class PaymentCallbackController {

    private final PaymentPort paymentPort;

    public PaymentCallbackController(PaymentPort paymentPort) {
        this.paymentPort = paymentPort;
    }

    @PostMapping
    public ResponseEntity<PaymentResult> handle(@RequestBody CallbackRequest request) {
        PaymentResult result = paymentPort.processPaymentCallback(new PaymentCallbackCommand(
            Id.of(request.orderId()),
            Id.of(request.paymentId()),
            request.gatewayStatus(),
            request.externalRef(),
            request.messageId(),
            request.idempotencyKey()
        ));
        return ResponseEntity.ok(result);
    }

    public record CallbackRequest(
        @NotBlank String orderId,
        @NotBlank String paymentId,
        @NotBlank String gatewayStatus,
        String externalRef,
        @NotBlank String messageId,
        String idempotencyKey
    ) {}
}
