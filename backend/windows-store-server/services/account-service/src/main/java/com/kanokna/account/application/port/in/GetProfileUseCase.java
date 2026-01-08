package com.kanokna.account.application.port.in;

import com.kanokna.account.application.dto.GetProfileQuery;
import com.kanokna.account.application.dto.UserProfileDto;

/**
 * Use case for retrieving user profiles.
 */
public interface GetProfileUseCase {
    UserProfileDto getProfile(GetProfileQuery query);
}