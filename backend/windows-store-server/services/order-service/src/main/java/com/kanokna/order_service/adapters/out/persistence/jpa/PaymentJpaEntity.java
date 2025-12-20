package com.kanokna.order_service.adapters.out.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "payment")
public class PaymentJpaEntity {

    @Id
    private String id;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(nullable = false, precision = 19, scale = 2)
    private java.math.BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private String method;

    @Column(nullable = false)
    private String status;

    @Column(name = "external_ref")
    private String externalRef;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    protected PaymentJpaEntity() {}

    public PaymentJpaEntity(String id, String orderId, java.math.BigDecimal amount, String currency, String method, String status, String externalRef, String idempotencyKey) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.method = method;
        this.status = status;
        this.externalRef = externalRef;
        this.idempotencyKey = idempotencyKey;
    }

    public String getId() { return id; }
    public String getOrderId() { return orderId; }
    public java.math.BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getMethod() { return method; }
    public String getStatus() { return status; }
    public String getExternalRef() { return externalRef; }
    public String getIdempotencyKey() { return idempotencyKey; }
}
