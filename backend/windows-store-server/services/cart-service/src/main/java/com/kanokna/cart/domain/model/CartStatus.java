package com.kanokna.cart.domain.model;

/**
 * Cart state lifecycle.
 */
public enum CartStatus {
    ACTIVE,
    CHECKED_OUT,
    ABANDONED,
    MERGED
}
