package com.kanokna.account.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavedAddressJpaRepository extends JpaRepository<SavedAddressJpaEntity, UUID> {
    List<SavedAddressJpaEntity> findByUserId(UUID userId);
    Optional<SavedAddressJpaEntity> findByUserIdAndId(UUID userId, UUID id);
    void deleteByUserIdAndId(UUID userId, UUID id);
}
