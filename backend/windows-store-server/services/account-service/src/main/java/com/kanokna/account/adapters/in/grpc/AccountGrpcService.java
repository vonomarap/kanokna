package com.kanokna.account.adapters.in.grpc;

import java.util.List;

import org.springframework.grpc.server.service.GrpcService;

import com.kanokna.account.application.dto.SavedAddressDto;
import com.kanokna.account.application.dto.UserProfileDto;
import com.kanokna.account.application.port.in.AddressManagementUseCase;
import com.kanokna.account.application.port.in.GetProfileUseCase;
import com.kanokna.account.application.port.in.UpdateProfileUseCase;
import com.kanokna.account.v1.AccountServiceGrpc;
import com.kanokna.account.v1.AddAddressRequest;
import com.kanokna.account.v1.AddAddressResponse;
import com.kanokna.account.v1.DeleteAddressRequest;
import com.kanokna.account.v1.DeleteAddressResponse;
import com.kanokna.account.v1.GetOrderHistoryRequest;
import com.kanokna.account.v1.GetOrderHistoryResponse;
import com.kanokna.account.v1.GetProfileRequest;
import com.kanokna.account.v1.GetProfileResponse;
import com.kanokna.account.v1.ListAddressesRequest;
import com.kanokna.account.v1.ListAddressesResponse;
import com.kanokna.account.v1.UpdateAddressRequest;
import com.kanokna.account.v1.UpdateAddressResponse;
import com.kanokna.account.v1.UpdateProfileRequest;
import com.kanokna.account.v1.UpdateProfileResponse;

import io.grpc.stub.StreamObserver;

/**
 * MODULE_CONTRACT id="MC-account-grpc-adapter" LAYER="adapters.in.grpc"
 * INTENT="gRPC adapter translating proto requests to application use case
 * calls"
 * LINKS="Technology.xml#TECH-grpc;RequirementsAnalysis.xml#UC-ACCOUNT-MANAGE-PROFILE"
 *
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
        responseObserver.onNext(mapper.toGetProfileResponse(profile));
        responseObserver.onCompleted();
    }

    @Override
    public void updateProfile(UpdateProfileRequest request, StreamObserver<UpdateProfileResponse> responseObserver) {
        UserProfileDto profile = updateProfileUseCase.updateProfile(mapper.toCommand(request));
        responseObserver.onNext(mapper.toUpdateProfileResponse(profile));
        responseObserver.onCompleted();
    }

    @Override
    public void addAddress(AddAddressRequest request, StreamObserver<AddAddressResponse> responseObserver) {
        SavedAddressDto address = addressManagementUseCase.addAddress(mapper.toCommand(request));
        responseObserver.onNext(mapper.toAddAddressResponse(address));
        responseObserver.onCompleted();
    }

    @Override
    public void updateAddress(UpdateAddressRequest request, StreamObserver<UpdateAddressResponse> responseObserver) {
        SavedAddressDto address = addressManagementUseCase.updateAddress(mapper.toCommand(request));
        responseObserver.onNext(mapper.toUpdateAddressResponse(address));
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
        List<SavedAddressDto> addresses = addressManagementUseCase.listAddresses(mapper.toQuery(request));
        responseObserver.onNext(mapper.toResponse(addresses));
        responseObserver.onCompleted();
    }

    @Override
    public void getOrderHistory(GetOrderHistoryRequest request, StreamObserver<GetOrderHistoryResponse> responseObserver) {
        responseObserver.onNext(GetOrderHistoryResponse.newBuilder().build());
        responseObserver.onCompleted();
    }
}
