package com.kanokna.account.application.port.out;

import com.kanokna.account.domain.model.SavedAddress;
import com.kanokna.shared.core.Id;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for address persistence.
 */
public interface SavedAddressRepository {
    List<SavedAddress> findByUserId(Id userId);

    Optional<SavedAddress> findByUserIdAndId(Id userId, Id addressId);

    List<SavedAddress> saveAll(Id userId, List<SavedAddress> addresses);

    void deleteByUserIdAndId(Id userId, Id addressId);
}