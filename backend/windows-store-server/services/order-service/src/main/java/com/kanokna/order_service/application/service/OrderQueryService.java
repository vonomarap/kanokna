package com.kanokna.order_service.application.service;

import com.kanokna.order_service.application.port.in.OrderQueryPort;
import com.kanokna.order_service.application.port.out.OrderRepository;
import com.kanokna.order_service.domain.model.Order;
import com.kanokna.shared.core.Id;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class OrderQueryService implements OrderQueryPort {

    private final OrderRepository orderRepository;

    public OrderQueryService(OrderRepository orderRepository) {
        this.orderRepository = Objects.requireNonNull(orderRepository);
    }

    @Override
    public Optional<Order> getOrder(Id orderId, Id customerId) {
        return orderRepository.findById(orderId)
            .filter(order -> customerId == null || customerId.equals(order.customerId()));
    }
}
