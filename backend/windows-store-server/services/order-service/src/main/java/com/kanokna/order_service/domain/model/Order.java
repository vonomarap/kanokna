package com.kanokna.order_service.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.kanokna.order_service.domain.exception.OrderDomainException;
import com.kanokna.order_service.domain.service.DecisionTrace;
import com.kanokna.shared.core.Id;
import com.kanokna.shared.money.Money;

public final class Order {
    private final Id id;
    private final Id customerId;
    private final Id cartId;
    private final OrderStatus status;
    private final List<OrderItem> items;
    private final Totals totals;
    private final Money paidTotal;
    private final ShippingInfo shippingInfo;
    private final InstallationInfo installationInfo;
    private final long version;

    private Order(
        Id id,
        Id customerId,
        Id cartId,
        OrderStatus status,
        List<OrderItem> items,
        Totals totals,
        Money paidTotal,
        ShippingInfo shippingInfo,
        InstallationInfo installationInfo,
        long version
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.customerId = customerId;
        this.cartId = Objects.requireNonNull(cartId, "cartId");
        this.status = Objects.requireNonNull(status, "status");
        this.items = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(items, "items")));
        this.totals = Objects.requireNonNull(totals, "totals");
        this.paidTotal = Objects.requireNonNull(paidTotal, "paidTotal");
        this.shippingInfo = shippingInfo;
        this.installationInfo = installationInfo;
        this.version = version;
        validateTotals();
    }

    public static Order create(Id orderId, Id customerId, Id cartId, List<OrderItem> items, Totals totals, ShippingInfo shippingInfo, InstallationInfo installationInfo) {
        Money zero = Money.zero(totals.grandTotal().getCurrency());
        return new Order(orderId, customerId, cartId, OrderStatus.PENDING_PAYMENT, items, totals, zero, shippingInfo, installationInfo, 0L);
    }

    public static Order restore(Id orderId, Id customerId, Id cartId, OrderStatus status, List<OrderItem> items, Totals totals, Money paidTotal, ShippingInfo shippingInfo, InstallationInfo installationInfo, long version) {
        return new Order(orderId, customerId, cartId, status, items, totals, paidTotal, shippingInfo, installationInfo, version);
    }

    public Order confirm() {
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new OrderDomainException("Order must be pending payment to confirm");
        }
        return new Order(id, customerId, cartId, OrderStatus.CONFIRMED, items, totals, paidTotal, shippingInfo, installationInfo, version + 1);
    }

    public Order applyPayment(Payment payment, DecisionTrace.Collector traces) {
        ensureCurrency(payment.amount());
        Money newPaid = paidTotal.add(payment.amount());
        if (newPaid.compareTo(totals.grandTotal()) > 0) {
            throw new OrderDomainException("Payment exceeds grand total");
        }
        OrderStatus newStatus = newPaid.compareTo(totals.grandTotal()) == 0 ? OrderStatus.CONFIRMED : status;
        traces.trace("PAY-STATE", "APPLIED", "[ORDER][payment][block=PAY-STATE][state=APPLIED] amount=" + payment.amount().getAmount());
        return new Order(id, customerId, cartId, newStatus, items, totals, newPaid, shippingInfo, installationInfo, version + 1);
    }

    public void ensureCurrency(Money money) {
        if (!money.getCurrency().equals(totals.grandTotal().getCurrency())) {
            throw new OrderDomainException("Currency mismatch on order");
        }
    }

    public Order scheduleInstallation(InstallationInfo info) {
        return new Order(id, customerId, cartId, status, items, totals, paidTotal, shippingInfo, info, version + 1);
    }

    private void validateTotals() {
        if (items.isEmpty()) {
            throw new OrderDomainException("Order requires at least one item");
        }
        if (totals.grandTotal().isNegative()) {
            throw new OrderDomainException("Grand total cannot be negative");
        }
    }

    public Id id() {
        return id;
    }

    public Id customerId() {
        return customerId;
    }

    public Id cartId() {
        return cartId;
    }

    public OrderStatus status() {
        return status;
    }

    public List<OrderItem> items() {
        return items;
    }

    public Totals totals() {
        return totals;
    }

    public Money paidTotal() {
        return paidTotal;
    }

    public Optional<ShippingInfo> shippingInfo() {
        return Optional.ofNullable(shippingInfo);
    }

    public Optional<InstallationInfo> installationInfo() {
        return Optional.ofNullable(installationInfo);
    }

    public long version() {
        return version;
    }
}
