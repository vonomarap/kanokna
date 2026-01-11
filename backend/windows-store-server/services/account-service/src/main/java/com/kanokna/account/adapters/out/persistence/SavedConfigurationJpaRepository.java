package com.kanokna.account.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavedConfigurationJpaRepository extends JpaRepository<SavedConfigurationJpaEntity, UUID> {
    List<SavedConfigurationJpaEntity> findByUserId(UUID userId);
    Optional<SavedConfigurationJpaEntity> findByUserIdAndId(UUID userId, UUID id);
    void deleteByUserIdAndId(UUID userId, UUID id);
}
