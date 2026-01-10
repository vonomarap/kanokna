package com.kanokna.cart.application.port.in;

import com.kanokna.cart.application.dto.CreateSnapshotCommand;
import com.kanokna.cart.application.dto.CreateSnapshotResult;

/**
 * Use case for creating checkout snapshots.
 */
public interface CreateSnapshotUseCase {
    CreateSnapshotResult createSnapshot(CreateSnapshotCommand command);
}
