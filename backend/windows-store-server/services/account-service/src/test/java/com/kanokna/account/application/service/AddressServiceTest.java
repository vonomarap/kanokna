package com.kanokna.account.application.service;

import com.kanokna.account.application.dto.AddAddressCommand;
import com.kanokna.account.application.dto.AddressDto;
import com.kanokna.account.application.dto.DeleteAddressCommand;
import com.kanokna.account.application.dto.ListAddressesQuery;
import com.kanokna.account.application.dto.SavedAddressDto;
import com.kanokna.account.application.dto.UpdateAddressCommand;
import com.kanokna.account.application.port.out.CurrentUser;
import com.kanokna.account.domain.event.AddressAddedEvent;
import com.kanokna.account.domain.event.AddressDeletedEvent;
import com.kanokna.account.domain.event.AddressUpdatedEvent;
import com.kanokna.account.domain.model.CurrencyPreference;
import com.kanokna.account.domain.model.LocalePreference;
import com.kanokna.account.domain.model.NotificationPreferences;
import com.kanokna.account.domain.model.PersonName;
import com.kanokna.account.domain.model.UserProfile;
import com.kanokna.account.support.AccountServiceTestFixture;
import com.kanokna.shared.core.Email;
import com.kanokna.shared.core.Id;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddressServiceTest {
    private AccountServiceTestFixture.InMemoryUserProfileRepository userProfileRepository;
    private AccountServiceTestFixture.InMemorySavedAddressRepository savedAddressRepository;
    private AccountServiceTestFixture.RecordingEventPublisher eventPublisher;
    private AccountServiceTestFixture.FixedCurrentUserProvider currentUserProvider;
    private AddressService service;

    @BeforeEach
    void setUp() {
        userProfileRepository = new AccountServiceTestFixture.InMemoryUserProfileRepository();
        savedAddressRepository = new AccountServiceTestFixture.InMemorySavedAddressRepository();
        eventPublisher = new AccountServiceTestFixture.RecordingEventPublisher();
        currentUserProvider = new AccountServiceTestFixture.FixedCurrentUserProvider();
        service = new AddressService(
            savedAddressRepository,
            userProfileRepository,
            eventPublisher,
            currentUserProvider
        );
    }

    @Test
    @DisplayName("addAddress_validInput_savesAndPublishesEvent")
    void addAddressValidInputSavesAndPublishesEvent() {
        UUID userId = UUID.randomUUID();
        seedProfile(userId);
        currentUserProvider.setCurrentUser(new CurrentUser(userId.toString(), "user@example.com", null, null, null, Set.of("CUSTOMER")));

        SavedAddressDto address = service.addAddress(new AddAddressCommand(userId, addressDto("Home"), "Home", true));

        assertNotNull(address.addressId());
        assertEquals("Home", address.label());
        assertTrue(address.isDefault());
        assertTrue(eventPublisher.events().stream().anyMatch(AddressAddedEvent.class::isInstance));
    }

    @Test
    @DisplayName("addAddress_setAsDefault_clearsOldDefault")
    void addAddressSetAsDefaultClearsOldDefault() {
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
    @DisplayName("updateAddress_validInput_updatesAndPublishesEvent")
    void updateAddressValidInputUpdatesAndPublishesEvent() {
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
        assertTrue(eventPublisher.events().stream().anyMatch(AddressUpdatedEvent.class::isInstance));
    }

    @Test
    @DisplayName("deleteAddress_existingAddress_deletesAndPublishesEvent")
    void deleteAddressExistingAddressDeletesAndPublishesEvent() {
        UUID userId = UUID.randomUUID();
        seedProfile(userId);
        currentUserProvider.setCurrentUser(new CurrentUser(userId.toString(), "user@example.com", null, null, null, Set.of("CUSTOMER")));

        SavedAddressDto saved = service.addAddress(new AddAddressCommand(userId, addressDto("Home"), "Home", false));
        service.deleteAddress(new DeleteAddressCommand(userId, UUID.fromString(saved.addressId())));

        assertTrue(service.listAddresses(new ListAddressesQuery(userId)).isEmpty());
        assertTrue(eventPublisher.events().stream().anyMatch(AddressDeletedEvent.class::isInstance));
    }

    @Test
    @DisplayName("listAddresses_returnsAddressesSortedByLabel")
    void listAddressesReturnsAddressesSortedByLabel() {
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
