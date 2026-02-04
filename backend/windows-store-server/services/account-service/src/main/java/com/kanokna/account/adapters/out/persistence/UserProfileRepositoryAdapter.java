package com.kanokna.account.adapters.out.persistence;

import com.kanokna.account.application.port.out.UserProfileRepository;
import com.kanokna.account.domain.model.UserProfile;
import com.kanokna.shared.core.Id;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * MODULE_CONTRACT id="MC-account-profile-repository-adapter"
 * LAYER="adapters.out.persistence" INTENT="JPA adapter translating UserProfile
 * domain to/from JPA entities"
 * LINKS="MC-account-service-domain;Technology.xml#TECH-postgresql"
 *
 * JPA adapter for user profile persistence.
 */
@Component
public class UserProfileRepositoryAdapter implements UserProfileRepository {

    private final UserProfileJpaRepository repository;
    private final SavedAddressJpaRepository addressRepository;
    private final AccountPersistenceMapper mapper;

    public UserProfileRepositoryAdapter(UserProfileJpaRepository repository,
            SavedAddressJpaRepository addressRepository,
            AccountPersistenceMapper mapper) {
        this.repository = repository;
        this.addressRepository = addressRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<UserProfile> findById(Id userId) {
        UUID id = UUID.fromString(userId.value());
        return repository.findById(id)
                .map(entity -> mapper.toDomain(entity, addressRepository.findByUserId(id)));
    }

    @Override
    public UserProfile save(UserProfile profile) {
        UserProfileJpaEntity saved = repository.save(mapper.toEntity(profile));
        List<SavedAddressJpaEntity> addresses = addressRepository.findByUserId(saved.getId());
        return mapper.toDomain(saved, addresses);
    }
}
