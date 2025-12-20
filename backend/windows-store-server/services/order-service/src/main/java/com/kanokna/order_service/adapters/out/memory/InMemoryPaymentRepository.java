package com.kanokna.order_service.adapters.out.memory;

import com.kanokna.order_service.application.port.out.PaymentRepository;
import com.kanokna.order_service.domain.model.Payment;
import com.kanokna.shared.core.Id;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryPaymentRepository implements PaymentRepository {

    private final Map<String, Payment> store = new ConcurrentHashMap<>();

    @Override
    public void save(Payment payment, Id orderId) {
        store.put(payment.id().value(), payment);
    }

    @Override
    public Optional<Payment> findById(Id paymentId) {
        return Optional.ofNullable(store.get(paymentId.value()));
    }
}
