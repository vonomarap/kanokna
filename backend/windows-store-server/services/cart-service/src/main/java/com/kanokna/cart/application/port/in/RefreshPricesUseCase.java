package com.kanokna.cart.application.port.in;

import com.kanokna.cart.application.dto.RefreshPricesCommand;
import com.kanokna.cart.application.dto.RefreshPricesResult;

/**
 * Use case for refreshing cart item prices.
 */
public interface RefreshPricesUseCase {
    RefreshPricesResult refreshPrices(RefreshPricesCommand command);
}
