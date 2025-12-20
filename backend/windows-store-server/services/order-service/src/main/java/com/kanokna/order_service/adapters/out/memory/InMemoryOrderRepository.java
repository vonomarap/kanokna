package com.kanokna.order_service.adapters.out.memory;

import com.kanokna.order_service.application.port.out.OrderRepository;
import com.kanokna.order_service.domain.model.Order;
import com.kanokna.shared.core.Id;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<String, Order> store = new ConcurrentHashMap<>();

    @Override
    public void save(Order order) {
        store.put(order.id().value(), order);
    }

    @Override
    public Optional<Order> findById(Id orderId) {
        return Optional.ofNullable(store.get(orderId.value()));
    }
}
