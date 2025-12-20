package com.kanokna.order_service.application.port.out;

import com.kanokna.order_service.domain.model.Order;
import com.kanokna.shared.core.Id;

import java.util.Optional;

public interface OrderRepository {

    void save(Order order);

    Optional<Order> findById(Id orderId);
}
