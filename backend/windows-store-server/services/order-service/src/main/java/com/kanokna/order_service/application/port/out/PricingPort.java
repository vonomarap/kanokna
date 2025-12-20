package com.kanokna.order_service.application.port.out;

import com.kanokna.order_service.application.dto.CartSnapshot;
import com.kanokna.order_service.domain.model.Totals;

public interface PricingPort {

    Totals reprice(CartSnapshot cartSnapshot);
}
