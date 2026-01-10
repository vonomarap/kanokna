package com.kanokna.cart.application.dto;

import com.kanokna.shared.money.Money;
import java.time.Instant;

/**
 * Result of creating a checkout snapshot.
 */
public record CreateSnapshotResult(
    String snapshotId,
    CartSnapshotDto cartSnapshot,
    Instant validUntil,
    boolean pricesChanged,
    Money previousTotal
) {
}
