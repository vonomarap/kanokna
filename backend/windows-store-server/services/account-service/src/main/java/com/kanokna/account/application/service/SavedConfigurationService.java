package com.kanokna.account.application.service;

import com.kanokna.account.application.dto.DeleteConfigurationCommand;
import com.kanokna.account.application.dto.ListConfigurationsQuery;
import com.kanokna.account.application.dto.SaveConfigurationCommand;
import com.kanokna.account.application.dto.SavedConfigurationDto;
import com.kanokna.account.application.port.out.CurrentUser;
import com.kanokna.account.application.port.out.CurrentUserProvider;
import com.kanokna.account.application.port.out.EventPublisher;
import com.kanokna.account.application.port.out.SavedConfigurationRepository;
import com.kanokna.account.domain.event.ConfigurationDeletedEvent;
import com.kanokna.account.domain.event.ConfigurationSavedEvent;
import com.kanokna.account.domain.exception.AccountDomainErrors;
import com.kanokna.account.domain.model.ConfigurationSnapshot;
import com.kanokna.account.domain.model.QuoteSnapshot;
import com.kanokna.account.domain.model.SavedConfiguration;
import com.kanokna.shared.core.Id;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/* <FUNCTION_CONTRACT id="FC-account-saveConfiguration"
        LAYER="application.service"
        INTENT="Save a product configuration as draft for later retrieval and checkout"
        INPUT="SaveConfigurationCommand(userId: UUID, name: String, productTemplateId: UUID, configurationSnapshot: JSON, quoteSnapshot?: JSON)"
        OUTPUT="SavedConfigurationDTO(configurationId: UUID)"
        SIDE_EFFECTS="Persists configuration, emits ConfigurationSavedEvent"
        LINKS="RequirementsAnalysis.xml#UC-B2C-CPQ-01;DevelopmentPlan.xml#DP-SVC-account-service;DevelopmentPlan.xml#Flow-B2C-CPQ">

        <PRECONDITIONS>
            <Item>userId is a valid UUID of authenticated user</Item>
            <Item>name is non-blank string (max 255 chars)</Item>
            <Item>productTemplateId is non-null UUID</Item>
            <Item>configurationSnapshot is valid JSON matching expected schema</Item>
            <Item>User is authenticated (cannot save anonymous configurations to account)</Item>
        </PRECONDITIONS>

        <POSTCONDITIONS>
            <Item>SavedConfiguration entity is persisted with generated configurationId</Item>
            <Item>createdAt and updatedAt timestamps are set</Item>
            <Item>ConfigurationSavedEvent is published</Item>
            <Item>Returns SavedConfigurationDTO with configurationId</Item>
        </POSTCONDITIONS>

        <INVARIANTS>
            <Item>configurationId is globally unique UUID</Item>
            <Item>User can have unlimited saved configurations (no limit in MVP)</Item>
            <Item>Saving same configuration twice creates separate entries (no dedup)</Item>
        </INVARIANTS>

        <ERROR_HANDLING>
            <Item type="BUSINESS" code="ERR-ACCT-INVALID-CONFIG-NAME">Name is blank or exceeds max
                length</Item>
            <Item type="BUSINESS" code="ERR-ACCT-INVALID-CONFIG-SNAPSHOT">Configuration snapshot
                fails schema validation</Item>
            <Item type="BUSINESS" code="ERR-ACCT-UNAUTHORIZED">User not authenticated</Item>
        </ERROR_HANDLING>

        <CONFIGURATION_SNAPSHOT_SCHEMA>
            <Description>Expected structure of configurationSnapshot JSONB field</Description>
            <Schema>
                {
                "productTemplateId": "UUID",
                "dimensions": {
                "width": "number (mm)",
                "height": "number (mm)",
                "unit": "mm"
                },
                "selectedOptions": [
                {
                "optionGroupId": "UUID",
                "optionId": "UUID",
                "value": "any (depends on option type)"
                }
                ],
                "accessories": [
                {
                "accessoryId": "UUID",
                "quantity": "integer >= 1"
                }
                ],
                "notes": "optional string"
                }
            </Schema>
        </CONFIGURATION_SNAPSHOT_SCHEMA>

        <BLOCK_ANCHORS>
            <Item id="BA-ACCT-CFG-01">Validate configuration snapshot structure</Item>
            <Item id="BA-ACCT-CFG-02">Persist saved configuration</Item>
            <Item id="BA-ACCT-CFG-03">Emit ConfigurationSavedEvent</Item>
        </BLOCK_ANCHORS>

        <LOGGING>
            <Item>[SVC=account-service][UC=UC-B2C-CPQ-01][BLOCK=BA-ACCT-CFG-01][[STATE=VALIDATING]
                eventType=CONFIG_SAVE_START decision=VALIDATE
                keyValues=userId={uuid},productTemplateId={uuid},name={name}</Item>
            <Item>[SVC=account-service][UC=UC-B2C-CPQ-01][BLOCK=BA-ACCT-CFG-01][[STATE=INVALID]
                eventType=CONFIG_SAVE decision=REJECT reason=SCHEMA_VALIDATION_FAILED
                keyValues=userId={uuid},errors={list}</Item>
            <Item>[SVC=account-service][UC=UC-B2C-CPQ-01][BLOCK=BA-ACCT-CFG-02][[STATE=PERSISTING]
                eventType=CONFIG_SAVE decision=PERSIST keyValues=userId={uuid},configId={uuid}</Item>
            <Item>[SVC=account-service][UC=UC-B2C-CPQ-01][BLOCK=BA-ACCT-CFG-03][[STATE=COMPLETE]
                eventType=CONFIG_SAVED decision=SUCCESS
                keyValues=userId={uuid},configId={uuid},productTemplateId={uuid}</Item>
        </LOGGING>

        <TESTS>
            <Case id="TC-FUNC-CFG-001">Save valid configuration returns configurationId</Case>
            <Case id="TC-FUNC-CFG-002">Save configuration with invalid JSON returns
                ERR-ACCT-INVALID-CONFIG-SNAPSHOT</Case>
            <Case id="TC-FUNC-CFG-003">Save configuration emits ConfigurationSavedEvent</Case>
            <Case id="TC-FUNC-CFG-004">Save configuration with quote snapshot persists both</Case>
            <Case id="TC-FUNC-CFG-005">Unauthenticated save attempt returns UNAUTHORIZED</Case>
        </TESTS>
    </FUNCTION_CONTRACT> */

/**
 * MODULE_CONTRACT id="MC-account-savedconfig-service"
 * LAYER="application.service"
 * INTENT="Specialized service for saved product configuration operations"
 * LINKS="Technology.xml#DEC-ACCOUNT-DECOMPOSITION;RequirementsAnalysis.xml#UC-B2C-CPQ-01"
 */
@Service
@Transactional
public class SavedConfigurationService {
    private static final Logger log = LoggerFactory.getLogger(SavedConfigurationService.class);
    private static final String USE_CASE_CPQ = "UC-B2C-CPQ-01";

    private final SavedConfigurationRepository savedConfigurationRepository;
    private final EventPublisher eventPublisher;
    private final CurrentUserProvider currentUserProvider;
    private final ConfigurationSnapshotValidator configurationSnapshotValidator;

    public SavedConfigurationService(
        SavedConfigurationRepository savedConfigurationRepository,
        EventPublisher eventPublisher,
        CurrentUserProvider currentUserProvider,
        ConfigurationSnapshotValidator configurationSnapshotValidator
    ) {
        this.savedConfigurationRepository = savedConfigurationRepository;
        this.eventPublisher = eventPublisher;
        this.currentUserProvider = currentUserProvider;
        this.configurationSnapshotValidator = configurationSnapshotValidator;
    }

    public SavedConfigurationDto saveConfiguration(SaveConfigurationCommand command) {
        Id userId = Id.of(command.userId().toString());
        CurrentUser currentUser = AccountServiceUtils.requireCurrentUser(currentUserProvider);
        AccountServiceUtils.authorizeAccess(currentUser, userId);

        String name = command.name();
        if (name == null || name.isBlank() || name.strip().length() > 255) {
            throw AccountDomainErrors.invalidConfigName(name);
        }

        UUID productTemplateId = command.productTemplateId();
        if (productTemplateId == null) {
            throw AccountDomainErrors.invalidConfigSnapshot("productTemplateId is required");
        }

        // BA-ACCT-CFG-01: Validate configuration snapshot structure
        log.info(AccountServiceUtils.logLine(USE_CASE_CPQ, "BA-ACCT-CFG-01", "VALIDATING",
            "CONFIG_SAVE_START", "VALIDATE",
            "userId=" + userId.value() + ",productTemplateId=" + productTemplateId + ",name=" + name.strip()));

        try {
            configurationSnapshotValidator.validate(productTemplateId, command.configurationSnapshot());
        } catch (Exception ex) {
            log.info("[SVC=account-service][UC=UC-B2C-CPQ-01][BLOCK=BA-ACCT-CFG-01][STATE=INVALID]"
                + " eventType=CONFIG_SAVE decision=REJECT reason=SCHEMA_VALIDATION_FAILED"
                + " keyValues=userId=" + userId.value());
            throw ex;
        }

        Instant now = Instant.now();
        SavedConfiguration configuration = new SavedConfiguration(
            Id.random(),
            userId,
            name.strip(),
            Id.of(productTemplateId.toString()),
            new ConfigurationSnapshot(command.configurationSnapshot()),
            command.quoteSnapshot() != null ? new QuoteSnapshot(command.quoteSnapshot()) : null,
            now,
            now
        );

        // BA-ACCT-CFG-02: Persist saved configuration
        log.info(AccountServiceUtils.logLine(USE_CASE_CPQ, "BA-ACCT-CFG-02", "PERSISTING",
            "CONFIG_SAVE", "PERSIST", "userId=" + userId.value() + ",configId=" + configuration.configurationId().value()));

        SavedConfiguration saved = savedConfigurationRepository.save(configuration);

        // BA-ACCT-CFG-03: Emit ConfigurationSavedEvent
        ConfigurationSavedEvent event = ConfigurationSavedEvent.create(
            userId.value(),
            saved.configurationId().value(),
            saved.productTemplateId().value(),
            1L
        );
        eventPublisher.publish(event.shortType(), event);

        log.info(AccountServiceUtils.logLine(USE_CASE_CPQ, "BA-ACCT-CFG-03", "COMPLETE",
            "CONFIG_SAVED", "SUCCESS",
            "userId=" + userId.value() + ",configId=" + saved.configurationId().value()
                + ",productTemplateId=" + saved.productTemplateId().value()));

        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SavedConfigurationDto> listConfigurations(ListConfigurationsQuery query) {
        Id userId = Id.of(query.userId().toString());
        CurrentUser currentUser = AccountServiceUtils.requireCurrentUser(currentUserProvider);
        AccountServiceUtils.authorizeAccess(currentUser, userId);
        return savedConfigurationRepository.findByUserId(userId)
            .stream()
            .map(this::toDto)
            .toList();
    }

    public void deleteConfiguration(DeleteConfigurationCommand command) {
        Id userId = Id.of(command.userId().toString());
        CurrentUser currentUser = AccountServiceUtils.requireCurrentUser(currentUserProvider);
        AccountServiceUtils.authorizeAccess(currentUser, userId);

        UUID configurationId = command.configurationId();
        SavedConfiguration existing = savedConfigurationRepository
            .findByUserIdAndId(userId, Id.of(configurationId.toString()))
            .orElseThrow(() -> new IllegalArgumentException("Saved configuration not found"));

        savedConfigurationRepository.deleteByUserIdAndId(userId, Id.of(configurationId.toString()));
        ConfigurationDeletedEvent event = ConfigurationDeletedEvent.create(
            userId.value(),
            existing.configurationId().value(),
            1L
        );
        eventPublisher.publish(event.shortType(), event);
    }

    private SavedConfigurationDto toDto(SavedConfiguration configuration) {
        return new SavedConfigurationDto(
            configuration.configurationId().value(),
            configuration.name(),
            configuration.productTemplateId().value(),
            configuration.configurationSnapshot().json(),
            configuration.quoteSnapshot() != null ? configuration.quoteSnapshot().json() : null,
            configuration.createdAt(),
            configuration.updatedAt()
        );
    }
}
