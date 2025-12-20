package com.kanokna.order_service.application.port.in;

import com.kanokna.order_service.domain.model.Order;
import com.kanokna.shared.core.Id;

import java.util.Optional;

public interface OrderQueryPort {

    Optional<Order> getOrder(Id orderId, Id customerId);
}
