package com.kanokna.account.adapters.in.grpc;

import com.kanokna.account.application.dto.*;
import com.kanokna.account.application.port.in.AddressManagementUseCase;
import com.kanokna.account.application.port.in.GetProfileUseCase;
import com.kanokna.account.application.port.in.UpdateProfileUseCase;
import com.kanokna.account.v1.GetProfileRequest;
import com.kanokna.account.v1.GetProfileResponse;
import com.kanokna.account.v1.ListAddressesRequest;
import com.kanokna.account.v1.ListAddressesResponse;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountGrpcServiceIntegrationTest {

    @Test
    @DisplayName("GetProfile maps request and response")
    void getProfileMapsRequestAndResponse() {
        GetProfileUseCase getProfileUseCase = mock(GetProfileUseCase.class);
        UpdateProfileUseCase updateProfileUseCase = mock(UpdateProfileUseCase.class);
        AddressManagementUseCase addressManagementUseCase = mock(AddressManagementUseCase.class);
        AccountGrpcMapper mapper = new AccountGrpcMapper();
        AccountGrpcService service = new AccountGrpcService(
            getProfileUseCase,
            updateProfileUseCase,
            addressManagementUseCase,
            mapper
        );

        UserProfileDto profile = new UserProfileDto(
            "user-1",
            "user@example.com",
            "Jane",
            "Doe",
            "+79001234567",
            "ru",
            "RUB",
            new NotificationPreferencesDto(true, false, true),
            List.of(),
            Instant.parse("2026-01-01T10:00:00Z"),
            Instant.parse("2026-01-01T10:10:00Z")
        );
        when(getProfileUseCase.getProfile(any())).thenReturn(profile);

        GetProfileRequest request = GetProfileRequest.newBuilder()
            .setUserId(UUID.randomUUID().toString())
            .build();

        TestObserver<GetProfileResponse> observer = new TestObserver<>();
        service.getProfile(request, observer);

        assertNull(observer.error);
        assertTrue(observer.completed);
        assertNotNull(observer.value);
        assertEquals("user-1", observer.value.getProfile().getUserId());
        assertEquals("user@example.com", observer.value.getProfile().getEmail());

        ArgumentCaptor<GetProfileQuery> captor = ArgumentCaptor.forClass(GetProfileQuery.class);
        verify(getProfileUseCase).getProfile(captor.capture());
        assertEquals(request.getUserId(), captor.getValue().userId().toString());
    }

    @Test
    @DisplayName("ListAddresses maps response")
    void listAddressesMapsResponse() {
        GetProfileUseCase getProfileUseCase = mock(GetProfileUseCase.class);
        UpdateProfileUseCase updateProfileUseCase = mock(UpdateProfileUseCase.class);
        AddressManagementUseCase addressManagementUseCase = mock(AddressManagementUseCase.class);
        AccountGrpcMapper mapper = new AccountGrpcMapper();
        AccountGrpcService service = new AccountGrpcService(
            getProfileUseCase,
            updateProfileUseCase,
            addressManagementUseCase,
            mapper
        );

        SavedAddressDto address = new SavedAddressDto(
            UUID.randomUUID().toString(),
            new AddressDto("RU", "Moscow", "123456", "Main Street", null),
            "Home",
            true,
            Instant.parse("2026-01-01T10:00:00Z"),
            Instant.parse("2026-01-01T10:00:00Z")
        );
        when(addressManagementUseCase.listAddresses(any())).thenReturn(List.of(address));

        ListAddressesRequest request = ListAddressesRequest.newBuilder()
            .setUserId(UUID.randomUUID().toString())
            .build();

        TestObserver<ListAddressesResponse> observer = new TestObserver<>();
        service.listAddresses(request, observer);

        assertNull(observer.error);
        assertTrue(observer.completed);
        assertNotNull(observer.value);
        assertEquals(1, observer.value.getAddressesCount());
        assertEquals("Home", observer.value.getAddresses(0).getLabel());
    }

    private static class TestObserver<T> implements StreamObserver<T> {
        private T value;
        private Throwable error;
        private boolean completed;

        @Override
        public void onNext(T value) {
            this.value = value;
        }

        @Override
        public void onError(Throwable t) {
            this.error = t;
        }

        @Override
        public void onCompleted() {
            this.completed = true;
        }
    }
}
