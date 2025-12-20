package com.kanokna.order_service.adapters.out.gateway;

import com.kanokna.order_service.application.dto.InitiatePaymentCommand;
import com.kanokna.order_service.application.dto.PaymentGatewaySession;
import com.kanokna.order_service.application.port.out.PaymentGatewayPort;
import org.springframework.stereotype.Component;

@Component
public class PaymentGatewayStub implements PaymentGatewayPort {
    @Override
    public PaymentGatewaySession createSession(InitiatePaymentCommand command) {
        return new PaymentGatewaySession("https://pay.example.com/session/" + command.orderId().value(), "sess-" + command.orderId().value());
    }
}
