package com.kanokna.cart.application.port.in;

import com.kanokna.cart.application.dto.MergeCartsCommand;
import com.kanokna.cart.application.dto.MergeCartsResult;

/**
 * Use case for merging carts.
 */
public interface MergeCartsUseCase {
    MergeCartsResult mergeCarts(MergeCartsCommand command);
}
