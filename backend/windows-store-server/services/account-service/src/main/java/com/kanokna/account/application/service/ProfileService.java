package com.kanokna.account.application.service;

import com.kanokna.account.adapters.config.AccountProperties;
import com.kanokna.account.application.dto.GetProfileQuery;
import com.kanokna.account.application.dto.NotificationPreferencesDto;
import com.kanokna.account.application.dto.UpdateProfileCommand;
import com.kanokna.account.application.dto.UserProfileDto;
import com.kanokna.account.application.port.out.CurrentUser;
import com.kanokna.account.application.port.out.CurrentUserProvider;
import com.kanokna.account.application.port.out.EventPublisher;
import com.kanokna.account.application.port.out.UserProfileRepository;
import com.kanokna.account.domain.event.UserProfileCreatedEvent;
import com.kanokna.account.domain.event.UserProfileUpdatedEvent;
import com.kanokna.account.domain.exception.AccountDomainErrors;
import com.kanokna.account.domain.model.CurrencyPreference;
import com.kanokna.account.domain.model.LocalePreference;
import com.kanokna.account.domain.model.NotificationPreferences;
import com.kanokna.account.domain.model.PersonName;
import com.kanokna.account.domain.model.SavedAddress;
import com.kanokna.account.domain.model.UserProfile;
import com.kanokna.shared.core.Email;
import com.kanokna.shared.core.Id;
import com.kanokna.shared.core.PhoneNumber;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            <Item>Profile is always returned for valid authenticated user (auto-
                create on first
                access)</Item>
            <Item>Read operation is idempotent for existing profiles</Item>
        </INVARIANTS>

        <ERROR_HANDLING>
            <Item type="BUSINESS" code="ERR-ACCT-UNAUTHORIZED">Caller attempting
                to access another
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

/**
 * MODULE_CONTRACT id="MC-account-profile-service"
 * LAYER="application.service"
 * INTENT="Specialized service for user profile CRUD operations"
 * LINKS="Technology.xml#DEC-ACCOUNT-DECOMPOSITION;RequirementsAnalysis.xml#UC-ACCOUNT-MANAGE-PROFILE"
 */
@Service
@Transactional
public class ProfileService {
    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);
    private static final String USE_CASE_PROFILE = "UC-ACCOUNT-MANAGE-PROFILE";

    private final UserProfileRepository userProfileRepository;
    private final EventPublisher eventPublisher;
    private final CurrentUserProvider currentUserProvider;
    private final AccountProperties accountProperties;

    public ProfileService(
        UserProfileRepository userProfileRepository,
        EventPublisher eventPublisher,
        CurrentUserProvider currentUserProvider,
        AccountProperties accountProperties
    ) {
        this.userProfileRepository = userProfileRepository;
        this.eventPublisher = eventPublisher;
        this.currentUserProvider = currentUserProvider;
        this.accountProperties = accountProperties;
    }

    public UserProfileDto getProfile(GetProfileQuery query) {
        Id requestedUserId = Id.of(query.userId().toString());
        CurrentUser currentUser = AccountServiceUtils.requireCurrentUser(currentUserProvider);
        AccountServiceUtils.authorizeAccess(currentUser, requestedUserId);

        // BA-ACCT-PROF-01: Fetch user profile from repository
        log.info(AccountServiceUtils.logLine(USE_CASE_PROFILE, "BA-ACCT-PROF-01", "FETCHING",
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
            log.info(AccountServiceUtils.logLine(USE_CASE_PROFILE, "BA-ACCT-PROF-03", "SYNCING",
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
        log.info(AccountServiceUtils.logLine(USE_CASE_PROFILE, "BA-ACCT-PROF-01", "COMPLETE",
            "PROFILE_GET", "SUCCESS", "userId=" + requestedUserId.value() + ",addressCount=" + addressCount));

        return toDto(profile, profile.addresses());
    }

    public UserProfileDto updateProfile(UpdateProfileCommand command) {
        Id requestedUserId = Id.of(command.userId().toString());
        CurrentUser currentUser = AccountServiceUtils.requireCurrentUser(currentUserProvider);
        AccountServiceUtils.authorizeAccess(currentUser, requestedUserId);

        if (command.firstName() == null && command.lastName() == null
            && command.phoneNumber() == null && command.preferredLanguage() == null
            && command.preferredCurrency() == null) {
            throw new IllegalArgumentException("At least one field must be provided");
        }

        // BA-ACCT-PROF-01: Fetch user profile from repository
        log.info(AccountServiceUtils.logLine(USE_CASE_PROFILE, "BA-ACCT-PROF-01", "FETCHING",
            "PROFILE_UPDATE_START", "LOOKUP", "userId=" + requestedUserId.value()));

        UserProfile profile = userProfileRepository.findById(requestedUserId)
            .orElseThrow(() -> AccountDomainErrors.profileNotFound(requestedUserId.value()));

        PersonName updatedName = buildUpdatedName(profile, command);
        PhoneNumber updatedPhone = parsePhone(command.phoneNumber());
        LocalePreference updatedLanguage = parseLanguage(command.preferredLanguage());
        CurrencyPreference updatedCurrency = parseCurrency(command.preferredCurrency());

        String fields = renderUpdatedFields(command);
        // BA-ACCT-PROF-02: Validate and apply profile updates
        log.info(AccountServiceUtils.logLine(USE_CASE_PROFILE, "BA-ACCT-PROF-02", "VALIDATING",
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

        log.info(AccountServiceUtils.logLine(USE_CASE_PROFILE, "BA-ACCT-PROF-02", "UPDATED",
            "PROFILE_UPDATED", "COMMIT", "userId=" + requestedUserId.value() + ",fields=" + fields));

        return toDto(profile, profile.addresses());
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
            addresses != null ? addresses.stream().map(AccountServiceUtils::toDto).toList() : List.of(),
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
}
