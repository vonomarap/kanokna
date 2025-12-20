package com.kanokna.order_service.application.port.in;

import com.kanokna.order_service.application.dto.PlaceOrderCommand;
import com.kanokna.order_service.application.dto.OrderCreatedResponse;

public interface CheckoutPort {

    OrderCreatedResponse placeOrder(PlaceOrderCommand command);
}
