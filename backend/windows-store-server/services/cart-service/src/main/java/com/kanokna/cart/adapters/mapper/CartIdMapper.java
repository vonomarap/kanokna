package com.kanokna.cart.adapters.mapper;

import com.kanokna.cart.domain.model.CartId;
import com.kanokna.cart.domain.model.CartItemId;
import org.mapstruct.Mapper;

import java.util.UUID;

/**
 * MapStruct mapper for Cart ID value object conversions.
 */
@Mapper(componentModel = "spring")
public interface CartIdMapper {

    default CartId toCartId(UUID uuid) {
        return uuid != null ? CartId.of(uuid) : null;
    }

    default UUID toUuid(CartId cartId) {
        return cartId != null ? cartId.value() : null;
    }

    default CartItemId toCartItemId(UUID uuid) {
        return uuid != null ? CartItemId.of(uuid) : null;
    }

    default UUID toUuid(CartItemId itemId) {
        return itemId != null ? itemId.value() : null;
    }
}
