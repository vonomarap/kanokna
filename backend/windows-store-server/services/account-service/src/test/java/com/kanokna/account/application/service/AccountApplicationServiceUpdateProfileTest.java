package com.kanokna.account.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.account.adapters.config.AccountProperties;
import com.kanokna.account.application.dto.UpdateProfileCommand;
import com.kanokna.account.application.dto.UserProfileDto;
import com.kanokna.account.application.port.out.CurrentUser;
import com.kanokna.account.domain.exception.AccountDomainErrors;
import com.kanokna.account.domain.model.*;
import com.kanokna.account.support.AccountServiceTestFixture;
import com.kanokna.shared.core.DomainException;
import com.kanokna.shared.core.Email;
import com.kanokna.shared.core.Id;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccountApplicationServiceUpdateProfileTest {
    private AccountServiceTestFixture.InMemoryUserProfileRepository userProfileRepository;
    private AccountServiceTestFixture.InMemorySavedAddressRepository savedAddressRepository;
    private AccountServiceTestFixture.InMemorySavedConfigurationRepository savedConfigurationRepository;
    private AccountServiceTestFixture.RecordingEventPublisher eventPublisher;
    private AccountServiceTestFixture.FixedCurrentUserProvider currentUserProvider;
    private AccountApplicationService service;

    @BeforeEach
    void setUp() {
        userProfileRepository = new AccountServiceTestFixture.InMemoryUserProfileRepository();
        savedAddressRepository = new AccountServiceTestFixture.InMemorySavedAddressRepository();
        savedConfigurationRepository = new AccountServiceTestFixture.InMemorySavedConfigurationRepository();
        eventPublisher = new AccountServiceTestFixture.RecordingEventPublisher();
        currentUserProvider = new AccountServiceTestFixture.FixedCurrentUserProvider();
        service = new AccountApplicationService(
            userProfileRepository,
            savedAddressRepository,
            savedConfigurationRepository,
            eventPublisher,
            currentUserProvider,
            new AccountProperties(),
            new ConfigurationSnapshotValidator(new ObjectMapper())
        );
    }

    @Test
    @DisplayName("TC-FUNC-UPD-001 / TC-ACCT-003: Update first and last name succeeds")
    void updateProfileName() {
        String userId = UUID.randomUUID().toString();
        userProfileRepository.save(createProfile(userId, "user@example.com"));
        currentUserProvider.setCurrentUser(new CurrentUser(userId, "user@example.com", "Old", "Name", null, Set.of("CUSTOMER")));

        UpdateProfileCommand command = new UpdateProfileCommand(UUID.fromString(userId),
            "New", "Name", null, null, null);
        UserProfileDto updated = service.updateProfile(command);

        assertEquals("New", updated.firstName());
        assertEquals("Name", updated.lastName());
    }

    @Test
    @DisplayName("TC-FUNC-UPD-002: Update phone number with valid format succeeds")
    void updateProfilePhoneValid() {
        String userId = UUID.randomUUID().toString();
        userProfileRepository.save(createProfile(userId, "user@example.com"));
        currentUserProvider.setCurrentUser(new CurrentUser(userId, "user@example.com", null, null, null, Set.of("CUSTOMER")));

        UpdateProfileCommand command = new UpdateProfileCommand(UUID.fromString(userId),
            null, null, "+7 (900) 123-45-67", null, null);
        UserProfileDto updated = service.updateProfile(command);

        assertEquals("+79001234567", updated.phoneNumber());
    }

    @Test
    @DisplayName("TC-FUNC-UPD-003: Update phone number with invalid format returns ERR-ACCT-INVALID-PHONE")
    void updateProfilePhoneInvalid() {
        String userId = UUID.randomUUID().toString();
        userProfileRepository.save(createProfile(userId, "user@example.com"));
        currentUserProvider.setCurrentUser(new CurrentUser(userId, "user@example.com", null, null, null, Set.of("CUSTOMER")));

        UpdateProfileCommand command = new UpdateProfileCommand(UUID.fromString(userId),
            null, null, "invalid", null, null);
        DomainException ex = assertThrows(DomainException.class, () -> service.updateProfile(command));

        assertEquals(AccountDomainErrors.invalidPhone("x").getCode(), ex.getCode());
    }

    @Test
    @DisplayName("TC-FUNC-UPD-004: Update with no fields provided returns validation error")
    void updateProfileNoFields() {
        String userId = UUID.randomUUID().toString();
        userProfileRepository.save(createProfile(userId, "user@example.com"));
        currentUserProvider.setCurrentUser(new CurrentUser(userId, "user@example.com", null, null, null, Set.of("CUSTOMER")));

        UpdateProfileCommand command = new UpdateProfileCommand(UUID.fromString(userId),
            null, null, null, null, null);
        assertThrows(IllegalArgumentException.class, () -> service.updateProfile(command));
    }

    @Test
    @DisplayName("TC-FUNC-UPD-005: Concurrent updates with version conflict are handled")
    void updateProfileOptimisticLock() {
        String userId = UUID.randomUUID().toString();
        userProfileRepository.save(createProfile(userId, "user@example.com"));
        userProfileRepository.setFailOnSave(true);
        currentUserProvider.setCurrentUser(new CurrentUser(userId, "user@example.com", null, null, null, Set.of("CUSTOMER")));

        UpdateProfileCommand command = new UpdateProfileCommand(UUID.fromString(userId),
            "New", null, null, null, null);
        DomainException ex = assertThrows(DomainException.class, () -> service.updateProfile(command));

        assertEquals(AccountDomainErrors.concurrentModification(userId).getCode(), ex.getCode());
    }

    private UserProfile createProfile(String userId, String email) {
        Instant now = Instant.now();
        return UserProfile.create(
            Id.of(userId),
            Email.of(email),
            new PersonName("Old", "Name"),
            null,
            new LocalePreference("ru"),
            new CurrencyPreference("RUB"),
            NotificationPreferences.of(true, false, true),
            null,
            now
        );
    }
}
