package com.kanokna.account.application.port.in;

import com.kanokna.account.application.dto.UpdateProfileCommand;
import com.kanokna.account.application.dto.UserProfileDto;

/**
 * Use case for updating user profiles.
 */
public interface UpdateProfileUseCase {
    UserProfileDto updateProfile(UpdateProfileCommand command);
}