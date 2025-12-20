package com.kanokna.order_service.adapters.out.persistence;

import com.kanokna.order_service.adapters.out.persistence.jpa.PaymentJpaEntity;
import com.kanokna.order_service.adapters.out.persistence.jpa.PaymentSpringRepository;
import com.kanokna.order_service.application.port.out.PaymentRepository;
import com.kanokna.order_service.domain.model.Payment;
import com.kanokna.shared.core.Id;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class PaymentJpaAdapter implements PaymentRepository {

    private final PaymentSpringRepository repository;

    public PaymentJpaAdapter(PaymentSpringRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void save(Payment payment, Id orderId) {
        PaymentJpaEntity entity = PaymentPersistenceMapper.toEntity(payment, orderId);
        repository.save(entity);
    }

    @Override
    public Optional<Payment> findById(Id paymentId) {
        return repository.findById(paymentId.value())
            .map(PaymentPersistenceMapper::toDomain);
    }
}
