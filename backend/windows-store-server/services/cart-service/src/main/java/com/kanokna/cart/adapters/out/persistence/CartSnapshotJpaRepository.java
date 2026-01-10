package com.kanokna.cart.adapters.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartSnapshotJpaRepository extends JpaRepository<CartSnapshotJpaEntity, UUID> {
}
