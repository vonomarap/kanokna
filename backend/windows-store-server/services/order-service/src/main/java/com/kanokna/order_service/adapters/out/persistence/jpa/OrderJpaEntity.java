package com.kanokna.order_service.adapters.out.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "orders")
@DynamicUpdate
public class OrderJpaEntity {

    @Id
    private String id;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "cart_id", nullable = false)
    private String cartId;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false, length = 3)
    private String currency;

    @Lob
    @Column(name = "items_json", nullable = false)
    private String itemsJson;

    @Lob
    @Column(name = "totals_json", nullable = false)
    private String totalsJson;

    @Lob
    @Column(name = "shipping_json")
    private String shippingJson;

    @Lob
    @Column(name = "installation_json")
    private String installationJson;

    @Column(name = "paid_total", nullable = false)
    private java.math.BigDecimal paidTotal;

    @Column(nullable = false)
    private long version;

    protected OrderJpaEntity() { }

    public OrderJpaEntity(String id, String customerId, String cartId, String status, String currency, String itemsJson, String totalsJson, String shippingJson, String installationJson, java.math.BigDecimal paidTotal, long version) {
        this.id = id;
        this.customerId = customerId;
        this.cartId = cartId;
        this.status = status;
        this.currency = currency;
        this.itemsJson = itemsJson;
        this.totalsJson = totalsJson;
        this.shippingJson = shippingJson;
        this.installationJson = installationJson;
        this.paidTotal = paidTotal;
        this.version = version;
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getCartId() { return cartId; }
    public String getStatus() { return status; }
    public String getCurrency() { return currency; }
    public String getItemsJson() { return itemsJson; }
    public String getTotalsJson() { return totalsJson; }
    public String getShippingJson() { return shippingJson; }
    public String getInstallationJson() { return installationJson; }
    public java.math.BigDecimal getPaidTotal() { return paidTotal; }
    public long getVersion() { return version; }
}
