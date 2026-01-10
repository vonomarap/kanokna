package com.kanokna.cart.adapters.out.persistence;

import com.kanokna.cart.application.port.out.CartSnapshotRepository;
import com.kanokna.cart.domain.model.CartSnapshot;
import org.springframework.stereotype.Component;

/**
 * JPA adapter for cart snapshot persistence.
 */
@Component
public class CartSnapshotRepositoryAdapter implements CartSnapshotRepository {
    private final CartSnapshotJpaRepository repository;
    private final CartPersistenceMapper mapper;

    public CartSnapshotRepositoryAdapter(CartSnapshotJpaRepository repository, CartPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public CartSnapshot save(CartSnapshot snapshot) {
        CartSnapshotJpaEntity saved = repository.save(mapper.toEntity(snapshot));
        return mapper.toDomain(saved);
    }
}
