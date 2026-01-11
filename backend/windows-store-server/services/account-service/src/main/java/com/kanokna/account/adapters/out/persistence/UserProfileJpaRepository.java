package com.kanokna.account.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserProfileJpaRepository extends JpaRepository<UserProfileJpaEntity, UUID> {
}