package com.kanokna.account.domain.model;

/* <MODULE_CONTRACT id="MC-account-service-domain"
    ROLE="DomainModule"
    SERVICE="account-service"
    LAYER="domain"
    BOUNDED_CONTEXT="account"
    SPECIFICATION="UC-ACCOUNT-REGISTER,UC-ACCOUNT-LOGIN,UC-ACCOUNT-MANAGE-PROFILE">

    <PURPOSE>
        Domain module for the account-service managing user profiles, saved configurations,
        delivery addresses, and user preferences. Integrates with Keycloak for identity
        (authentication/authorization) while owning profile enrichment data, addresses,
        and saved product configurations for CPQ workflow.
    </PURPOSE>

    <RESPONSIBILITIES>
        <Item>Store and manage user profiles synced from Keycloak on first login</Item>
        <Item>Manage multiple delivery/installation addresses per user</Item>
        <Item>Store user preferences (language, currency, notification settings)</Item>
        <Item>Manage saved product configurations (drafts) for later retrieval</Item>
        <Item>B2B: manage partner organization profiles and sub-user associations (MVP deferred)</Item>
        <Item>Emit domain events for profile changes and configuration saves</Item>
    </RESPONSIBILITIES>

    <AGGREGATES>
        <Aggregate name="UserProfile" root="true">
            <Description>User profile aggregate with addresses and preferences</Description>
            <Entities>
                <Entity name="SavedAddress">Delivery/installation address with label and default
                    flag</Entity>
            </Entities>
            <ValueObjects>
                <ValueObject name="UserId">Keycloak sub claim identifier (UUID)</ValueObject>
                <ValueObject name="Email">User email (from shared-kernel)</ValueObject>
                <ValueObject name="PhoneNumber">Contact phone (from shared-kernel)</ValueObject>
                <ValueObject name="PersonName">First and last name</ValueObject>
                <ValueObject name="Locale">Preferred language (BCP-47)</ValueObject>
                <ValueObject name="Currency">Preferred currency (ISO-4217)</ValueObject>
                <ValueObject name="NotificationPreferences">Email/SMS/Push preferences</ValueObject>
            </ValueObjects>
        </Aggregate>

        <Aggregate name="SavedConfiguration">
            <Description>User-saved product configuration for later retrieval</Description>
            <ValueObjects>
                <ValueObject name="ConfigurationId">Unique configuration identifier</ValueObject>
                <ValueObject name="ProductTemplateId">Reference to product template</ValueObject>
                <ValueObject name="ConfigurationSnapshot">JSONB snapshot of configuration state</ValueObject>
                <ValueObject name="QuoteSnapshot">Optional pricing quote at save time</ValueObject>
            </ValueObjects>
        </Aggregate>

        <Aggregate name="PartnerOrganization" scope="B2B-DEFERRED">
            <Description>B2B organization profile (deferred to Phase 2)</Description>
            <Note>MVP scope: minimal implementation, full B2B in later wave</Note>
        </Aggregate>
    </AGGREGATES>

    <DOMAIN_EVENTS>
        <Event name="UserProfileCreatedEvent">
            <Description>Emitted when user profile is created on first login</Description>
            <Payload>userId, email, createdAt</Payload>
            <Consumers>notification-service (welcome email), reporting-service</Consumers>
        </Event>
        <Event name="UserProfileUpdatedEvent">
            <Description>Emitted when profile details are updated</Description>
            <Payload>userId, changedFields, updatedAt</Payload>
            <Consumers>reporting-service</Consumers>
        </Event>
        <Event name="AddressAddedEvent">
            <Description>Emitted when a new address is added</Description>
            <Payload>userId, addressId, isDefault</Payload>
        </Event>
        <Event name="AddressUpdatedEvent">
            <Description>Emitted when an address is updated</Description>
            <Payload>userId, addressId, changedFields</Payload>
        </Event>
        <Event name="AddressDeletedEvent">
            <Description>Emitted when an address is deleted</Description>
            <Payload>userId, addressId</Payload>
        </Event>
        <Event name="ConfigurationSavedEvent">
            <Description>Emitted when user saves a product configuration</Description>
            <Payload>userId, configurationId, productTemplateId, savedAt</Payload>
            <Consumers>reporting-service (conversion funnel)</Consumers>
        </Event>
        <Event name="ConfigurationDeletedEvent">
            <Description>Emitted when saved configuration is deleted</Description>
            <Payload>userId, configurationId</Payload>
        </Event>
    </DOMAIN_EVENTS>

    <INVARIANTS>
        <Item>UserId must be a valid UUID from Keycloak sub claim</Item>
        <Item>Email must be valid format (RFC 5322)</Item>
        <Item>User can have at most one default address per address type (delivery, installation)</Item>
        <Item>Setting a new default address clears previous default for that type</Item>
        <Item>Saved configuration must reference a valid product template ID</Item>
        <Item>ConfigurationSnapshot must be valid JSON matching expected schema</Item>
        <Item>User cannot have duplicate addresses (same street, city, postal code)</Item>
    </INVARIANTS>

    <CROSS_CUTTING>
        <SECURITY>
            <Item>Only profile owner or ADMIN role can update profile/address</Item>
            <Item>Keycloak roles mapped to authorities for access control</Item>
        </SECURITY>
        <RELIABILITY>
            <Item>Profile operations are idempotent (retry-safe)</Item>
            <Item>Optimistic locking via version field for profile updates</Item>
        </RELIABILITY>
        <OBSERVABILITY>
            <Item>Log all profile CRUD operations with userId and action</Item>
            <Item>Metrics: profiles_created_total, configurations_saved_total, addresses_total</Item>
            <Item>All logs include traceId, spanId, correlationId from MDC</Item>
        </OBSERVABILITY>
    </CROSS_CUTTING>

    <LOGGING>
        <FORMAT>[SVC=account-service][UC={usecase}][BLOCK={blockId}][STATE={state}] eventType={type}
            decision={decision} keyValues={...}</FORMAT>
        <EXAMPLES>
            <Item>[SVC=account-service][UC=UC-ACCOUNT-MANAGE-PROFILE][BLOCK=BA-ACCT-PROF-01][STATE=FETCHING]
                eventType=PROFILE_GET decision=LOOKUP keyValues=userId={uuid}</Item>
            <Item>[SVC=account-service][UC=UC-ACCOUNT-MANAGE-PROFILE][BLOCK=BA-ACCT-PROF-02][STATE=UPDATING]
                eventType=PROFILE_UPDATE decision=APPLY keyValues=userId={uuid},fields=name,phone</Item>
            <Item>[SVC=account-service][UC=UC-ACCOUNT-MANAGE-PROFILE][BLOCK=BA-ACCT-ADDR-01][STATE=VALIDATING]
                eventType=ADDRESS_ADD decision=EVALUATE keyValues=userId={uuid},label=Home</Item>
            <Item>[SVC=account-service][UC=UC-B2C-CPQ-01][BLOCK=BA-ACCT-CFG-01][[STATE=SAVING]
                eventType=CONFIG_SAVE decision=PERSIST
                keyValues=userId={uuid},configId={uuid},productTemplateId={uuid}}</Item>
        </EXAMPLES>
    </LOGGING>

    <BLOCK_ANCHORS>
        <!-- Profile Anchors -->
        <Anchor id="BA-ACCT-PROF-01" purpose="Fetch user profile from repository" />
        <Anchor id="BA-ACCT-PROF-02" purpose="Validate and apply profile updates" />
        <Anchor id="BA-ACCT-PROF-03" purpose="Sync profile from Keycloak on first login" />
        <!-- Address Anchors -->
        <Anchor id="BA-ACCT-ADDR-01" purpose="Validate address input" />
        <Anchor id="BA-ACCT-ADDR-02" purpose="Check for duplicate address" />
        <Anchor id="BA-ACCT-ADDR-03" purpose="Update default address flag" />
        <!-- Configuration Anchors -->
        <Anchor id="BA-ACCT-CFG-01" purpose="Validate configuration snapshot structure" />
        <Anchor id="BA-ACCT-CFG-02" purpose="Persist saved configuration" />
        <Anchor id="BA-ACCT-CFG-03" purpose="Emit ConfigurationSavedEvent" />
    </BLOCK_ANCHORS>

    <TESTS>
        <!-- Profile Tests -->
        <Case id="TC-ACCT-001">Get profile for existing user returns profile data</Case>
        <Case id="TC-ACCT-002">Get profile for non-existent user returns NOT_FOUND</Case>
        <Case id="TC-ACCT-003">Update profile changes name and phone number</Case>
        <Case id="TC-ACCT-004">Update profile with invalid email returns validation error</Case>
        <Case id="TC-ACCT-005">First login creates profile from Keycloak user info</Case>
        <!-- Address Tests -->
        <Case id="TC-ACCT-006">Add address succeeds with valid input</Case>
        <Case id="TC-ACCT-007">Add duplicate address returns ALREADY_EXISTS error</Case>
        <Case id="TC-ACCT-008">Set address as default clears previous default</Case>
        <Case id="TC-ACCT-009">Delete address removes from user's address list</Case>
        <Case id="TC-ACCT-010">Cannot delete the only address if order in progress (future)</Case>
        <!-- Configuration Tests -->
        <Case id="TC-ACCT-011">Save configuration persists snapshot and returns ID</Case>
        <Case id="TC-ACCT-012">Save configuration with invalid JSON returns validation error</Case>
        <Case id="TC-ACCT-013">List saved configurations returns all user's configs</Case>
        <Case id="TC-ACCT-014">Delete saved configuration removes from list</Case>
        <Case id="TC-ACCT-015">User cannot access another user's configurations (403)</Case>
    </TESTS>

    <LINKS>
        <Link ref="RequirementsAnalysis.xml#UC-ACCOUNT-REGISTER" />
        <Link ref="RequirementsAnalysis.xml#UC-ACCOUNT-LOGIN" />
        <Link ref="RequirementsAnalysis.xml#UC-ACCOUNT-MANAGE-PROFILE" />
        <Link ref="RequirementsAnalysis.xml#ACT-IDENTITY-PROVIDER" />
        <Link ref="DevelopmentPlan.xml#DP-SVC-account-service" />
        <Link ref="DevelopmentPlan.xml#Flow-B2C-CPQ" />
        <Link ref="Technology.xml#TECH-postgresql" />
        <Link ref="Technology.xml#TECH-keycloak" />
        <Link ref="api-contracts/proto/kanokna/account/v1/account_service.proto" />
    </LINKS>

    <DECISIONS>
        <Decision id="DEC-ACCT-KEYCLOAK-SYNC" status="APPROVED">
            <Title>Keycloak Profile Sync Strategy</Title>
            <Selected>On-Login Sync</Selected>
            <Alternatives>Webhook-based sync, Admin API polling</Alternatives>
            <Rationale>
                On-login sync is simplest: when user authenticates, extract claims from JWT
                (sub, email, name) and upsert profile if not exists or email changed.
                Avoids webhook complexity and reduces Keycloak coupling.
            </Rationale>
        </Decision>
        <Decision id="DEC-ACCT-CONFIG-SNAPSHOT" status="APPROVED">
            <Title>Saved Configuration Snapshot Format</Title>
            <Selected>JSONB with schema validation</Selected>
            <Rationale>
                ConfigurationSnapshot stored as JSONB in PostgreSQL allows flexible schema
                evolution and efficient querying. Application validates against expected
                schema before persisting. QuoteSnapshot is nullable for price-free saves.
            </Rationale>
            <Schema>
                {
                "productTemplateId": "uuid",
                "dimensions": {"width": number, "height": number, "unit": "mm|cm"},
                "selectedOptions": [{"optionGroupId": "uuid", "optionId": "uuid", "value": any}],
                "accessories": [{"accessoryId": "uuid", "quantity": number}],
                "savedAt": "ISO8601"
                }
            </Schema>
        </Decision>
        <Decision id="DEC-ACCT-B2B-SCOPE" status="APPROVED">
            <Title>B2B PartnerOrganization MVP Scope</Title>
            <Selected>Minimal implementation - UserProfile with partnerOrganizationId reference only</Selected>
            <Rationale>
                Full B2B organization management (sub-users, RBAC, partner tiers) deferred to Phase
                2.
                MVP includes optional partnerOrganizationId on UserProfile for basic B2B user
                identification.
            </Rationale>
        </Decision>
    </DECISIONS>

    <ASSUMPTIONS>
        <Item id="A-ACCT-01">Keycloak handles user registration UI; account-service only syncs
            profile data on first login</Item>
        <Item id="A-ACCT-02">Email from Keycloak JWT is always verified (Keycloak enforces email
            verification)</Item>
        <Item id="A-ACCT-03">SavedConfiguration does not validate productTemplateId against
            catalog-service (validation at retrieval time)</Item>
        <Item id="A-ACCT-04">Address validation is format-only; no geocoding or postal code
            validation in MVP</Item>
    </ASSUMPTIONS>

</MODULE_CONTRACT> */

import com.kanokna.account.domain.exception.AccountDomainErrors;
import com.kanokna.shared.core.Address;
import com.kanokna.shared.core.Email;
import com.kanokna.shared.core.Id;
import com.kanokna.shared.core.PhoneNumber;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * User profile aggregate root.
 */
public class UserProfile {
    private final Id userId;
    private final Email email;
    private PersonName personName;
    private PhoneNumber phoneNumber;
    private LocalePreference preferredLanguage;
    private CurrencyPreference preferredCurrency;
    private NotificationPreferences notificationPreferences;
    private final Id partnerOrganizationId;
    private List<SavedAddress> addresses;
    private final Instant createdAt;
    private Instant updatedAt;
    private final int version;

    private UserProfile(Id userId,
                        Email email,
                        PersonName personName,
                        PhoneNumber phoneNumber,
                        LocalePreference preferredLanguage,
                        CurrencyPreference preferredCurrency,
                        NotificationPreferences notificationPreferences,
                        Id partnerOrganizationId,
                        Instant createdAt,
                        Instant updatedAt,
                        int version) {
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.email = Objects.requireNonNull(email, "email cannot be null");
        this.personName = personName;
        this.phoneNumber = phoneNumber;
        this.preferredLanguage = preferredLanguage;
        this.preferredCurrency = preferredCurrency;
        this.notificationPreferences = notificationPreferences;
        this.partnerOrganizationId = partnerOrganizationId;
        this.addresses = new ArrayList<>();
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public static UserProfile create(Id userId,
                                     Email email,
                                     PersonName personName,
                                     PhoneNumber phoneNumber,
                                     LocalePreference preferredLanguage,
                                     CurrencyPreference preferredCurrency,
                                     NotificationPreferences notificationPreferences,
                                     Id partnerOrganizationId,
                                     Instant now) {
        return new UserProfile(
            userId,
            email,
            personName,
            phoneNumber,
            preferredLanguage,
            preferredCurrency,
            notificationPreferences,
            partnerOrganizationId,
            now,
            now,
            0
        );
    }

    public static UserProfile rehydrate(Id userId,
                                        Email email,
                                        PersonName personName,
                                        PhoneNumber phoneNumber,
                                        LocalePreference preferredLanguage,
                                        CurrencyPreference preferredCurrency,
                                        NotificationPreferences notificationPreferences,
                                        Id partnerOrganizationId,
                                        Instant createdAt,
                                        Instant updatedAt,
                                        int version,
                                        List<SavedAddress> addresses) {
        UserProfile profile = new UserProfile(
            userId,
            email,
            personName,
            phoneNumber,
            preferredLanguage,
            preferredCurrency,
            notificationPreferences,
            partnerOrganizationId,
            createdAt,
            updatedAt,
            version
        );
        profile.replaceAddresses(addresses);
        return profile;
    }

    public Id userId() {
        return userId;
    }

    public Email email() {
        return email;
    }

    public PersonName personName() {
        return personName;
    }

    public PhoneNumber phoneNumber() {
        return phoneNumber;
    }

    public LocalePreference preferredLanguage() {
        return preferredLanguage;
    }

    public CurrencyPreference preferredCurrency() {
        return preferredCurrency;
    }

    public NotificationPreferences notificationPreferences() {
        return notificationPreferences;
    }

    public Id partnerOrganizationId() {
        return partnerOrganizationId;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }

    public int version() {
        return version;
    }

    public List<SavedAddress> addresses() {
        return List.copyOf(addresses);
    }

    public void replaceAddresses(List<SavedAddress> nextAddresses) {
        this.addresses = new ArrayList<>(Optional.ofNullable(nextAddresses).orElse(List.of()));
    }

    public List<String> applyProfileUpdates(PersonName updatedName,
                                            PhoneNumber updatedPhone,
                                            LocalePreference updatedLanguage,
                                            CurrencyPreference updatedCurrency,
                                            NotificationPreferences updatedPreferences,
                                            Instant now) {
        List<String> changed = new ArrayList<>();

        if (updatedName != null && !updatedName.sameAs(this.personName)) {
            this.personName = updatedName;
            changed.add("name");
        }
        if (updatedPhone != null && !Objects.equals(updatedPhone, this.phoneNumber)) {
            this.phoneNumber = updatedPhone;
            changed.add("phoneNumber");
        }
        if (updatedLanguage != null && !Objects.equals(updatedLanguage, this.preferredLanguage)) {
            this.preferredLanguage = updatedLanguage;
            changed.add("preferredLanguage");
        }
        if (updatedCurrency != null && !Objects.equals(updatedCurrency, this.preferredCurrency)) {
            this.preferredCurrency = updatedCurrency;
            changed.add("preferredCurrency");
        }
        if (updatedPreferences != null && !Objects.equals(updatedPreferences, this.notificationPreferences)) {
            this.notificationPreferences = updatedPreferences;
            changed.add("notificationPreferences");
        }
        if (!changed.isEmpty()) {
            this.updatedAt = now;
        }
        return changed;
    }

    public SavedAddress addAddress(Address address, String label, boolean setAsDefault, Instant now) {
        if (address == null) {
            throw AccountDomainErrors.invalidAddress("Address must be provided");
        }
        if (hasDuplicateAddress(address, null)) {
            throw AccountDomainErrors.addressDuplicate(address.city());
        }

        List<SavedAddress> updated = new ArrayList<>(addresses);
        if (setAsDefault) {
            updated = clearDefault(updated, now);
        }

        SavedAddress savedAddress = SavedAddress.create(Id.random(), address, label, setAsDefault, now);
        updated.add(savedAddress);
        addresses = updated;
        return savedAddress;
    }

    public SavedAddress updateAddress(Id addressId,
                                      Address updatedAddress,
                                      String updatedLabel,
                                      Boolean setAsDefault,
                                      Instant now) {
        SavedAddress existing = findAddress(addressId)
            .orElseThrow(() -> AccountDomainErrors.addressNotFound(addressId.value()));

        Address candidateAddress = updatedAddress != null ? updatedAddress : existing.address();
        if (hasDuplicateAddress(candidateAddress, addressId)) {
            throw AccountDomainErrors.addressDuplicate(candidateAddress.city());
        }

        List<SavedAddress> updated = new ArrayList<>(addresses);
        if (Boolean.TRUE.equals(setAsDefault)) {
            updated = clearDefault(updated, now);
        }

        SavedAddress updatedAddressValue = existing.update(updatedAddress, updatedLabel, setAsDefault, now);
        updated.removeIf(addr -> addr.addressId().equals(addressId));
        updated.add(updatedAddressValue);
        addresses = updated;
        return updatedAddressValue;
    }

    public void deleteAddress(Id addressId) {
        SavedAddress existing = findAddress(addressId)
            .orElseThrow(() -> AccountDomainErrors.addressNotFound(addressId.value()));
        List<SavedAddress> updated = new ArrayList<>(addresses);
        updated.removeIf(addr -> addr.addressId().equals(existing.addressId()));
        addresses = updated;
    }

    public List<SavedAddress> listAddressesSorted() {
        return addresses.stream()
            .sorted(Comparator.comparing(SavedAddress::label, String.CASE_INSENSITIVE_ORDER))
            .toList();
    }

    private Optional<SavedAddress> findAddress(Id addressId) {
        return addresses.stream().filter(addr -> addr.addressId().equals(addressId)).findFirst();
    }

    private boolean hasDuplicateAddress(Address address, Id excludeAddressId) {
        return addresses.stream()
            .filter(existing -> !existing.addressId().equals(excludeAddressId))
            .anyMatch(existing ->
                existing.address().line1().equals(address.line1())
                    && existing.address().city().equals(address.city())
                    && existing.address().postalCode().equals(address.postalCode())
            );
    }

    private List<SavedAddress> clearDefault(List<SavedAddress> current, Instant now) {
        List<SavedAddress> updated = new ArrayList<>();
        for (SavedAddress address : current) {
            if (address.isDefault()) {
                updated.add(address.withDefault(false, now));
            } else {
                updated.add(address);
            }
        }
        return updated;
    }
}
