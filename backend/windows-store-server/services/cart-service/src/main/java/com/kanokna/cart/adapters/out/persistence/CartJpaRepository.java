package com.kanokna.cart.adapters.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartJpaRepository extends JpaRepository<CartJpaEntity, UUID> {
    @EntityGraph(attributePaths = "items")
    Optional<CartJpaEntity> findByCustomerId(String customerId);

    @EntityGraph(attributePaths = "items")
    Optional<CartJpaEntity> findBySessionId(String sessionId);
}
