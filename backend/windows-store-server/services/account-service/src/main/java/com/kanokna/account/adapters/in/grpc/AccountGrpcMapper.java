package com.kanokna.account.adapters.in.grpc;

import com.google.protobuf.Timestamp;
import com.kanokna.account.application.dto.*;
import com.kanokna.account.v1.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Mapper between account DTOs and protobuf messages.
 */
@Component
public class AccountGrpcMapper {
    public GetProfileQuery toQuery(GetProfileRequest request) {
        return new GetProfileQuery(UUID.fromString(request.getUserId()));
    }

    public UpdateProfileCommand toCommand(UpdateProfileRequest request) {
        return new UpdateProfileCommand(
            UUID.fromString(request.getUserId()),
            blankToNull(request.getFirstName()),
            blankToNull(request.getLastName()),
            blankToNull(request.getPhoneNumber()),
            blankToNull(request.getPreferredLanguage()),
            blankToNull(request.getPreferredCurrency())
        );
    }

    public AddAddressCommand toCommand(AddAddressRequest request) {
        return new AddAddressCommand(
            UUID.fromString(request.getUserId()),
            toDto(request.getAddress()),
            request.getLabel(),
            request.getSetAsDefault()
        );
    }

    public UpdateAddressCommand toCommand(UpdateAddressRequest request) {
        AddressDto address = request.hasAddress() ? toDto(request.getAddress()) : null;
        return new UpdateAddressCommand(
            UUID.fromString(request.getUserId()),
            UUID.fromString(request.getAddressId()),
            address,
            blankToNull(request.getLabel()),
            request.getSetAsDefault()
        );
    }

    public DeleteAddressCommand toCommand(DeleteAddressRequest request) {
        return new DeleteAddressCommand(
            UUID.fromString(request.getUserId()),
            UUID.fromString(request.getAddressId())
        );
    }

    public ListAddressesQuery toQuery(ListAddressesRequest request) {
        return new ListAddressesQuery(UUID.fromString(request.getUserId()));
    }

    public GetProfileResponse toResponse(UserProfileDto profile) {
        return GetProfileResponse.newBuilder()
            .setProfile(toProto(profile))
            .build();
    }

    public UpdateProfileResponse toResponse(UserProfileDto profile) {
        return UpdateProfileResponse.newBuilder()
            .setProfile(toProto(profile))
            .build();
    }

    public AddAddressResponse toResponse(SavedAddressDto address) {
        return AddAddressResponse.newBuilder()
            .setAddress(toProto(address))
            .build();
    }

    public UpdateAddressResponse toResponse(SavedAddressDto address) {
        return UpdateAddressResponse.newBuilder()
            .setAddress(toProto(address))
            .build();
    }

    public DeleteAddressResponse toDeleteResponse(boolean success) {
        return DeleteAddressResponse.newBuilder()
            .setSuccess(success)
            .build();
    }

    public ListAddressesResponse toResponse(java.util.List<SavedAddressDto> addresses) {
        ListAddressesResponse.Builder builder = ListAddressesResponse.newBuilder();
        if (addresses != null) {
            addresses.forEach(address -> builder.addAddresses(toProto(address)));
        }
        return builder.build();
    }

    private UserProfile toProto(UserProfileDto profile) {
        UserProfile.Builder builder = UserProfile.newBuilder()
            .setUserId(profile.userId())
            .setEmail(profile.email() == null ? "" : profile.email())
            .setFirstName(profile.firstName() == null ? "" : profile.firstName())
            .setLastName(profile.lastName() == null ? "" : profile.lastName())
            .setPhoneNumber(profile.phoneNumber() == null ? "" : profile.phoneNumber())
            .setPreferredLanguage(profile.preferredLanguage() == null ? "" : profile.preferredLanguage())
            .setPreferredCurrency(profile.preferredCurrency() == null ? "" : profile.preferredCurrency());

        if (profile.createdAt() != null) {
            builder.setCreatedAt(toTimestamp(profile.createdAt()));
        }
        if (profile.updatedAt() != null) {
            builder.setUpdatedAt(toTimestamp(profile.updatedAt()));
        }
        return builder.build();
    }

    private SavedAddress toProto(SavedAddressDto address) {
        return SavedAddress.newBuilder()
            .setAddressId(address.addressId())
            .setLabel(address.label())
            .setIsDefault(address.isDefault())
            .setAddress(toProto(address.address()))
            .build();
    }

    private com.kanokna.common.v1.Address toProto(AddressDto address) {
        return com.kanokna.common.v1.Address.newBuilder()
            .setCountry(address.country())
            .setCity(address.city())
            .setPostalCode(address.postalCode())
            .setLine1(address.line1())
            .setLine2(address.line2() == null ? "" : address.line2())
            .build();
    }

    private AddressDto toDto(com.kanokna.common.v1.Address address) {
        return new AddressDto(
            address.getCountry(),
            address.getCity(),
            address.getPostalCode(),
            address.getLine1(),
            blankToNull(address.getLine2())
        );
    }

    private Timestamp toTimestamp(Instant instant) {
        return Timestamp.newBuilder()
            .setSeconds(instant.getEpochSecond())
            .setNanos(instant.getNano())
            .build();
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
