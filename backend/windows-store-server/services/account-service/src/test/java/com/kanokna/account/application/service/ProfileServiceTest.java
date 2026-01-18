package com.kanokna.account.application.service;

import com.kanokna.account.adapters.config.AccountProperties;
import com.kanokna.account.application.dto.GetProfileQuery;
import com.kanokna.account.application.dto.UpdateProfileCommand;
import com.kanokna.account.application.dto.UserProfileDto;
import com.kanokna.account.application.port.out.CurrentUser;
import com.kanokna.account.domain.event.UserProfileCreatedEvent;
import com.kanokna.account.domain.event.UserProfileUpdatedEvent;
import com.kanokna.account.domain.exception.AccountDomainErrors;
import com.kanokna.account.domain.model.CurrencyPreference;
import com.kanokna.account.domain.model.LocalePreference;
import com.kanokna.account.domain.model.NotificationPreferences;
import com.kanokna.account.domain.model.PersonName;
import com.kanokna.account.domain.model.UserProfile;
import com.kanokna.account.support.AccountServiceTestFixture;
import com.kanokna.shared.core.DomainException;
import com.kanokna.shared.core.Email;
import com.kanokna.shared.core.Id;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfileServiceTest {
    private AccountServiceTestFixture.InMemoryUserProfileRepository userProfileRepository;
    private AccountServiceTestFixture.RecordingEventPublisher eventPublisher;
    private AccountServiceTestFixture.FixedCurrentUserProvider currentUserProvider;
    private AccountProperties accountProperties;
    private ProfileService service;

    @BeforeEach
    void setUp() {
        userProfileRepository = new AccountServiceTestFixture.InMemoryUserProfileRepository();
        eventPublisher = new AccountServiceTestFixture.RecordingEventPublisher();
        currentUserProvider = new AccountServiceTestFixture.FixedCurrentUserProvider();
        accountProperties = new AccountProperties(true, 0, null, null);
        service = new ProfileService(
            userProfileRepository,
            eventPublisher,
            currentUserProvider,
            accountProperties
        );
    }

    @Test
    @DisplayName("getProfile_existingUser_returnsProfile")
    void getProfileExistingUserReturnsProfile() {
        String userId = UUID.randomUUID().toString();
        UserProfile profile = createProfile(userId, "user@example.com");
        userProfileRepository.save(profile);
        currentUserProvider.setCurrentUser(new CurrentUser(userId, "user@example.com", "Jane", "Doe", null, Set.of("CUSTOMER")));

        UserProfileDto result = service.getProfile(new GetProfileQuery(UUID.fromString(userId)));

        assertEquals(userId, result.userId());
        assertEquals("user@example.com", result.email());
    }

    @Test
    @DisplayName("getProfile_newUser_autoCreatesFromKeycloak")
    void getProfileNewUserAutoCreatesFromKeycloak() {
        String userId = UUID.randomUUID().toString();
        currentUserProvider.setCurrentUser(new CurrentUser(userId, "new@example.com", "New", "User", null, Set.of("CUSTOMER")));

        UserProfileDto result = service.getProfile(new GetProfileQuery(UUID.fromString(userId)));

        assertEquals(userId, result.userId());
        assertEquals("new@example.com", result.email());
        assertTrue(eventPublisher.events().stream().anyMatch(UserProfileCreatedEvent.class::isInstance));
    }

    @Test
    @DisplayName("updateProfile_validInput_updatesAndPublishesEvent")
    void updateProfileValidInputUpdatesAndPublishesEvent() {
        String userId = UUID.randomUUID().toString();
        userProfileRepository.save(createProfile(userId, "user@example.com"));
        currentUserProvider.setCurrentUser(new CurrentUser(userId, "user@example.com", "Old", "Name", null, Set.of("CUSTOMER")));

        UpdateProfileCommand command = new UpdateProfileCommand(UUID.fromString(userId),
            "New", "Name", null, null, null);
        UserProfileDto updated = service.updateProfile(command);

        assertEquals("New", updated.firstName());
        assertEquals("Name", updated.lastName());
        assertTrue(eventPublisher.events().stream().anyMatch(UserProfileUpdatedEvent.class::isInstance));
    }

    @Test
    @DisplayName("updateProfile_invalidPhone_throwsException")
    void updateProfileInvalidPhoneThrowsException() {
        String userId = UUID.randomUUID().toString();
        userProfileRepository.save(createProfile(userId, "user@example.com"));
        currentUserProvider.setCurrentUser(new CurrentUser(userId, "user@example.com", null, null, null, Set.of("CUSTOMER")));

        UpdateProfileCommand command = new UpdateProfileCommand(UUID.fromString(userId),
            null, null, "invalid", null, null);
        DomainException ex = assertThrows(DomainException.class, () -> service.updateProfile(command));

        assertEquals(AccountDomainErrors.invalidPhone("x").getCode(), ex.getCode());
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
