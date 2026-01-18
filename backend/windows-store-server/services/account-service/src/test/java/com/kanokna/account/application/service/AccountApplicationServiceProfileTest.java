package com.kanokna.account.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.account.adapters.config.AccountProperties;
import com.kanokna.account.application.dto.GetProfileQuery;
import com.kanokna.account.application.dto.UserProfileDto;
import com.kanokna.account.application.port.out.CurrentUser;
import com.kanokna.account.domain.event.UserProfileCreatedEvent;
import com.kanokna.account.domain.exception.AccountDomainErrors;
import com.kanokna.account.domain.model.*;
import com.kanokna.account.support.AccountServiceTestFixture;
import com.kanokna.shared.core.Email;
import com.kanokna.shared.core.Id;
import com.kanokna.shared.core.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccountApplicationServiceProfileTest {
    private AccountServiceTestFixture.InMemoryUserProfileRepository userProfileRepository;
    private AccountServiceTestFixture.InMemorySavedAddressRepository savedAddressRepository;
    private AccountServiceTestFixture.InMemorySavedConfigurationRepository savedConfigurationRepository;
    private AccountServiceTestFixture.RecordingEventPublisher eventPublisher;
    private AccountServiceTestFixture.FixedCurrentUserProvider currentUserProvider;
    private AccountProperties accountProperties;
    private AccountApplicationService service;

    @BeforeEach
    void setUp() {
        userProfileRepository = new AccountServiceTestFixture.InMemoryUserProfileRepository();
        savedAddressRepository = new AccountServiceTestFixture.InMemorySavedAddressRepository();
        savedConfigurationRepository = new AccountServiceTestFixture.InMemorySavedConfigurationRepository();
        eventPublisher = new AccountServiceTestFixture.RecordingEventPublisher();
        currentUserProvider = new AccountServiceTestFixture.FixedCurrentUserProvider();
        accountProperties = new AccountProperties(true, 0, null, null);
        ProfileService profileService = new ProfileService(
            userProfileRepository,
            eventPublisher,
            currentUserProvider,
            accountProperties
        );
        AddressService addressService = new AddressService(
            savedAddressRepository,
            userProfileRepository,
            eventPublisher,
            currentUserProvider
        );
        SavedConfigurationService savedConfigurationService = new SavedConfigurationService(
            savedConfigurationRepository,
            eventPublisher,
            currentUserProvider,
            new ConfigurationSnapshotValidator(new ObjectMapper())
        );
        service = new AccountApplicationService(profileService, addressService, savedConfigurationService);
    }

    @Test
    @DisplayName("TC-FUNC-PROF-001 / TC-ACCT-001: Get profile for existing user returns profile data")
    void getProfileExistingUser() {
        String userId = UUID.randomUUID().toString();
        UserProfile profile = createProfile(userId, "user@example.com");
        userProfileRepository.save(profile);
        currentUserProvider.setCurrentUser(new CurrentUser(userId, "user@example.com", "Jane", "Doe", null, Set.of("CUSTOMER")));

        UserProfileDto result = service.getProfile(new GetProfileQuery(UUID.fromString(userId)));

        assertEquals(userId, result.userId());
        assertEquals("user@example.com", result.email());
    }

    @Test
    @DisplayName("TC-FUNC-PROF-002 / TC-ACCT-005: First login creates profile from Keycloak claims")
    void getProfileCreatesOnFirstLogin() {
        String userId = UUID.randomUUID().toString();
        currentUserProvider.setCurrentUser(new CurrentUser(userId, "new@example.com", "New", "User", null, Set.of("CUSTOMER")));

        UserProfileDto result = service.getProfile(new GetProfileQuery(UUID.fromString(userId)));

        assertEquals(userId, result.userId());
        assertEquals("new@example.com", result.email());
        assertTrue(eventPublisher.events().stream().anyMatch(UserProfileCreatedEvent.class::isInstance));
    }

    @Test
    @DisplayName("TC-FUNC-PROF-003: Get profile for another user without ADMIN role returns unauthorized")
    void getProfileUnauthorizedForOtherUser() {
        String userId = UUID.randomUUID().toString();
        String otherUserId = UUID.randomUUID().toString();
        userProfileRepository.save(createProfile(otherUserId, "other@example.com"));
        currentUserProvider.setCurrentUser(new CurrentUser(userId, "user@example.com", "Jane", "Doe", null, Set.of("CUSTOMER")));

        DomainException ex = assertThrows(DomainException.class,
            () -> service.getProfile(new GetProfileQuery(UUID.fromString(otherUserId))));

        assertEquals(AccountDomainErrors.unauthorized("x").getCode(), ex.getCode());
    }

    @Test
    @DisplayName("TC-FUNC-PROF-004: ADMIN can access any user's profile")
    void getProfileAdminAccess() {
        String adminId = UUID.randomUUID().toString();
        String targetId = UUID.randomUUID().toString();
        userProfileRepository.save(createProfile(targetId, "target@example.com"));
        currentUserProvider.setCurrentUser(new CurrentUser(adminId, "admin@example.com", "Admin", "User", null, Set.of("ADMIN")));

        UserProfileDto result = service.getProfile(new GetProfileQuery(UUID.fromString(targetId)));

        assertEquals(targetId, result.userId());
    }

    @Test
    @DisplayName("TC-ACCT-002: Get profile for non-existent user returns NOT_FOUND when auto-create disabled")
    void getProfileNotFoundWhenAutoCreateDisabled() {
        String userId = UUID.randomUUID().toString();
        accountProperties = new AccountProperties(false, 0, null, null);
        ProfileService profileService = new ProfileService(
            userProfileRepository,
            eventPublisher,
            currentUserProvider,
            accountProperties
        );
        AddressService addressService = new AddressService(
            savedAddressRepository,
            userProfileRepository,
            eventPublisher,
            currentUserProvider
        );
        SavedConfigurationService savedConfigurationService = new SavedConfigurationService(
            savedConfigurationRepository,
            eventPublisher,
            currentUserProvider,
            new ConfigurationSnapshotValidator(new ObjectMapper())
        );
        service = new AccountApplicationService(profileService, addressService, savedConfigurationService);
        currentUserProvider.setCurrentUser(new CurrentUser(userId, "user@example.com", "Jane", "Doe", null, Set.of("CUSTOMER")));

        DomainException ex = assertThrows(DomainException.class,
            () -> service.getProfile(new GetProfileQuery(UUID.fromString(userId))));

        assertEquals(AccountDomainErrors.profileNotFound(userId).getCode(), ex.getCode());
    }

    @Test
    @DisplayName("TC-ACCT-004: Invalid email during profile creation returns validation error")
    void getProfileInvalidEmail() {
        String userId = UUID.randomUUID().toString();
        currentUserProvider.setCurrentUser(new CurrentUser(userId, "invalid-email", "Bad", "Email", null, Set.of("CUSTOMER")));

        assertThrows(IllegalArgumentException.class,
            () -> service.getProfile(new GetProfileQuery(UUID.fromString(userId))));
    }

    private UserProfile createProfile(String userId, String email) {
        Instant now = Instant.now();
        return UserProfile.create(
            Id.of(userId),
            Email.of(email),
            new PersonName("Jane", "Doe"),
            null,
            new LocalePreference("ru"),
            new CurrencyPreference("RUB"),
            NotificationPreferences.of(true, false, true),
            null,
            now
        );
    }
}
