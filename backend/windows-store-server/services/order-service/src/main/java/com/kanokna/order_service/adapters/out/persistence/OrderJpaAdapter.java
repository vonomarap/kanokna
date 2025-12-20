package com.kanokna.order_service.adapters.out.persistence;

import com.kanokna.order_service.adapters.out.persistence.jpa.OrderJpaEntity;
import com.kanokna.order_service.adapters.out.persistence.jpa.OrderSpringRepository;
import com.kanokna.order_service.application.port.out.OrderRepository;
import com.kanokna.order_service.domain.model.Order;
import com.kanokna.shared.core.Id;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class OrderJpaAdapter implements OrderRepository {

    private final OrderSpringRepository repository;

    public OrderJpaAdapter(OrderSpringRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void save(Order order) {
        OrderJpaEntity entity = OrderPersistenceMapper.toEntity(order);
        repository.save(entity);
    }

    @Override
    public Optional<Order> findById(Id orderId) {
        return repository.findById(orderId.value())
            .map(OrderPersistenceMapper::toDomain);
    }
}
