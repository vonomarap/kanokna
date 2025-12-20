package com.kanokna.order_service.application.port.out;

import com.kanokna.order_service.application.dto.InitiatePaymentCommand;
import com.kanokna.order_service.application.dto.PaymentGatewaySession;

public interface PaymentGatewayPort {

    PaymentGatewaySession createSession(InitiatePaymentCommand command);
}
