package com.kanokna.account.adapters.out.persistence;

import com.kanokna.account.application.port.out.SavedAddressRepository;
import com.kanokna.account.domain.model.SavedAddress;
import com.kanokna.shared.core.Id;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA adapter for saved address persistence.
 */
@Component
public class SavedAddressRepositoryAdapter implements SavedAddressRepository {
    private final SavedAddressJpaRepository repository;
    private final AccountPersistenceMapper mapper;

    public SavedAddressRepositoryAdapter(SavedAddressJpaRepository repository,
                                         AccountPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<SavedAddress> findByUserId(Id userId) {
        return repository.findByUserId(UUID.fromString(userId.value()))
            .stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public Optional<SavedAddress> findByUserIdAndId(Id userId, Id addressId) {
        return repository.findByUserIdAndId(
                UUID.fromString(userId.value()),
                UUID.fromString(addressId.value())
            )
            .map(mapper::toDomain);
    }

    @Override
    public List<SavedAddress> saveAll(Id userId, List<SavedAddress> addresses) {
        List<SavedAddressJpaEntity> entities = addresses.stream()
            .map(address -> mapper.toEntity(userId, address))
            .toList();
        return repository.saveAll(entities).stream()
            .map(mapper::toDomain)
            .toList();
    }

    @Override
    public void deleteByUserIdAndId(Id userId, Id addressId) {
        repository.deleteByUserIdAndId(
            UUID.fromString(userId.value()),
            UUID.fromString(addressId.value())
        );
    }
}
