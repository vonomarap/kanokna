package com.kanokna.order_service.application.port.out;

import com.kanokna.order_service.domain.model.Payment;
import com.kanokna.shared.core.Id;

import java.util.Optional;

public interface PaymentRepository {

    void save(Payment payment, Id orderId);

    Optional<Payment> findById(Id paymentId);
}
