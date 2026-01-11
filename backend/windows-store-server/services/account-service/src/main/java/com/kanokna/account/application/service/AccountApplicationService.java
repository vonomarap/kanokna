package com.kanokna.account.application.service;

/* <FUNCTION_CONTRACT id="FC-account-getProfile"
        LAYER="application.service"
        INTENT="Retrieve user profile by userId, creating from Keycloak claims if first access"
        INPUT="GetProfileQuery(userId: UUID)"
        OUTPUT="UserProfileDTO"
        SIDE_EFFECTS="May create profile on first access (write)"
        LINKS="RequirementsAnalysis.xml#UC-ACCOUNT-MANAGE-PROFILE;DevelopmentPlan.xml#DP-SVC-account-service">

        <PRECONDITIONS>
            <Item>userId is a valid UUID (from JWT sub claim)</Item>
            <Item>Caller is authenticated (JWT token present)</Item>
            <Item>Caller userId matches requested userId OR caller has ADMIN role</Item>
        </PRECONDITIONS>

        <POSTCONDITIONS>
            <Item>Returns UserProfileDTO with all profile fields populated</Item>
            <Item>If profile did not exist, it is created from Keycloak claims and
                UserProfileCreatedEvent is emitted</Item>
            <Item>Profile includes list of saved addresses</Item>
        </POSTCONDITIONS>

        <INVARIANTS>
            <Item>Profile is always returned for valid authenticated user (auto-create on first
                access)</Item>
            <Item>Read operation is idempotent for existing profiles</Item>
        </INVARIANTS>

        <ERROR_HANDLING>
            <Item type="BUSINESS" code="ERR-ACCT-UNAUTHORIZED">Caller attempting to access another
                user's profile without ADMIN role</Item>
            <Item type="TECHNICAL" code="ERR-ACCT-KEYCLOAK-UNAVAILABLE">Cannot fetch claims from
                Keycloak (rare, fallback to minimal profile)</Item>
        </ERROR_HANDLING>

        <BLOCK_ANCHORS>
            <Item id="BA-ACCT-PROF-01">Fetch user profile from repository</Item>
            <Item id="BA-ACCT-PROF-03">Sync profile from Keycloak on first login</Item>
        </BLOCK_ANCHORS>

        <LOGGING>
            <Item>[SVC=account-service][UC=UC-ACCOUNT-MANAGE-PROFILE][BLOCK=BA-ACCT-PROF-01][STATE=FETCHING]
                eventType=PROFILE_GET decision=LOOKUP keyValues=userId={uuid}</Item>
            <Item>[SVC=account-service][UC=UC-ACCOUNT-MANAGE-PROFILE][BLOCK=BA-ACCT-PROF-03][STATE=SYNCING]
                eventType=PROFILE_SYNC decision=CREATE_FROM_KEYCLOAK
                keyValues=userId={uuid},email={masked}</Item>
            <Item>[SVC=account-service][UC=UC-ACCOUNT-MANAGE-PROFILE][BLOCK=BA-ACCT-PROF-01][STATE=COMPLETE]
                eventType=PROFILE_GET decision=SUCCESS keyValues=userId={uuid},addressCount={n}</Item>
        </LOGGING>

        <TESTS>
            <Case id="TC-FUNC-PROF-001">Get profile for existing user returns complete
                UserProfileDTO</Case>
            <Case id="TC-FUNC-PROF-002">Get profile for new user creates profile from Keycloak
                claims</Case>
            <Case id="TC-FUNC-PROF-003">Get profile for another user without ADMIN role returns
                UNAUTHORIZED</Case>
            <Case id="TC-FUNC-PROF-004">ADMIN can access any user's profile</Case>
        </TESTS>

        <gRPC_MAPPING>
            <Method>AccountService.GetProfile</Method>
            <ProtoRequest>GetProfileRequest</ProtoRequest>
            <ProtoResponse>GetProfileResponse</ProtoResponse>
        </gRPC_MAPPING>
    </FUNCTION_CONTRACT> */

/* <FUNCTION_CONTRACT id="FC-account-updateProfile"
        LAYER="application.service"
        INTENT="Update user profile fields (name, phone, preferences)"
        INPUT="UpdateProfileCommand(userId: UUID, firstName?: String, lastName?: String, phoneNumber?: String, preferredLanguage?: String, preferredCurrency?: String)"
        OUTPUT="UserProfileDTO"
        SIDE_EFFECTS="Persists profile changes, emits UserProfileUpdatedEvent"
        LINKS="RequirementsAnalysis.xml#UC-ACCOUNT-MANAGE-PROFILE;DevelopmentPlan.xml#DP-SVC-account-service">

        <PRECONDITIONS>
            <Item>userId is a valid UUID</Item>
            <Item>Profile exists for userId (auto-created on first getProfile)</Item>
            <Item>Caller is authenticated and owns this profile OR has ADMIN role</Item>
            <Item>At least one update field is provided</Item>
            <Item>If provided, phoneNumber matches valid phone format</Item>
            <Item>If provided, preferredLanguage is valid BCP-47 tag</Item>
            <Item>If provided, preferredCurrency is valid ISO-4217 code</Item>
        </PRECONDITIONS>

        <POSTCONDITIONS>
            <Item>Specified fields are updated in persistent storage</Item>
            <Item>updatedAt timestamp is refreshed</Item>
            <Item>UserProfileUpdatedEvent is published with list of changed fields</Item>
            <Item>Returns updated UserProfileDTO</Item>
        </POSTCONDITIONS>

        <INVARIANTS>
            <Item>Email cannot be updated via this method (managed by Keycloak)</Item>
            <Item>Optimistic locking prevents concurrent update conflicts</Item>
            <Item>Update is idempotent if same values are provided</Item>
        </INVARIANTS>

        <ERROR_HANDLING>
            <Item type="BUSINESS" code="ERR-ACCT-PROFILE-NOT-FOUND">Profile does not exist for
                userId</Item>
            <Item type="BUSINESS" code="ERR-ACCT-UNAUTHORIZED">Caller not owner and not ADMIN</Item>
            <Item type="BUSINESS" code="ERR-ACCT-INVALID-PHONE">Phone number format invalid</Item>
            <Item type="BUSINESS" code="ERR-ACCT-INVALID-LANGUAGE">Language code not recognized</Item>
            <Item type="BUSINESS" code="ERR-ACCT-INVALID-CURRENCY">Currency code not recognized</Item>
            <Item type="TECHNICAL" code="ERR-ACCT-CONCURRENT-MODIFICATION">Optimistic lock failure
                (retry)</Item>
        </ERROR_HANDLING>

        <BLOCK_ANCHORS>
            <Item id="BA-ACCT-PROF-01">Fetch user profile from repository</Item>
            <Item id="BA-ACCT-PROF-02">Validate and apply profile updates</Item>
        </BLOCK_ANCHORS>

        <LOGGING>
            <Item>[SVC=account-service][UC=UC-ACCOUNT-MANAGE-PROFILE][BLOCK=BA-ACCT-PROF-01][STATE=FETCHING]
                eventType=PROFILE_UPDATE_START decision=LOOKUP keyValues=userId={uuid}</Item>
            <Item>[SVC=account-service][UC=UC-ACCOUNT-MANAGE-PROFILE][BLOCK=BA-ACCT-PROF-02][STATE=VALIDATING]
                eventType=PROFILE_UPDATE decision=VALIDATE keyValues=userId={uuid},fields={list}</Item>
            <Item>[SVC=account-service][UC=UC-ACCOUNT-MANAGE-PROFILE][BLOCK=BA-ACCT-PROF-02][STATE=UPDATED]
                eventType=PROFILE_UPDATED decision=COMMIT keyValues=userId={uuid},fields={list}</Item>
        </LOGGING>

        <TESTS>
            <Case id="TC-FUNC-UPD-001">Update first name and last name succeeds</Case>
            <Case id="TC-FUNC-UPD-002">Update phone number with valid format succeeds</Case>
            <Case id="TC-FUNC-UPD-003">Update phone number with invalid format returns
                ERR-ACCT-INVALID-PHONE</Case>
            <Case id="TC-FUNC-UPD-004">Update with no fields provided returns validation error</Case>
            <Case id="TC-FUNC-UPD-005">Concurrent updates with version conflict are handled</Case>
        </TESTS>

        <gRPC_MAPPING>
            <Method>AccountService.UpdateProfile</Method>
            <ProtoRequest>UpdateProfileRequest</ProtoRequest>
            <ProtoResponse>UpdateProfileResponse</ProtoResponse>
        </gRPC_MAPPING>
    </FUNCTION_CONTRACT> */

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
            <Item>[SVC=account-service][UC=UC-B2C-CPQ-01][BLOCK=BA-ACCT-CFG-01][STATE=VALIDATING]
                eventType=CONFIG_SAVE_START decision=VALIDATE
                keyValues=userId={uuid},productTemplateId={uuid},name={name}</Item>
            <Item>[SVC=account-service][UC=UC-B2C-CPQ-01][BLOCK=BA-ACCT-CFG-01][STATE=INVALID]
                eventType=CONFIG_SAVE decision=REJECT reason=SCHEMA_VALIDATION_FAILED
                keyValues=userId={uuid},errors={list}</Item>
            <Item>[SVC=account-service][UC=UC-B2C-CPQ-01][BLOCK=BA-ACCT-CFG-02][STATE=PERSISTING]
                eventType=CONFIG_SAVE decision=PERSIST keyValues=userId={uuid},configId={uuid}</Item>
            <Item>[SVC=account-service][UC=UC-B2C-CPQ-01][BLOCK=BA-ACCT-CFG-03][STATE=COMPLETE]
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

/* <FUNCTION_CONTRACT id="FC-account-manageAddresses"
        LAYER="application.service"
        INTENT="CRUD operations for user delivery/installation addresses"
        INPUT="AddressCommand variants (Add, Update, Delete, List)"
        OUTPUT="SavedAddressDTO or List&lt;SavedAddressDTO&gt;"
        SIDE_EFFECTS="Persists address changes, emits Address*Event"
        LINKS="RequirementsAnalysis.xml#UC-ACCOUNT-MANAGE-PROFILE;DevelopmentPlan.xml#DP-SVC-account-service">

        <OPERATIONS>
            <Operation name="AddAddress">
                <Input>AddAddressCommand(userId, address: Address, label: String, setAsDefault:
                    boolean)</Input>
                <Output>SavedAddressDTO</Output>
            </Operation>
            <Operation name="UpdateAddress">
                <Input>UpdateAddressCommand(userId, addressId, address?: Address, label?: String,
                    setAsDefault?: boolean)</Input>
                <Output>SavedAddressDTO</Output>
            </Operation>
            <Operation name="DeleteAddress">
                <Input>DeleteAddressCommand(userId, addressId)</Input>
                <Output>void</Output>
            </Operation>
            <Operation name="ListAddresses">
                <Input>ListAddressesQuery(userId)</Input>
                <Output>List&lt;SavedAddressDTO&gt;</Output>
            </Operation>
        </OPERATIONS>

        <PRECONDITIONS>
            <Item>userId is valid UUID of authenticated caller</Item>
            <Item>For Update/Delete: addressId exists and belongs to userId</Item>
            <Item>Address contains required fields: street, city, postalCode, country</Item>
            <Item>Label is non-blank (max 50 chars, e.g., "Home", "Work", "Site A")</Item>
        </PRECONDITIONS>

        <POSTCONDITIONS>
            <Item>Add: New address is persisted with generated addressId</Item>
            <Item>Add with setAsDefault: Previous default is cleared</Item>
            <Item>Update: Specified fields are updated</Item>
            <Item>Delete: Address is removed from user's list</Item>
            <Item>Appropriate Event (Added, Updated, Deleted) is emitted</Item>
        </POSTCONDITIONS>

        <INVARIANTS>
            <Item>User can have at most one default address</Item>
            <Item>Setting new default clears old default</Item>
            <Item>Deleting default address does NOT auto-promote another (user must set new default)</Item>
            <Item>Duplicate addresses (same street, city, postalCode) for same user are rejected</Item>
        </INVARIANTS>

        <ERROR_HANDLING>
            <Item type="BUSINESS" code="ERR-ACCT-ADDRESS-NOT-FOUND">Address does not exist or does
                not belong to user</Item>
            <Item type="BUSINESS" code="ERR-ACCT-ADDRESS-DUPLICATE">Address with same details
                already exists</Item>
            <Item type="BUSINESS" code="ERR-ACCT-INVALID-ADDRESS">Address missing required fields or
                invalid format</Item>
            <Item type="BUSINESS" code="ERR-ACCT-LABEL-TOO-LONG">Label exceeds 50 characters</Item>
        </ERROR_HANDLING>

        <BLOCK_ANCHORS>
            <Item id="BA-ACCT-ADDR-01">Validate address input</Item>
            <Item id="BA-ACCT-ADDR-02">Check for duplicate address</Item>
            <Item id="BA-ACCT-ADDR-03">Update default address flag</Item>
        </BLOCK_ANCHORS>

        <LOGGING>
            <Item>[SVC=account-service][UC=UC-ACCOUNT-MANAGE-PROFILE][BLOCK=BA-ACCT-ADDR-01][STATE=VALIDATING]
                eventType=ADDRESS_ADD decision=VALIDATE keyValues=userId={uuid},label={label}</Item>
            <Item>[SVC=account-service][UC=UC-ACCOUNT-MANAGE-PROFILE][BLOCK=BA-ACCT-ADDR-02][STATE=CHECKING]
                eventType=ADDRESS_DUPLICATE_CHECK decision=EVALUATE
                keyValues=userId={uuid},city={city}</Item>
            <Item>[SVC=account-service][UC=UC-ACCOUNT-MANAGE-PROFILE][BLOCK=BA-ACCT-ADDR-03][STATE=UPDATING_DEFAULT]
                eventType=ADDRESS_DEFAULT decision=CLEAR_OLD
                keyValues=userId={uuid},oldDefaultId={uuid}</Item>
            <Item>[SVC=account-service][UC=UC-ACCOUNT-MANAGE-PROFILE][BLOCK=BA-ACCT-ADDR-01][STATE=COMPLETE]
                eventType=ADDRESS_ADDED decision=SUCCESS
                keyValues=userId={uuid},addressId={uuid},label={label}</Item>
        </LOGGING>

        <TESTS>
            <Case id="TC-FUNC-ADDR-001">Add address with valid input succeeds</Case>
            <Case id="TC-FUNC-ADDR-002">Add duplicate address returns ERR-ACCT-ADDRESS-DUPLICATE</Case>
            <Case id="TC-FUNC-ADDR-003">Add address with setAsDefault clears previous default</Case>
            <Case id="TC-FUNC-ADDR-004">Update address label succeeds</Case>
            <Case id="TC-FUNC-ADDR-005">Update non-existent address returns
                ERR-ACCT-ADDRESS-NOT-FOUND</Case>
            <Case id="TC-FUNC-ADDR-006">Delete address removes from list</Case>
            <Case id="TC-FUNC-ADDR-007">List addresses returns all user's addresses sorted by label</Case>
        </TESTS>

        <gRPC_MAPPING>
            <Method>AccountService.AddAddress</Method>
            <Method>AccountService.UpdateAddress</Method>
            <Method>AccountService.DeleteAddress</Method>
            <Method>AccountService.ListAddresses</Method>
        </gRPC_MAPPING>
    </FUNCTION_CONTRACT> */

import com.kanokna.account.adapters.config.AccountProperties;
import com.kanokna.account.application.dto.*;
import com.kanokna.account.application.port.in.AddressManagementUseCase;
import com.kanokna.account.application.port.in.ConfigurationManagementUseCase;
import com.kanokna.account.application.port.in.GetProfileUseCase;
import com.kanokna.account.application.port.in.UpdateProfileUseCase;
import com.kanokna.account.application.port.out.CurrentUser;
import com.kanokna.account.application.port.out.CurrentUserProvider;
import com.kanokna.account.application.port.out.EventPublisher;
import com.kanokna.account.application.port.out.SavedAddressRepository;
import com.kanokna.account.application.port.out.SavedConfigurationRepository;
import com.kanokna.account.application.port.out.UserProfileRepository;
import com.kanokna.account.domain.event.*;
import com.kanokna.account.domain.exception.AccountDomainErrors;
import com.kanokna.account.domain.model.*;
import com.kanokna.shared.core.Email;
import com.kanokna.shared.core.Id;
import com.kanokna.shared.core.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service implementing account use cases.
 */
@Service
@Transactional
public class AccountApplicationService implements
    GetProfileUseCase,
    UpdateProfileUseCase,
    AddressManagementUseCase,
    ConfigurationManagementUseCase {

    private static final Logger log = LoggerFactory.getLogger(AccountApplicationService.class);
    private static final String SERVICE = "account-service";
    private static final String USE_CASE_PROFILE = "UC-ACCOUNT-MANAGE-PROFILE";
    private static final String USE_CASE_CPQ = "UC-B2C-CPQ-01";

    private final UserProfileRepository userProfileRepository;
    private final SavedAddressRepository savedAddressRepository;
    private final SavedConfigurationRepository savedConfigurationRepository;
    private final EventPublisher eventPublisher;
    private final CurrentUserProvider currentUserProvider;
    private final AccountProperties accountProperties;
    private final ConfigurationSnapshotValidator configurationSnapshotValidator;

    public AccountApplicationService(
        UserProfileRepository userProfileRepository,
        SavedAddressRepository savedAddressRepository,
        SavedConfigurationRepository savedConfigurationRepository,
        EventPublisher eventPublisher,
        CurrentUserProvider currentUserProvider,
        AccountProperties accountProperties,
        ConfigurationSnapshotValidator configurationSnapshotValidator
    ) {
        this.userProfileRepository = userProfileRepository;
        this.savedAddressRepository = savedAddressRepository;
        this.savedConfigurationRepository = savedConfigurationRepository;
        this.eventPublisher = eventPublisher;
        this.currentUserProvider = currentUserProvider;
        this.accountProperties = accountProperties;
        this.configurationSnapshotValidator = configurationSnapshotValidator;
    }

    @Override
    public UserProfileDto getProfile(GetProfileQuery query) {
        Id requestedUserId = Id.of(query.userId().toString());
        CurrentUser currentUser = requireCurrentUser();
        authorizeAccess(currentUser, requestedUserId);

        // BA-ACCT-PROF-01: Fetch user profile from repository
        log.info(logLine(USE_CASE_PROFILE, "BA-ACCT-PROF-01", "FETCHING",
            "PROFILE_GET", "LOOKUP", "userId=" + requestedUserId.value()));

        Optional<UserProfile> existing = userProfileRepository.findById(requestedUserId);
        UserProfile profile;
        if (existing.isPresent()) {
            profile = existing.get();
        } else {
            if (!accountProperties.isAutoCreateProfile()) {
                throw AccountDomainErrors.profileNotFound(requestedUserId.value());
            }
            // BA-ACCT-PROF-03: Sync profile from Keycloak on first login
            String maskedEmail = maskEmail(currentUser.email());
            log.info(logLine(USE_CASE_PROFILE, "BA-ACCT-PROF-03", "SYNCING",
                "PROFILE_SYNC", "CREATE_FROM_KEYCLOAK",
                "userId=" + requestedUserId.value() + ",email=" + maskedEmail));

            profile = createProfileFromCurrentUser(requestedUserId, currentUser);
            profile = userProfileRepository.save(profile);
            UserProfileCreatedEvent event = UserProfileCreatedEvent.create(
                profile.userId().value(),
                profile.email().asString(),
                profile.version()
            );
            eventPublisher.publish(event.shortType(), event);
        }

        int addressCount = profile.addresses() != null ? profile.addresses().size() : 0;
        log.info(logLine(USE_CASE_PROFILE, "BA-ACCT-PROF-01", "COMPLETE",
            "PROFILE_GET", "SUCCESS", "userId=" + requestedUserId.value() + ",addressCount=" + addressCount));

        return toDto(profile, profile.addresses());
    }

    @Override
    public UserProfileDto updateProfile(UpdateProfileCommand command) {
        Id requestedUserId = Id.of(command.userId().toString());
        CurrentUser currentUser = requireCurrentUser();
        authorizeAccess(currentUser, requestedUserId);

        if (command.firstName() == null && command.lastName() == null
            && command.phoneNumber() == null && command.preferredLanguage() == null
            && command.preferredCurrency() == null) {
            throw new IllegalArgumentException("At least one field must be provided");
        }

        // BA-ACCT-PROF-01: Fetch user profile from repository
        log.info(logLine(USE_CASE_PROFILE, "BA-ACCT-PROF-01", "FETCHING",
            "PROFILE_UPDATE_START", "LOOKUP", "userId=" + requestedUserId.value()));

        UserProfile profile = userProfileRepository.findById(requestedUserId)
            .orElseThrow(() -> AccountDomainErrors.profileNotFound(requestedUserId.value()));

        PersonName updatedName = buildUpdatedName(profile, command);
        PhoneNumber updatedPhone = parsePhone(command.phoneNumber());
        LocalePreference updatedLanguage = parseLanguage(command.preferredLanguage());
        CurrencyPreference updatedCurrency = parseCurrency(command.preferredCurrency());

        String fields = renderUpdatedFields(command);
        // BA-ACCT-PROF-02: Validate and apply profile updates
        log.info(logLine(USE_CASE_PROFILE, "BA-ACCT-PROF-02", "VALIDATING",
            "PROFILE_UPDATE", "VALIDATE", "userId=" + requestedUserId.value() + ",fields=" + fields));

        Instant now = Instant.now();
        List<String> changedFields = profile.applyProfileUpdates(
            updatedName,
            updatedPhone,
            updatedLanguage,
            updatedCurrency,
            null,
            now
        );

        if (!changedFields.isEmpty()) {
            try {
                profile = userProfileRepository.save(profile);
            } catch (OptimisticLockingFailureException ex) {
                throw AccountDomainErrors.concurrentModification(requestedUserId.value());
            }

            UserProfileUpdatedEvent event = UserProfileUpdatedEvent.create(
                profile.userId().value(),
                profile.version(),
                changedFields
            );
            eventPublisher.publish(event.shortType(), event);
        }

        log.info(logLine(USE_CASE_PROFILE, "BA-ACCT-PROF-02", "UPDATED",
            "PROFILE_UPDATED", "COMMIT", "userId=" + requestedUserId.value() + ",fields=" + fields));

        return toDto(profile, profile.addresses());
    }

    @Override
    public SavedConfigurationDto saveConfiguration(SaveConfigurationCommand command) {
        Id userId = Id.of(command.userId().toString());
        CurrentUser currentUser = requireCurrentUser();
        authorizeAccess(currentUser, userId);

        String name = command.name();
        if (name == null || name.isBlank() || name.strip().length() > 255) {
            throw AccountDomainErrors.invalidConfigName(name);
        }

        UUID productTemplateId = command.productTemplateId();
        if (productTemplateId == null) {
            throw AccountDomainErrors.invalidConfigSnapshot("productTemplateId is required");
        }

        // BA-ACCT-CFG-01: Validate configuration snapshot structure
        log.info(logLine(USE_CASE_CPQ, "BA-ACCT-CFG-01", "VALIDATING",
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
        log.info(logLine(USE_CASE_CPQ, "BA-ACCT-CFG-02", "PERSISTING",
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

        log.info(logLine(USE_CASE_CPQ, "BA-ACCT-CFG-03", "COMPLETE",
            "CONFIG_SAVED", "SUCCESS",
            "userId=" + userId.value() + ",configId=" + saved.configurationId().value()
                + ",productTemplateId=" + saved.productTemplateId().value()));

        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavedConfigurationDto> listConfigurations(ListConfigurationsQuery query) {
        Id userId = Id.of(query.userId().toString());
        CurrentUser currentUser = requireCurrentUser();
        authorizeAccess(currentUser, userId);
        return savedConfigurationRepository.findByUserId(userId)
            .stream()
            .map(this::toDto)
            .toList();
    }

    @Override
    public void deleteConfiguration(DeleteConfigurationCommand command) {
        Id userId = Id.of(command.userId().toString());
        CurrentUser currentUser = requireCurrentUser();
        authorizeAccess(currentUser, userId);

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

    @Override
    public SavedAddressDto addAddress(AddAddressCommand command) {
        Id userId = Id.of(command.userId().toString());
        CurrentUser currentUser = requireCurrentUser();
        authorizeAccess(currentUser, userId);

        // BA-ACCT-ADDR-01: Validate address input
        log.info(logLine(USE_CASE_PROFILE, "BA-ACCT-ADDR-01", "VALIDATING",
            "ADDRESS_ADD", "VALIDATE", "userId=" + userId.value() + ",label=" + command.label()));

        UserProfile profile = loadProfileWithAddresses(userId);
        AddressDto addressDto = command.address();

        // BA-ACCT-ADDR-02: Check for duplicate address
        String city = addressDto != null ? addressDto.city() : "";
        log.info(logLine(USE_CASE_PROFILE, "BA-ACCT-ADDR-02", "CHECKING",
            "ADDRESS_DUPLICATE_CHECK", "EVALUATE", "userId=" + userId.value() + ",city=" + city));

        Optional<SavedAddress> oldDefault = profile.addresses().stream().filter(SavedAddress::isDefault).findFirst();
        if (command.setAsDefault() && oldDefault.isPresent()) {
            // BA-ACCT-ADDR-03: Update default address flag
            log.info(logLine(USE_CASE_PROFILE, "BA-ACCT-ADDR-03", "UPDATING_DEFAULT",
                "ADDRESS_DEFAULT", "CLEAR_OLD",
                "userId=" + userId.value() + ",oldDefaultId=" + oldDefault.get().addressId().value()));
        }

        Instant now = Instant.now();
        SavedAddress savedAddress = profile.addAddress(addressDto.toValueObject(), command.label(), command.setAsDefault(), now);
        List<SavedAddress> savedAddresses = savedAddressRepository.saveAll(userId, profile.addresses());
        profile.replaceAddresses(savedAddresses);

        SavedAddress persisted = savedAddresses.stream()
            .filter(address -> Objects.equals(address.addressId(), savedAddress.addressId()))
            .findFirst()
            .orElse(savedAddress);

        AddressAddedEvent event = AddressAddedEvent.create(
            userId.value(),
            persisted.addressId().value(),
            persisted.isDefault(),
            profile.version()
        );
        eventPublisher.publish(event.shortType(), event);

        log.info(logLine(USE_CASE_PROFILE, "BA-ACCT-ADDR-01", "COMPLETE",
            "ADDRESS_ADDED", "SUCCESS",
            "userId=" + userId.value() + ",addressId=" + persisted.addressId().value() + ",label=" + persisted.label()));

        return toDto(persisted);
    }

    @Override
    public SavedAddressDto updateAddress(UpdateAddressCommand command) {
        Id userId = Id.of(command.userId().toString());
        CurrentUser currentUser = requireCurrentUser();
        authorizeAccess(currentUser, userId);

        // BA-ACCT-ADDR-01: Validate address input
        log.info(logLine(USE_CASE_PROFILE, "BA-ACCT-ADDR-01", "VALIDATING",
            "ADDRESS_UPDATE", "VALIDATE", "userId=" + userId.value()));

        UserProfile profile = loadProfileWithAddresses(userId);
        Optional<SavedAddress> oldDefault = profile.addresses().stream().filter(SavedAddress::isDefault).findFirst();
        if (Boolean.TRUE.equals(command.setAsDefault()) && oldDefault.isPresent()) {
            // BA-ACCT-ADDR-03: Update default address flag
            log.info(logLine(USE_CASE_PROFILE, "BA-ACCT-ADDR-03", "UPDATING_DEFAULT",
                "ADDRESS_DEFAULT", "CLEAR_OLD",
                "userId=" + userId.value() + ",oldDefaultId=" + oldDefault.get().addressId().value()));
        }

        Instant now = Instant.now();
        SavedAddress updated = profile.updateAddress(
            Id.of(command.addressId().toString()),
            command.address() != null ? command.address().toValueObject() : null,
            command.label(),
            command.setAsDefault(),
            now
        );

        List<SavedAddress> savedAddresses = savedAddressRepository.saveAll(userId, profile.addresses());
        profile.replaceAddresses(savedAddresses);

        SavedAddress persisted = savedAddresses.stream()
            .filter(address -> Objects.equals(address.addressId(), updated.addressId()))
            .findFirst()
            .orElse(updated);

        List<String> changedFields = buildAddressChangedFields(command);
        AddressUpdatedEvent event = AddressUpdatedEvent.create(
            userId.value(),
            persisted.addressId().value(),
            profile.version(),
            changedFields
        );
        eventPublisher.publish(event.shortType(), event);

        return toDto(persisted);
    }

    @Override
    public void deleteAddress(DeleteAddressCommand command) {
        Id userId = Id.of(command.userId().toString());
        CurrentUser currentUser = requireCurrentUser();
        authorizeAccess(currentUser, userId);

        UserProfile profile = loadProfileWithAddresses(userId);
        profile.deleteAddress(Id.of(command.addressId().toString()));
        savedAddressRepository.deleteByUserIdAndId(userId, Id.of(command.addressId().toString()));

        AddressDeletedEvent event = AddressDeletedEvent.create(
            userId.value(),
            command.addressId().toString(),
            profile.version()
        );
        eventPublisher.publish(event.shortType(), event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SavedAddressDto> listAddresses(ListAddressesQuery query) {
        Id userId = Id.of(query.userId().toString());
        CurrentUser currentUser = requireCurrentUser();
        authorizeAccess(currentUser, userId);

        UserProfile profile = loadProfileWithAddresses(userId);
        return profile.listAddressesSorted().stream()
            .map(this::toDto)
            .toList();
    }

    private CurrentUser requireCurrentUser() {
        CurrentUser currentUser = currentUserProvider.currentUser();
        if (currentUser == null || currentUser.userId() == null || currentUser.userId().isBlank()) {
            throw AccountDomainErrors.unauthorized("Unauthenticated request");
        }
        return currentUser;
    }

    private void authorizeAccess(CurrentUser currentUser, Id requestedUserId) {
        if (currentUser.isAdmin()) {
            return;
        }
        if (!Objects.equals(currentUser.userId(), requestedUserId.value())) {
            throw AccountDomainErrors.unauthorized("User not authorized for requested resource");
        }
    }

    private UserProfile loadProfileWithAddresses(Id userId) {
        UserProfile profile = userProfileRepository.findById(userId)
            .orElseThrow(() -> AccountDomainErrors.profileNotFound(userId.value()));
        List<SavedAddress> addresses = savedAddressRepository.findByUserId(userId);
        profile.replaceAddresses(addresses);
        return profile;
    }

    private PersonName buildUpdatedName(UserProfile profile, UpdateProfileCommand command) {
        if (command.firstName() == null && command.lastName() == null) {
            return null;
        }
        String firstName = command.firstName();
        String lastName = command.lastName();
        if (profile.personName() != null) {
            if (firstName == null) {
                firstName = profile.personName().firstName();
            }
            if (lastName == null) {
                lastName = profile.personName().lastName();
            }
        }
        return new PersonName(firstName, lastName);
    }

    private PhoneNumber parsePhone(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        try {
            return PhoneNumber.of(phoneNumber);
        } catch (IllegalArgumentException ex) {
            throw AccountDomainErrors.invalidPhone(phoneNumber);
        }
    }

    private LocalePreference parseLanguage(String language) {
        if (language == null) {
            return null;
        }
        String tag = language.strip();
        if (tag.isEmpty()) {
            throw AccountDomainErrors.invalidLanguage(language);
        }
        Locale locale = Locale.forLanguageTag(tag);
        if (locale.getLanguage().isEmpty() || "und".equalsIgnoreCase(locale.getLanguage())) {
            throw AccountDomainErrors.invalidLanguage(language);
        }
        return new LocalePreference(tag);
    }

    private CurrencyPreference parseCurrency(String currency) {
        if (currency == null) {
            return null;
        }
        String normalized = currency.strip().toUpperCase(Locale.ROOT);
        try {
            Currency.getInstance(normalized);
        } catch (IllegalArgumentException ex) {
            throw AccountDomainErrors.invalidCurrency(currency);
        }
        return new CurrencyPreference(normalized);
    }

    private UserProfile createProfileFromCurrentUser(Id userId, CurrentUser currentUser) {
        String email = currentUser.email();
        if (email == null || email.isBlank()) {
            throw AccountDomainErrors.keycloakUnavailable("Email claim missing", null);
        }
        PersonName name = new PersonName(currentUser.firstName(), currentUser.lastName());
        PhoneNumber phone = null;
        if (currentUser.phoneNumber() != null && !currentUser.phoneNumber().isBlank()) {
            try {
                phone = PhoneNumber.of(currentUser.phoneNumber());
            } catch (IllegalArgumentException ex) {
                phone = null;
            }
        }

        LocalePreference defaultLanguage = parseLanguage(accountProperties.getDefaults().getLanguage());
        CurrencyPreference defaultCurrency = parseCurrency(accountProperties.getDefaults().getCurrency());
        NotificationPreferences notificationPreferences = NotificationPreferences.of(
            accountProperties.getDefaults().isNotificationEmail(),
            accountProperties.getDefaults().isNotificationSms(),
            accountProperties.getDefaults().isNotificationPush()
        );

        Instant now = Instant.now();
        return UserProfile.create(
            userId,
            Email.of(email),
            name,
            phone,
            defaultLanguage,
            defaultCurrency,
            notificationPreferences,
            null,
            now
        );
    }

    private UserProfileDto toDto(UserProfile profile, List<SavedAddress> addresses) {
        return new UserProfileDto(
            profile.userId().value(),
            profile.email().asString(),
            profile.personName() != null ? profile.personName().firstName() : null,
            profile.personName() != null ? profile.personName().lastName() : null,
            profile.phoneNumber() != null ? profile.phoneNumber().value() : null,
            profile.preferredLanguage() != null ? profile.preferredLanguage().languageTag() : null,
            profile.preferredCurrency() != null ? profile.preferredCurrency().currencyCode() : null,
            toDto(profile.notificationPreferences()),
            addresses != null ? addresses.stream().map(this::toDto).toList() : List.of(),
            profile.createdAt(),
            profile.updatedAt()
        );
    }

    private NotificationPreferencesDto toDto(NotificationPreferences preferences) {
        if (preferences == null) {
            return null;
        }
        return new NotificationPreferencesDto(
            preferences.email(),
            preferences.sms(),
            preferences.push()
        );
    }

    private SavedAddressDto toDto(SavedAddress address) {
        return new SavedAddressDto(
            address.addressId().value(),
            AddressDto.fromValueObject(address.address()),
            address.label(),
            address.isDefault(),
            address.createdAt(),
            address.updatedAt()
        );
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

    private List<String> buildAddressChangedFields(UpdateAddressCommand command) {
        List<String> fields = new ArrayList<>();
        if (command.address() != null) {
            fields.add("address");
        }
        if (command.label() != null) {
            fields.add("label");
        }
        if (command.setAsDefault() != null) {
            fields.add("isDefault");
        }
        return fields;
    }

    private String renderUpdatedFields(UpdateProfileCommand command) {
        List<String> fields = new ArrayList<>();
        if (command.firstName() != null) {
            fields.add("firstName");
        }
        if (command.lastName() != null) {
            fields.add("lastName");
        }
        if (command.phoneNumber() != null) {
            fields.add("phoneNumber");
        }
        if (command.preferredLanguage() != null) {
            fields.add("preferredLanguage");
        }
        if (command.preferredCurrency() != null) {
            fields.add("preferredCurrency");
        }
        return String.join("|", fields);
    }

    private String maskEmail(String email) {
        if (email == null || email.isBlank()) {
            return "unknown";
        }
        Email parsed = Email.of(email);
        return parsed.masked();
    }

    private String logLine(String useCase, String block, String state,
                           String eventType, String decision, String keyValues) {
        StringBuilder builder = new StringBuilder();
        builder.append("[SVC=").append(SERVICE).append("]")
            .append("[UC=").append(useCase).append("]")
            .append("[BLOCK=").append(block).append("]")
            .append("[STATE=").append(state).append("]")
            .append(" eventType=").append(eventType)
            .append(" decision=").append(decision);
        if (keyValues != null && !keyValues.isBlank()) {
            builder.append(" keyValues=").append(keyValues);
        }
        return builder.toString();
    }
}
