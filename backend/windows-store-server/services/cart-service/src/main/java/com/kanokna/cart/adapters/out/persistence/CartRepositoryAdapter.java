package com.kanokna.cart.adapters.out.persistence;

import com.kanokna.cart.application.port.out.CartRepository;
import com.kanokna.cart.domain.model.Cart;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * JPA adapter for cart persistence.
 */
@Component
public class CartRepositoryAdapter implements CartRepository {
    private final CartJpaRepository repository;
    private final CartPersistenceMapper mapper;

    public CartRepositoryAdapter(CartJpaRepository repository, CartPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Cart> findByCustomerId(String customerId) {
        return repository.findByCustomerId(customerId).map(mapper::toDomain);
    }

    @Override
    public Optional<Cart> findBySessionId(String sessionId) {
        return repository.findBySessionId(sessionId).map(mapper::toDomain);
    }

    @Override
    public Cart save(Cart cart) {
        CartJpaEntity saved = repository.save(mapper.toEntity(cart));
        return mapper.toDomain(saved);
    }
}
