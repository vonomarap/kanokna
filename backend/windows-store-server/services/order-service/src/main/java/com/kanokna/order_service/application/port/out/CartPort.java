package com.kanokna.order_service.application.port.out;

import com.kanokna.order_service.application.dto.CartSnapshot;
import com.kanokna.shared.core.Id;

public interface CartPort {

    CartSnapshot fetchCart(Id cartId);
}
