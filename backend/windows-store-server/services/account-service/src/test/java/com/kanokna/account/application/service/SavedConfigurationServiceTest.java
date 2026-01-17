package com.kanokna.account.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.account.application.dto.DeleteConfigurationCommand;
import com.kanokna.account.application.dto.ListConfigurationsQuery;
import com.kanokna.account.application.dto.SaveConfigurationCommand;
import com.kanokna.account.application.dto.SavedConfigurationDto;
import com.kanokna.account.application.port.out.CurrentUser;
import com.kanokna.account.domain.event.ConfigurationDeletedEvent;
import com.kanokna.account.domain.event.ConfigurationSavedEvent;
import com.kanokna.account.domain.exception.AccountDomainErrors;
import com.kanokna.account.support.AccountServiceTestFixture;
import com.kanokna.shared.core.DomainException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SavedConfigurationServiceTest {
    private AccountServiceTestFixture.InMemorySavedConfigurationRepository savedConfigurationRepository;
    private AccountServiceTestFixture.RecordingEventPublisher eventPublisher;
    private AccountServiceTestFixture.FixedCurrentUserProvider currentUserProvider;
    private SavedConfigurationService service;

    @BeforeEach
    void setUp() {
        savedConfigurationRepository = new AccountServiceTestFixture.InMemorySavedConfigurationRepository();
        eventPublisher = new AccountServiceTestFixture.RecordingEventPublisher();
        currentUserProvider = new AccountServiceTestFixture.FixedCurrentUserProvider();
        service = new SavedConfigurationService(
            savedConfigurationRepository,
            eventPublisher,
            currentUserProvider,
            new ConfigurationSnapshotValidator(new ObjectMapper())
        );
    }

    @Test
    @DisplayName("saveConfiguration_validInput_savesAndPublishesEvent")
    void saveConfigurationValidInputSavesAndPublishesEvent() {
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
        assertTrue(eventPublisher.events().stream().anyMatch(ConfigurationSavedEvent.class::isInstance));
    }

    @Test
    @DisplayName("saveConfiguration_invalidSnapshot_throwsException")
    void saveConfigurationInvalidSnapshotThrowsException() {
        UUID userId = UUID.randomUUID();
        currentUserProvider.setCurrentUser(new CurrentUser(userId.toString(), "user@example.com", null, null, null, Set.of("CUSTOMER")));

        SaveConfigurationCommand command = new SaveConfigurationCommand(
            userId,
            "Config B",
            UUID.randomUUID(),
            "{\"bad\":\"json\"}",
            null
        );

        DomainException ex = assertThrows(DomainException.class, () -> service.saveConfiguration(command));
        assertEquals(AccountDomainErrors.invalidConfigSnapshot("x").getCode(), ex.getCode());
    }

    @Test
    @DisplayName("listConfigurations_returnsUserConfigurations")
    void listConfigurationsReturnsUserConfigurations() {
        UUID userId = UUID.randomUUID();
        UUID productTemplateId = UUID.randomUUID();
        currentUserProvider.setCurrentUser(new CurrentUser(userId.toString(), "user@example.com", null, null, null, Set.of("CUSTOMER")));

        service.saveConfiguration(new SaveConfigurationCommand(
            userId,
            "Config C",
            productTemplateId,
            validSnapshot(productTemplateId),
            null
        ));

        List<SavedConfigurationDto> configs = service.listConfigurations(new ListConfigurationsQuery(userId));
        assertEquals(1, configs.size());
        assertEquals("Config C", configs.get(0).name());
    }

    @Test
    @DisplayName("deleteConfiguration_existingConfig_deletesAndPublishesEvent")
    void deleteConfigurationExistingConfigDeletesAndPublishesEvent() {
        UUID userId = UUID.randomUUID();
        UUID productTemplateId = UUID.randomUUID();
        currentUserProvider.setCurrentUser(new CurrentUser(userId.toString(), "user@example.com", null, null, null, Set.of("CUSTOMER")));

        SavedConfigurationDto saved = service.saveConfiguration(new SaveConfigurationCommand(
            userId,
            "Config D",
            productTemplateId,
            validSnapshot(productTemplateId),
            null
        ));

        service.deleteConfiguration(new DeleteConfigurationCommand(userId, UUID.fromString(saved.configurationId())));

        assertTrue(service.listConfigurations(new ListConfigurationsQuery(userId)).isEmpty());
        assertTrue(eventPublisher.events().stream().anyMatch(ConfigurationDeletedEvent.class::isInstance));
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
