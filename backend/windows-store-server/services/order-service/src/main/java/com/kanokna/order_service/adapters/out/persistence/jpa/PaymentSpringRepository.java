package com.kanokna.order_service.adapters.out.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentSpringRepository extends JpaRepository<PaymentJpaEntity, String> {
}
