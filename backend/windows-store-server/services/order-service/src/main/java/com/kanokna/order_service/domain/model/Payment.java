package com.kanokna.order_service.domain.model;

import com.kanokna.order_service.domain.exception.OrderDomainException;
import com.kanokna.shared.core.Id;
import com.kanokna.shared.money.Money;

import java.util.Objects;

public final class Payment {
    private final Id id;
    private final Money amount;
    private final PaymentMethod method;
    private final PaymentStatus status;
    private final String externalRef;
    private final String idempotencyKey;

    public Payment(Id id, Money amount, PaymentMethod method, PaymentStatus status, String externalRef, String idempotencyKey) {
        this.id = Objects.requireNonNull(id, "id");
        this.amount = Objects.requireNonNull(amount, "amount");
        this.method = Objects.requireNonNull(method, "method");
        this.status = Objects.requireNonNull(status, "status");
        this.externalRef = externalRef == null ? "" : externalRef;
        this.idempotencyKey = idempotencyKey == null ? "" : idempotencyKey;
    }

    public Id id() {
        return id;
    }

    public Money amount() {
        return amount;
    }

    public PaymentStatus status() {
        return status;
    }

    public PaymentMethod method() {
        return method;
    }

    public String externalRef() {
        return externalRef;
    }

    public String idempotencyKey() {
        return idempotencyKey;
    }

    public Payment applyStatus(PaymentStatus newStatus) {
        if (status == PaymentStatus.CAPTURED && newStatus != PaymentStatus.REFUNDED) {
            throw new OrderDomainException("Cannot change captured payment except refund");
        }
        if (status == PaymentStatus.FAILED && newStatus != PaymentStatus.PENDING) {
            throw new OrderDomainException("Failed payment can only restart as pending");
        }
        return new Payment(id, amount, method, newStatus, externalRef, idempotencyKey);
    }
}
