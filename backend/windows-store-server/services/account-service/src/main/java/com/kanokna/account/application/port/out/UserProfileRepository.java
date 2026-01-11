package com.kanokna.account.application.port.out;

import com.kanokna.account.domain.model.UserProfile;
import com.kanokna.shared.core.Id;

import java.util.Optional;

/**
 * Outbound port for profile persistence.
 */
public interface UserProfileRepository {
    Optional<UserProfile> findById(Id userId);

    UserProfile save(UserProfile profile);
}