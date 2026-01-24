package com.kanokna.account.application.service;

import com.kanokna.account.application.dto.AddAddressCommand;
import com.kanokna.account.application.dto.AddressDto;
import com.kanokna.account.application.dto.DeleteAddressCommand;
import com.kanokna.account.application.dto.ListAddressesQuery;
import com.kanokna.account.application.dto.SavedAddressDto;
import com.kanokna.account.application.dto.UpdateAddressCommand;
import com.kanokna.account.application.port.out.CurrentUser;
import com.kanokna.account.application.port.out.CurrentUserProvider;
import com.kanokna.account.application.port.out.EventPublisher;
import com.kanokna.account.application.port.out.SavedAddressRepository;
import com.kanokna.account.application.port.out.UserProfileRepository;
import com.kanokna.account.domain.event.AddressAddedEvent;
import com.kanokna.account.domain.event.AddressDeletedEvent;
import com.kanokna.account.domain.event.AddressUpdatedEvent;
import com.kanokna.account.domain.exception.AccountDomainErrors;
import com.kanokna.account.domain.model.SavedAddress;
import com.kanokna.account.domain.model.UserProfile;
import com.kanokna.shared.core.Id;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

/**
 * MODULE_CONTRACT id="MC-account-address-service"
 * LAYER="application.service"
 * INTENT="Specialized service for address management operations"
 * LINKS="Technology.xml#DEC-ACCOUNT-DECOMPOSITION;RequirementsAnalysis.xml#UC-ACCOUNT-MANAGE-PROFILE"
 */
@Service
@Transactional
public class AddressService {
    private static final Logger log = LoggerFactory.getLogger(AddressService.class);
    private static final String USE_CASE_PROFILE = "UC-ACCOUNT-MANAGE-PROFILE";

    private final SavedAddressRepository savedAddressRepository;
    private final UserProfileRepository userProfileRepository;
    private final EventPublisher eventPublisher;
    private final CurrentUserProvider currentUserProvider;

    public AddressService(
        SavedAddressRepository savedAddressRepository,
        UserProfileRepository userProfileRepository,
        EventPublisher eventPublisher,
        CurrentUserProvider currentUserProvider
    ) {
        this.savedAddressRepository = savedAddressRepository;
        this.userProfileRepository = userProfileRepository;
        this.eventPublisher = eventPublisher;
        this.currentUserProvider = currentUserProvider;
    }

    public SavedAddressDto addAddress(AddAddressCommand command) {
        Id userId = Id.of(command.userId().toString());
        CurrentUser currentUser = AccountServiceUtils.requireCurrentUser(currentUserProvider);
        AccountServiceUtils.authorizeAccess(currentUser, userId);

        // BA-ACCT-ADDR-01: Validate address input
        log.info(AccountServiceUtils.logLine(USE_CASE_PROFILE, "BA-ACCT-ADDR-01", "VALIDATING",
            "ADDRESS_ADD", "VALIDATE", "userId=" + userId.value() + ",label=" + command.label()));

        UserProfile profile = loadProfileWithAddresses(userId);
        AddressDto addressDto = command.address();

        // BA-ACCT-ADDR-02: Check for duplicate address
        String city = addressDto != null ? addressDto.city() : "";
        log.info(AccountServiceUtils.logLine(USE_CASE_PROFILE, "BA-ACCT-ADDR-02", "CHECKING",
            "ADDRESS_DUPLICATE_CHECK", "EVALUATE", "userId=" + userId.value() + ",city=" + city));

        Optional<SavedAddress> oldDefault = profile.addresses().stream().filter(SavedAddress::isDefault).findFirst();
        if (command.setAsDefault() && oldDefault.isPresent()) {
            // BA-ACCT-ADDR-03: Update default address flag
            log.info(AccountServiceUtils.logLine(USE_CASE_PROFILE, "BA-ACCT-ADDR-03", "UPDATING_DEFAULT",
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

        log.info(AccountServiceUtils.logLine(USE_CASE_PROFILE, "BA-ACCT-ADDR-01", "COMPLETE",
            "ADDRESS_ADDED", "SUCCESS",
            "userId=" + userId.value() + ",addressId=" + persisted.addressId().value() + ",label=" + persisted.label()));

        return AccountServiceUtils.toDto(persisted);
    }

    public SavedAddressDto updateAddress(UpdateAddressCommand command) {
        Id userId = Id.of(command.userId().toString());
        CurrentUser currentUser = AccountServiceUtils.requireCurrentUser(currentUserProvider);
        AccountServiceUtils.authorizeAccess(currentUser, userId);

        // BA-ACCT-ADDR-01: Validate address input
        log.info(AccountServiceUtils.logLine(USE_CASE_PROFILE, "BA-ACCT-ADDR-01", "VALIDATING",
            "ADDRESS_UPDATE", "VALIDATE", "userId=" + userId.value()));

        UserProfile profile = loadProfileWithAddresses(userId);
        Optional<SavedAddress> oldDefault = profile.addresses().stream().filter(SavedAddress::isDefault).findFirst();
        if (Boolean.TRUE.equals(command.setAsDefault()) && oldDefault.isPresent()) {
            // BA-ACCT-ADDR-03: Update default address flag
            log.info(AccountServiceUtils.logLine(USE_CASE_PROFILE, "BA-ACCT-ADDR-03", "UPDATING_DEFAULT",
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

        return AccountServiceUtils.toDto(persisted);
    }

    public void deleteAddress(DeleteAddressCommand command) {
        Id userId = Id.of(command.userId().toString());
        CurrentUser currentUser = AccountServiceUtils.requireCurrentUser(currentUserProvider);
        AccountServiceUtils.authorizeAccess(currentUser, userId);

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

    @Transactional(readOnly = true)
    public List<SavedAddressDto> listAddresses(ListAddressesQuery query) {
        Id userId = Id.of(query.userId().toString());
        CurrentUser currentUser = AccountServiceUtils.requireCurrentUser(currentUserProvider);
        AccountServiceUtils.authorizeAccess(currentUser, userId);

        UserProfile profile = loadProfileWithAddresses(userId);
        return profile.listAddressesSorted().stream()
            .map(AccountServiceUtils::toDto)
            .toList();
    }

    private UserProfile loadProfileWithAddresses(Id userId) {
        UserProfile profile = userProfileRepository.findById(userId)
            .orElseThrow(() -> AccountDomainErrors.profileNotFound(userId.value()));
        List<SavedAddress> addresses = savedAddressRepository.findByUserId(userId);
        profile.replaceAddresses(addresses);
        return profile;
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
}
