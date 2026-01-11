package com.kanokna.account.application.port.out;

import com.kanokna.account.domain.model.SavedConfiguration;
import com.kanokna.shared.core.Id;

import java.util.List;
import java.util.Optional;

/**
 * Outbound port for saved configuration persistence.
 */
public interface SavedConfigurationRepository {
    SavedConfiguration save(SavedConfiguration configuration);

    List<SavedConfiguration> findByUserId(Id userId);

    Optional<SavedConfiguration> findByUserIdAndId(Id userId, Id configurationId);

    void deleteByUserIdAndId(Id userId, Id configurationId);
}