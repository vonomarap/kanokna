package com.kanokna.order_service.adapters.out.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.order_service.adapters.out.persistence.jpa.OrderJpaEntity;
import com.kanokna.order_service.domain.exception.OrderDomainException;
import com.kanokna.order_service.domain.model.InstallationInfo;
import com.kanokna.order_service.domain.model.Order;
import com.kanokna.order_service.domain.model.OrderItem;
import com.kanokna.order_service.domain.model.OrderStatus;
import com.kanokna.order_service.domain.model.ShippingInfo;
import com.kanokna.order_service.domain.model.Totals;
import com.kanokna.shared.core.Id;
import com.kanokna.shared.money.Money;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

public final class OrderPersistenceMapper {
    private static final ObjectMapper mapper = new ObjectMapper();

    private OrderPersistenceMapper() {}

    public static Order toDomain(OrderJpaEntity entity) {
        try {
            List<OrderItemRecord> itemRecords = mapper.readValue(entity.getItemsJson(), mapper.getTypeFactory().constructCollectionType(List.class, OrderItemRecord.class));
            TotalsRecord totalsRecord = mapper.readValue(entity.getTotalsJson(), TotalsRecord.class);
            ShippingInfo shipping = entity.getShippingJson() == null ? null : mapper.readValue(entity.getShippingJson(), ShippingInfo.class);
            InstallationInfo installation = entity.getInstallationJson() == null ? null : mapper.readValue(entity.getInstallationJson(), InstallationInfo.class);

            List<OrderItem> items = itemRecords.stream()
                .map(rec -> new OrderItem(Id.of(rec.configurationId), rec.quantity, money(rec.unitPrice, entity.getCurrency()), money(rec.totalPrice, entity.getCurrency())))
                .toList();

            Totals totals = new Totals(
                money(totalsRecord.subtotal, entity.getCurrency()),
                money(totalsRecord.discounts, entity.getCurrency()),
                money(totalsRecord.shipping, entity.getCurrency()),
                money(totalsRecord.installation, entity.getCurrency()),
                money(totalsRecord.tax, entity.getCurrency()),
                money(totalsRecord.deposit, entity.getCurrency()),
                money(totalsRecord.grandTotal, entity.getCurrency())
            );

            return Order.restore(
                Id.of(entity.getId()),
                entity.getCustomerId() == null ? null : Id.of(entity.getCustomerId()),
                Id.of(entity.getCartId()),
                OrderStatus.valueOf(entity.getStatus()),
                items,
                totals,
                money(entity.getPaidTotal(), entity.getCurrency()),
                shipping,
                installation,
                entity.getVersion()
            );
        } catch (JsonProcessingException e) {
            throw new OrderDomainException("Failed to map order entity", e);
        }
    }

    public static OrderJpaEntity toEntity(Order order) {
        try {
            List<OrderItemRecord> items = order.items().stream()
                .map(item -> new OrderItemRecord(item.configurationId().value(), item.quantity(), item.unitPrice().getAmount(), item.totalPrice().getAmount()))
                .toList();
            TotalsRecord totals = new TotalsRecord(
                order.totals().subtotal().getAmount(),
                order.totals().discounts().getAmount(),
                order.totals().shipping().getAmount(),
                order.totals().installation().getAmount(),
                order.totals().tax().getAmount(),
                order.totals().deposit().getAmount(),
                order.totals().grandTotal().getAmount()
            );
            String itemsJson = mapper.writeValueAsString(items);
            String totalsJson = mapper.writeValueAsString(totals);
            String shippingJson = order.shippingInfo().map(val -> writeSafely(val)).orElse(null);
            String installationJson = order.installationInfo().map(val -> writeSafely(val)).orElse(null);

            return new OrderJpaEntity(
                order.id().value(),
                order.customerId() == null ? null : order.customerId().value(),
                order.cartId().value(),
                order.status().name(),
                order.totals().grandTotal().getCurrency().getCurrencyCode(),
                itemsJson,
                totalsJson,
                shippingJson,
                installationJson,
                order.paidTotal().getAmount(),
                order.version()
            );
        } catch (JsonProcessingException e) {
            throw new OrderDomainException("Failed to map order to entity", e);
        }
    }

    private static String writeSafely(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new OrderDomainException("Failed to serialize object", e);
        }
    }

    private static Money money(BigDecimal amount, String currencyCode) {
        return Money.of(amount, Currency.getInstance(currencyCode));
    }

    private record OrderItemRecord(String configurationId, int quantity, BigDecimal unitPrice, BigDecimal totalPrice) { }
    private record TotalsRecord(BigDecimal subtotal, BigDecimal discounts, BigDecimal shipping, BigDecimal installation, BigDecimal tax, BigDecimal deposit, BigDecimal grandTotal) { }
}
