package com.kanokna.account.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.account.adapters.config.AccountProperties;
import com.kanokna.account.application.dto.*;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccountApplicationServiceAddressTest {
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
    @DisplayName("TC-FUNC-ADDR-001 / TC-ACCT-006: Add address with valid input succeeds")
    void addAddressValid() {
        UUID userId = UUID.randomUUID();
        seedProfile(userId);
        currentUserProvider.setCurrentUser(new CurrentUser(userId.toString(), "user@example.com", null, null, null, Set.of("CUSTOMER")));

        SavedAddressDto address = service.addAddress(new AddAddressCommand(userId, addressDto("Home"), "Home", true));

        assertNotNull(address.addressId());
        assertEquals("Home", address.label());
        assertTrue(address.isDefault());
    }

    @Test
    @DisplayName("TC-FUNC-ADDR-002 / TC-ACCT-007: Add duplicate address returns ERR-ACCT-ADDRESS-DUPLICATE")
    void addAddressDuplicate() {
        UUID userId = UUID.randomUUID();
        seedProfile(userId);
        currentUserProvider.setCurrentUser(new CurrentUser(userId.toString(), "user@example.com", null, null, null, Set.of("CUSTOMER")));

        service.addAddress(new AddAddressCommand(userId, addressDto("Home"), "Home", true));

        DomainException ex = assertThrows(DomainException.class,
            () -> service.addAddress(new AddAddressCommand(userId, addressDto("Home"), "Home 2", false)));

        assertEquals(AccountDomainErrors.addressDuplicate("x").getCode(), ex.getCode());
    }

    @Test
    @DisplayName("TC-FUNC-ADDR-003 / TC-ACCT-008: Add address with setAsDefault clears previous default")
    void addAddressClearsDefault() {
        UUID userId = UUID.randomUUID();
        seedProfile(userId);
        currentUserProvider.setCurrentUser(new CurrentUser(userId.toString(), "user@example.com", null, null, null, Set.of("CUSTOMER")));

        service.addAddress(new AddAddressCommand(userId, addressDto("Home"), "Home", true));
        service.addAddress(new AddAddressCommand(userId, addressDto("Work"), "Work", true));

        List<SavedAddressDto> addresses = service.listAddresses(new ListAddressesQuery(userId));
        long defaults = addresses.stream().filter(SavedAddressDto::isDefault).count();
        assertEquals(1, defaults);
    }

    @Test
    @DisplayName("TC-FUNC-ADDR-004: Update address label succeeds")
    void updateAddressLabel() {
        UUID userId = UUID.randomUUID();
        seedProfile(userId);
        currentUserProvider.setCurrentUser(new CurrentUser(userId.toString(), "user@example.com", null, null, null, Set.of("CUSTOMER")));

        SavedAddressDto saved = service.addAddress(new AddAddressCommand(userId, addressDto("Home"), "Home", false));
        SavedAddressDto updated = service.updateAddress(new UpdateAddressCommand(
            userId,
            UUID.fromString(saved.addressId()),
            null,
            "Office",
            null
        ));

        assertEquals("Office", updated.label());
    }

    @Test
    @DisplayName("TC-FUNC-ADDR-005: Update non-existent address returns ERR-ACCT-ADDRESS-NOT-FOUND")
    void updateAddressNotFound() {
        UUID userId = UUID.randomUUID();
        seedProfile(userId);
        currentUserProvider.setCurrentUser(new CurrentUser(userId.toString(), "user@example.com", null, null, null, Set.of("CUSTOMER")));

        DomainException ex = assertThrows(DomainException.class,
            () -> service.updateAddress(new UpdateAddressCommand(
                userId,
                UUID.randomUUID(),
                addressDto("Home"),
                "Home",
                false
            )));

        assertEquals(AccountDomainErrors.addressNotFound("x").getCode(), ex.getCode());
    }

    @Test
    @DisplayName("TC-FUNC-ADDR-006 / TC-ACCT-009: Delete address removes from list")
    void deleteAddressRemoves() {
        UUID userId = UUID.randomUUID();
        seedProfile(userId);
        currentUserProvider.setCurrentUser(new CurrentUser(userId.toString(), "user@example.com", null, null, null, Set.of("CUSTOMER")));

        SavedAddressDto saved = service.addAddress(new AddAddressCommand(userId, addressDto("Home"), "Home", false));
        service.deleteAddress(new DeleteAddressCommand(userId, UUID.fromString(saved.addressId())));

        assertTrue(service.listAddresses(new ListAddressesQuery(userId)).isEmpty());
    }

    @Test
    @DisplayName("TC-FUNC-ADDR-007: List addresses returns all user's addresses sorted by label")
    void listAddressesSorted() {
        UUID userId = UUID.randomUUID();
        seedProfile(userId);
        currentUserProvider.setCurrentUser(new CurrentUser(userId.toString(), "user@example.com", null, null, null, Set.of("CUSTOMER")));

        service.addAddress(new AddAddressCommand(userId, addressDto("Work"), "Work", false));
        service.addAddress(new AddAddressCommand(userId, addressDto("Home"), "Home", false));

        List<SavedAddressDto> addresses = service.listAddresses(new ListAddressesQuery(userId));
        assertEquals(List.of("Home", "Work"), addresses.stream().map(SavedAddressDto::label).toList());
    }

    private void seedProfile(UUID userId) {
        Instant now = Instant.now();
        UserProfile profile = UserProfile.create(
            Id.of(userId.toString()),
            Email.of("user@example.com"),
            new PersonName("Jane", "Doe"),
            null,
            new LocalePreference("ru"),
            new CurrencyPreference("RUB"),
            NotificationPreferences.of(true, false, true),
            null,
            now
        );
        userProfileRepository.save(profile);
    }

    private AddressDto addressDto(String label) {
        return new AddressDto(
            "RU",
            "Moscow",
            "123456",
            "Main Street " + label,
            null
        );
    }
}
