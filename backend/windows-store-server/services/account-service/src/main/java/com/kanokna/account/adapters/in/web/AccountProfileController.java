package com.kanokna.account.adapters.in.web;

import com.kanokna.account.application.dto.UpdateProfileCommand;
import com.kanokna.account.application.dto.UserProfileDto;
import com.kanokna.account.application.dto.GetProfileQuery;
import com.kanokna.account.application.port.in.GetProfileUseCase;
import com.kanokna.account.application.port.in.UpdateProfileUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * MODULE_CONTRACT id="MC-account-profile-rest-adapter" LAYER="adapters.in.web"
 * INTENT="REST controller for profile management, translating HTTP to use case
 * calls"
 * LINKS="Technology.xml#TECH-spring-mvc;RequirementsAnalysis.xml#UC-ACCOUNT-MANAGE-PROFILE"
 *
 * REST controller for profile management. Endpoint:
 * /api/accounts/{userId}/profile
 */
@RestController
@RequestMapping("/api/accounts/{userId}/profile")
public class AccountProfileController {

    private final GetProfileUseCase getProfileUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;

    public AccountProfileController(GetProfileUseCase getProfileUseCase,
            UpdateProfileUseCase updateProfileUseCase) {
        this.getProfileUseCase = getProfileUseCase;
        this.updateProfileUseCase = updateProfileUseCase;
    }

    @GetMapping
    public ResponseEntity<UserProfileDto> getProfile(@PathVariable UUID userId) {
        UserProfileDto profile = getProfileUseCase.getProfile(new GetProfileQuery(userId));
        return ResponseEntity.ok(profile);
    }

    @PutMapping
    public ResponseEntity<UserProfileDto> updateProfile(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        UpdateProfileCommand command = new UpdateProfileCommand(
                userId,
                request.firstName(),
                request.lastName(),
                request.phoneNumber(),
                request.preferredLanguage(),
                request.preferredCurrency()
        );
        UserProfileDto profile = updateProfileUseCase.updateProfile(command);
        return ResponseEntity.ok(profile);
    }

    public record UpdateProfileRequest(
            String firstName,
            String lastName,
            String phoneNumber,
            String preferredLanguage,
            String preferredCurrency
            ) {

    }
}
