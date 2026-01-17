package com.kanokna.account.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.account.adapters.config.AccountProperties;
import com.kanokna.account.application.dto.DeleteConfigurationCommand;
import com.kanokna.account.application.dto.ListConfigurationsQuery;
import com.kanokna.account.application.dto.SaveConfigurationCommand;
import com.kanokna.account.application.dto.SavedConfigurationDto;
import com.kanokna.account.application.port.out.CurrentUser;
import com.kanokna.account.domain.event.ConfigurationSavedEvent;
import com.kanokna.account.domain.exception.AccountDomainErrors;
import com.kanokna.account.support.AccountServiceTestFixture;
import com.kanokna.shared.core.DomainException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccountApplicationServiceConfigurationTest {
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
        ProfileService profileService = new ProfileService(
            userProfileRepository,
            eventPublisher,
            currentUserProvider,
            new AccountProperties()
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
    @DisplayName("TC-FUNC-CFG-001 / TC-ACCT-011: Save valid configuration returns configurationId")
    void saveConfigurationValid() {
        UUID userId = UUID.randomUUID();
        UUID productTemplateId = UUID.randomUUID();
        currentUserProvider.setCurrentUser(new CurrentUser(userId.toString(), "user@example.com", null, null, null, Set.of("CUSTOMER")));

        SavedConfigurationDto saved = service.saveConfiguration(new SaveConfigurationCommand(
            userId,
            "Config A",
            productTemplateId,
            validSnapshot(productTemplateId),
            null
        ));

        assertNotNull(saved.configurationId());
        assertEquals("Config A", saved.name());
    }

    @Test
    @DisplayName("TC-FUNC-CFG-002 / TC-ACCT-012: Invalid JSON returns ERR-ACCT-INVALID-CONFIG-SNAPSHOT")
    void saveConfigurationInvalidSnapshot() {
        UUID userId = UUID.randomUUID();
        currentUserProvider.setCurrentUser(new CurrentUser(userId.toString(), "user@example.com", null, null, null, Set.of("CUSTOMER")));

        SaveConfigurationCommand command = new SaveConfigurationCommand(
            userId,
            "Config A",
            UUID.randomUUID(),
            "{\"bad\":\"json\"}",
            null
        );

        DomainException ex = assertThrows(DomainException.class, () -> service.saveConfiguration(command));
        assertEquals(AccountDomainErrors.invalidConfigSnapshot("x").getCode(), ex.getCode());
    }

    @Test
    @DisplayName("TC-FUNC-CFG-003: Save configuration emits ConfigurationSavedEvent")
    void saveConfigurationEmitsEvent() {
        UUID userId = UUID.randomUUID();
        UUID productTemplateId = UUID.randomUUID();
        currentUserProvider.setCurrentUser(new CurrentUser(userId.toString(), "user@example.com", null, null, null, Set.of("CUSTOMER")));

        service.saveConfiguration(new SaveConfigurationCommand(
            userId,
            "Config B",
            productTemplateId,
            validSnapshot(productTemplateId),
            null
        ));

        assertTrue(eventPublisher.events().stream().anyMatch(ConfigurationSavedEvent.class::isInstance));
    }

    @Test
    @DisplayName("TC-FUNC-CFG-004: Save configuration with quote snapshot persists both")
    void saveConfigurationWithQuoteSnapshot() {
        UUID userId = UUID.randomUUID();
        UUID productTemplateId = UUID.randomUUID();
        currentUserProvider.setCurrentUser(new CurrentUser(userId.toString(), "user@example.com", null, null, null, Set.of("CUSTOMER")));

        service.saveConfiguration(new SaveConfigurationCommand(
            userId,
            "Config C",
            productTemplateId,
            validSnapshot(productTemplateId),
            "{\"total\":\"1000 RUB\"}"
        ));

        List<SavedConfigurationDto> configs = service.listConfigurations(new ListConfigurationsQuery(userId));
        assertEquals(1, configs.size());
        assertEquals("{\"total\":\"1000 RUB\"}", configs.get(0).quoteSnapshot());
    }

    @Test
    @DisplayName("TC-ACCT-013: List saved configurations returns all user's configs")
    void listConfigurationsReturnsAll() {
        UUID userId = UUID.randomUUID();
        UUID productTemplateId = UUID.randomUUID();
        currentUserProvider.setCurrentUser(new CurrentUser(userId.toString(), "user@example.com", null, null, null, Set.of("CUSTOMER")));

        service.saveConfiguration(new SaveConfigurationCommand(
            userId,
            "Config X",
            productTemplateId,
            validSnapshot(productTemplateId),
            null
        ));

        List<SavedConfigurationDto> configs = service.listConfigurations(new ListConfigurationsQuery(userId));
        assertEquals(1, configs.size());
        assertEquals("Config X", configs.get(0).name());
    }

    @Test
    @DisplayName("TC-FUNC-CFG-005: Unauthenticated save attempt returns unauthorized")
    void saveConfigurationUnauthenticated() {
        UUID userId = UUID.randomUUID();
        currentUserProvider.setCurrentUser(null);

        SaveConfigurationCommand command = new SaveConfigurationCommand(
            userId,
            "Config D",
            UUID.randomUUID(),
            validSnapshot(),
            null
        );

        DomainException ex = assertThrows(DomainException.class, () -> service.saveConfiguration(command));
        assertEquals(AccountDomainErrors.unauthorized("x").getCode(), ex.getCode());
    }

    @Test
    @DisplayName("TC-ACCT-014: Delete saved configuration removes from list")
    void deleteConfigurationRemoves() {
        UUID userId = UUID.randomUUID();
        UUID productTemplateId = UUID.randomUUID();
        currentUserProvider.setCurrentUser(new CurrentUser(userId.toString(), "user@example.com", null, null, null, Set.of("CUSTOMER")));

        SavedConfigurationDto saved = service.saveConfiguration(new SaveConfigurationCommand(
            userId,
            "Config E",
            productTemplateId,
            validSnapshot(productTemplateId),
            null
        ));

        service.deleteConfiguration(new DeleteConfigurationCommand(userId, UUID.fromString(saved.configurationId())));

        assertTrue(service.listConfigurations(new ListConfigurationsQuery(userId)).isEmpty());
    }

    @Test
    @DisplayName("TC-ACCT-015: User cannot access another user's configurations")
    void listConfigurationsUnauthorized() {
        UUID userId = UUID.randomUUID();
        UUID otherUserId = UUID.randomUUID();
        currentUserProvider.setCurrentUser(new CurrentUser(userId.toString(), "user@example.com", null, null, null, Set.of("CUSTOMER")));

        DomainException ex = assertThrows(DomainException.class,
            () -> service.listConfigurations(new ListConfigurationsQuery(otherUserId)));
        assertEquals(AccountDomainErrors.unauthorized("x").getCode(), ex.getCode());
    }

    private String validSnapshot() {
        return validSnapshot(UUID.randomUUID());
    }

    private String validSnapshot(UUID productTemplateId) {
        return "{"
            + "\"productTemplateId\":\"" + productTemplateId + "\","
            + "\"dimensions\":{\"width\":100,\"height\":120,\"unit\":\"mm\"},"
            + "\"selectedOptions\":[{\"optionGroupId\":\"" + UUID.randomUUID()
            + "\",\"optionId\":\"" + UUID.randomUUID() + "\",\"value\":\"x\"}],"
            + "\"accessories\":[{\"accessoryId\":\"" + UUID.randomUUID() + "\",\"quantity\":1}]"
            + "}";
    }
}
