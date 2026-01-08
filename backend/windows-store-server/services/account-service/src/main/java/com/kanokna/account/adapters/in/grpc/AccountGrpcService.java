package com.kanokna.account.adapters.in.grpc;

import com.kanokna.account.application.dto.*;
import com.kanokna.account.application.port.in.AddressManagementUseCase;
import com.kanokna.account.application.port.in.GetProfileUseCase;
import com.kanokna.account.application.port.in.UpdateProfileUseCase;
import com.kanokna.account.v1.*;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * gRPC service for account operations.
 */
@GrpcService
public class AccountGrpcService extends AccountServiceGrpc.AccountServiceImplBase {
    private final GetProfileUseCase getProfileUseCase;
    private final UpdateProfileUseCase updateProfileUseCase;
    private final AddressManagementUseCase addressManagementUseCase;
    private final AccountGrpcMapper mapper;

    public AccountGrpcService(GetProfileUseCase getProfileUseCase,
                              UpdateProfileUseCase updateProfileUseCase,
                              AddressManagementUseCase addressManagementUseCase,
                              AccountGrpcMapper mapper) {
        this.getProfileUseCase = getProfileUseCase;
        this.updateProfileUseCase = updateProfileUseCase;
        this.addressManagementUseCase = addressManagementUseCase;
        this.mapper = mapper;
    }

    @Override
    public void getProfile(GetProfileRequest request, StreamObserver<GetProfileResponse> responseObserver) {
        UserProfileDto profile = getProfileUseCase.getProfile(mapper.toQuery(request));
        responseObserver.onNext(mapper.toResponse(profile));
        responseObserver.onCompleted();
    }

    @Override
    public void updateProfile(UpdateProfileRequest request, StreamObserver<UpdateProfileResponse> responseObserver) {
        UserProfileDto profile = updateProfileUseCase.updateProfile(mapper.toCommand(request));
        responseObserver.onNext(mapper.toResponse(profile));
        responseObserver.onCompleted();
    }

    @Override
    public void addAddress(AddAddressRequest request, StreamObserver<AddAddressResponse> responseObserver) {
        SavedAddressDto address = addressManagementUseCase.addAddress(mapper.toCommand(request));
        responseObserver.onNext(mapper.toResponse(address));
        responseObserver.onCompleted();
    }

    @Override
    public void updateAddress(UpdateAddressRequest request, StreamObserver<UpdateAddressResponse> responseObserver) {
        SavedAddressDto address = addressManagementUseCase.updateAddress(mapper.toCommand(request));
        responseObserver.onNext(mapper.toResponse(address));
        responseObserver.onCompleted();
    }

    @Override
    public void deleteAddress(DeleteAddressRequest request, StreamObserver<DeleteAddressResponse> responseObserver) {
        addressManagementUseCase.deleteAddress(mapper.toCommand(request));
        responseObserver.onNext(mapper.toDeleteResponse(true));
        responseObserver.onCompleted();
    }

    @Override
    public void listAddresses(ListAddressesRequest request, StreamObserver<ListAddressesResponse> responseObserver) {
        java.util.List<SavedAddressDto> addresses = addressManagementUseCase.listAddresses(mapper.toQuery(request));
        responseObserver.onNext(mapper.toResponse(addresses));
        responseObserver.onCompleted();
    }

    @Override
    public void getOrderHistory(GetOrderHistoryRequest request, StreamObserver<GetOrderHistoryResponse> responseObserver) {
        responseObserver.onNext(GetOrderHistoryResponse.newBuilder().build());
        responseObserver.onCompleted();
    }
}
