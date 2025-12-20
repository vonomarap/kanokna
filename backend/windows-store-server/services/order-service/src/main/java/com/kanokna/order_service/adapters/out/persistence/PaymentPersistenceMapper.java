package com.kanokna.order_service.adapters.out.persistence;

import java.util.Currency;

import com.kanokna.order_service.adapters.out.persistence.jpa.PaymentJpaEntity;
import com.kanokna.order_service.domain.model.Payment;
import com.kanokna.order_service.domain.model.PaymentMethod;
import com.kanokna.order_service.domain.model.PaymentStatus;
import com.kanokna.shared.core.Id;
import com.kanokna.shared.money.Money;

public final class PaymentPersistenceMapper {
    private PaymentPersistenceMapper() {}

    public static Payment toDomain(PaymentJpaEntity entity) {
        return new Payment(
            Id.of(entity.getId()),
            Money.of(entity.getAmount(), Currency.getInstance(entity.getCurrency())),
            PaymentMethod.valueOf(entity.getMethod()),
            PaymentStatus.valueOf(entity.getStatus()),
            entity.getExternalRef(),
            entity.getIdempotencyKey()
        );
    }

    public static PaymentJpaEntity toEntity(Payment payment, Id orderId) {
        return new PaymentJpaEntity(
            payment.id().value(),
            orderId.value(),
            payment.amount().getAmount(),
            payment.amount().getCurrency().getCurrencyCode(),
            payment.method().name(),
            payment.status().name(),
            payment.externalRef(),
            payment.idempotencyKey()
        );
    }
}
