package com.kanokna.account.application.service;

import com.kanokna.account.application.dto.AddressDto;
import com.kanokna.account.application.dto.SavedAddressDto;
import com.kanokna.account.application.port.out.CurrentUser;
import com.kanokna.account.application.port.out.CurrentUserProvider;
import com.kanokna.account.domain.exception.AccountDomainErrors;
import com.kanokna.account.domain.model.SavedAddress;
import com.kanokna.shared.core.Id;
import java.util.Objects;

final class AccountServiceUtils {
    private static final String SERVICE = "account-service";

    private AccountServiceUtils() {
        throw new IllegalStateException("Utility class");
    }

    static CurrentUser requireCurrentUser(CurrentUserProvider currentUserProvider) {
        CurrentUser currentUser = currentUserProvider.currentUser();
        if (currentUser == null || currentUser.userId() == null || currentUser.userId().isBlank()) {
            throw AccountDomainErrors.unauthorized("Unauthenticated request");
        }
        return currentUser;
    }

    static void authorizeAccess(CurrentUser currentUser, Id requestedUserId) {
        if (currentUser.isAdmin()) {
            return;
        }
        if (!Objects.equals(currentUser.userId(), requestedUserId.value())) {
            throw AccountDomainErrors.unauthorized("User not authorized for requested resource");
        }
    }

    static String logLine(String useCase, String block, String state,
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

    static SavedAddressDto toDto(SavedAddress address) {
        return new SavedAddressDto(
            address.addressId().value(),
            AddressDto.fromValueObject(address.address()),
            address.label(),
            address.isDefault(),
            address.createdAt(),
            address.updatedAt()
        );
    }
}
