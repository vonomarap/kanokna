package com.kanokna.order_service.application.port.in;

import com.kanokna.order_service.application.dto.InitiatePaymentCommand;
import com.kanokna.order_service.application.dto.PaymentCallbackCommand;
import com.kanokna.order_service.application.dto.PaymentResult;

public interface PaymentPort {

    PaymentResult initiatePayment(InitiatePaymentCommand command);

    PaymentResult processPaymentCallback(PaymentCallbackCommand command);
}
