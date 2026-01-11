package com.kanokna.account.adapters.out.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.account.domain.model.CurrencyPreference;
import com.kanokna.account.domain.model.LocalePreference;
import com.kanokna.account.domain.model.NotificationPreferences;
import com.kanokna.account.domain.model.PersonName;
import com.kanokna.account.domain.model.SavedAddress;
import com.kanokna.account.domain.model.SavedConfiguration;
import com.kanokna.account.domain.model.UserProfile;
import com.kanokna.account.domain.model.ConfigurationSnapshot;
import com.kanokna.account.domain.model.QuoteSnapshot;
import com.kanokna.shared.core.Address;
import com.kanokna.shared.core.Email;
import com.kanokna.shared.core.Id;
import com.kanokna.shared.core.PhoneNumber;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Mapper between account domain models and JPA entities.
 */
@Component
public class AccountPersistenceMapper {
    private final ObjectMapper objectMapper;

    public AccountPersistenceMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public UserProfile toDomain(UserProfileJpaEntity entity, List<SavedAddressJpaEntity> addressEntities) {
        PersonName name = new PersonName(entity.getFirstName(), entity.getLastName());
        PhoneNumber phone = entity.getPhoneNumber() != null ? PhoneNumber.of(entity.getPhoneNumber()) : null;
        LocalePreference localePreference = entity.getPreferredLanguage() != null
            ? new LocalePreference(entity.getPreferredLanguage())
            : null;
        CurrencyPreference currencyPreference = entity.getPreferredCurrency() != null
            ? new CurrencyPreference(entity.getPreferredCurrency())
            : null;
        NotificationPreferences notificationPreferences = readPreferences(entity.getNotificationPreferences());
        Id partnerId = entity.getPartnerOrganizationId() != null
            ? Id.of(entity.getPartnerOrganizationId().toString())
            : null;

        List<SavedAddress> addresses = addressEntities == null
            ? List.of()
            : addressEntities.stream().map(this::toDomain).toList();

        return UserProfile.rehydrate(
            Id.of(entity.getId().toString()),
            Email.of(entity.getEmail()),
            name,
            phone,
            localePreference,
            currencyPreference,
            notificationPreferences,
            partnerId,
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            entity.getVersion(),
            addresses
        );
    }

    public UserProfileJpaEntity toEntity(UserProfile profile) {
        UserProfileJpaEntity entity = new UserProfileJpaEntity();
        entity.setId(UUID.fromString(profile.userId().value()));
        entity.setEmail(profile.email().asString());
        entity.setFirstName(profile.personName() != null ? profile.personName().firstName() : null);
        entity.setLastName(profile.personName() != null ? profile.personName().lastName() : null);
        entity.setPhoneNumber(profile.phoneNumber() != null ? profile.phoneNumber().value() : null);
        entity.setPreferredLanguage(profile.preferredLanguage() != null
            ? profile.preferredLanguage().languageTag()
            : null);
        entity.setPreferredCurrency(profile.preferredCurrency() != null
            ? profile.preferredCurrency().currencyCode()
            : null);
        entity.setNotificationPreferences(writePreferences(profile.notificationPreferences()));
        entity.setPartnerOrganizationId(profile.partnerOrganizationId() != null
            ? UUID.fromString(profile.partnerOrganizationId().value())
            : null);
        entity.setVersion(profile.version());
        entity.setCreatedAt(profile.createdAt());
        entity.setUpdatedAt(profile.updatedAt());
        return entity;
    }

    public SavedAddress toDomain(SavedAddressJpaEntity entity) {
        Address address = toAddress(entity);
        return SavedAddress.rehydrate(
            Id.of(entity.getId().toString()),
            address,
            entity.getLabel(),
            entity.isDefault(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public SavedAddressJpaEntity toEntity(Id userId, SavedAddress address) {
        SavedAddressJpaEntity entity = new SavedAddressJpaEntity();
        entity.setId(UUID.fromString(address.addressId().value()));
        entity.setUserId(UUID.fromString(userId.value()));
        entity.setLabel(address.label());
        entity.setStreet(address.address().line1());
        entity.setCity(address.address().city());
        entity.setPostalCode(address.address().postalCode());
        entity.setCountry(address.address().country());
        // Persist Address.line2 into region column (no dedicated line2 column in schema).
        entity.setRegion(address.address().line2());
        entity.setDefault(address.isDefault());
        entity.setCreatedAt(address.createdAt());
        entity.setUpdatedAt(address.updatedAt());
        return entity;
    }

    public SavedConfiguration toDomain(SavedConfigurationJpaEntity entity) {
        return new SavedConfiguration(
            Id.of(entity.getId().toString()),
            Id.of(entity.getUserId().toString()),
            entity.getName(),
            Id.of(entity.getProductTemplateId().toString()),
            new ConfigurationSnapshot(entity.getConfigurationSnapshot()),
            entity.getQuoteSnapshot() != null ? new QuoteSnapshot(entity.getQuoteSnapshot()) : null,
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public SavedConfigurationJpaEntity toEntity(SavedConfiguration configuration) {
        SavedConfigurationJpaEntity entity = new SavedConfigurationJpaEntity();
        entity.setId(UUID.fromString(configuration.configurationId().value()));
        entity.setUserId(UUID.fromString(configuration.userId().value()));
        entity.setName(configuration.name());
        entity.setProductTemplateId(UUID.fromString(configuration.productTemplateId().value()));
        entity.setConfigurationSnapshot(configuration.configurationSnapshot().json());
        entity.setQuoteSnapshot(configuration.quoteSnapshot() != null ? configuration.quoteSnapshot().json() : null);
        entity.setCreatedAt(configuration.createdAt());
        entity.setUpdatedAt(configuration.updatedAt());
        return entity;
    }

    private Address toAddress(SavedAddressJpaEntity entity) {
        return new Address(
            entity.getCountry(),
            entity.getCity(),
            entity.getPostalCode(),
            entity.getStreet(),
            entity.getRegion()
        );
    }

    private NotificationPreferences readPreferences(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, NotificationPreferences.class);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse notification preferences JSON", ex);
        }
    }

    private String writePreferences(NotificationPreferences preferences) {
        if (preferences == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(preferences);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize notification preferences JSON", ex);
        }
    }
}
