package com.kanokna.account.adapters.out.persistence;

import com.kanokna.account.application.port.out.SavedConfigurationRepository;
import com.kanokna.account.domain.model.SavedConfiguration;
import com.kanokna.shared.core.Id;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA adapter for saved configuration persistence.
 */
@Component
public class SavedConfigurationRepositoryAdapter implements SavedConfigurationRepository {
    private final SavedConfigurationJpaRepository repository;
    private final AccountPersistenceMapper mapper;

    public SavedConfigurationRepositoryAdapter(SavedConfigurationJpaRepository repository,
                                               AccountPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public SavedConfiguration save(SavedConfiguration configuration) {
        return mapper.toDomain(repository.save(mapper.toEntity(configuration)));
    }

    @Override
    public List<SavedConfiguration> findByUserId(Id userId) {
        return repository.findByUserId(UUID.fromString(userId.value()))
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public Optional<SavedConfiguration> findByUserIdAndId(Id userId, Id configurationId) {
        return repository.findByUserIdAndId(
                UUID.fromString(userId.value()),
                UUID.fromString(configurationId.value())
            )
            .map(mapper::toDomain);
    }

    @Override
    public void deleteByUserIdAndId(Id userId, Id configurationId) {
        repository.deleteByUserIdAndId(
            UUID.fromString(userId.value()),
            UUID.fromString(configurationId.value())
        );
    }
}
