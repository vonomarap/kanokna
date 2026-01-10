package com.kanokna.cart.adapters.in.grpc;

import com.kanokna.cart.application.dto.AddItemResult;
import com.kanokna.cart.application.dto.ApplyPromoCodeResult;
import com.kanokna.cart.application.dto.CartDto;
import com.kanokna.cart.application.dto.CreateSnapshotResult;
import com.kanokna.cart.application.dto.MergeCartsResult;
import com.kanokna.cart.application.dto.RefreshPricesResult;
import com.kanokna.cart.application.port.in.AddItemUseCase;
import com.kanokna.cart.application.port.in.ApplyPromoCodeUseCase;
import com.kanokna.cart.application.port.in.ClearCartUseCase;
import com.kanokna.cart.application.port.in.CreateSnapshotUseCase;
import com.kanokna.cart.application.port.in.GetCartUseCase;
import com.kanokna.cart.application.port.in.MergeCartsUseCase;
import com.kanokna.cart.application.port.in.RefreshPricesUseCase;
import com.kanokna.cart.application.port.in.RemoveItemUseCase;
import com.kanokna.cart.application.port.in.RemovePromoCodeUseCase;
import com.kanokna.cart.application.port.in.UpdateItemUseCase;
import com.kanokna.cart.v1.AddItemRequest;
import com.kanokna.cart.v1.AddItemResponse;
import com.kanokna.cart.v1.ApplyPromoCodeRequest;
import com.kanokna.cart.v1.ApplyPromoCodeResponse;
import com.kanokna.cart.v1.CartServiceGrpc;
import com.kanokna.cart.v1.ClearCartRequest;
import com.kanokna.cart.v1.ClearCartResponse;
import com.kanokna.cart.v1.CreateSnapshotRequest;
import com.kanokna.cart.v1.CreateSnapshotResponse;
import com.kanokna.cart.v1.GetCartRequest;
import com.kanokna.cart.v1.GetCartResponse;
import com.kanokna.cart.v1.MergeCartsRequest;
import com.kanokna.cart.v1.MergeCartsResponse;
import com.kanokna.cart.v1.RefreshPricesRequest;
import com.kanokna.cart.v1.RefreshPricesResponse;
import com.kanokna.cart.v1.RemoveItemRequest;
import com.kanokna.cart.v1.RemoveItemResponse;
import com.kanokna.cart.v1.RemovePromoCodeRequest;
import com.kanokna.cart.v1.RemovePromoCodeResponse;
import com.kanokna.cart.v1.UpdateItemRequest;
import com.kanokna.cart.v1.UpdateItemResponse;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

/**
 * gRPC service for cart operations.
 */
@GrpcService
public class CartGrpcService extends CartServiceGrpc.CartServiceImplBase {
    private final GetCartUseCase getCartUseCase;
    private final AddItemUseCase addItemUseCase;
    private final UpdateItemUseCase updateItemUseCase;
    private final RemoveItemUseCase removeItemUseCase;
    private final ClearCartUseCase clearCartUseCase;
    private final RefreshPricesUseCase refreshPricesUseCase;
    private final ApplyPromoCodeUseCase applyPromoCodeUseCase;
    private final RemovePromoCodeUseCase removePromoCodeUseCase;
    private final MergeCartsUseCase mergeCartsUseCase;
    private final CreateSnapshotUseCase createSnapshotUseCase;
    private final CartGrpcMapper mapper;

    public CartGrpcService(GetCartUseCase getCartUseCase,
                           AddItemUseCase addItemUseCase,
                           UpdateItemUseCase updateItemUseCase,
                           RemoveItemUseCase removeItemUseCase,
                           ClearCartUseCase clearCartUseCase,
                           RefreshPricesUseCase refreshPricesUseCase,
                           ApplyPromoCodeUseCase applyPromoCodeUseCase,
                           RemovePromoCodeUseCase removePromoCodeUseCase,
                           MergeCartsUseCase mergeCartsUseCase,
                           CreateSnapshotUseCase createSnapshotUseCase,
                           CartGrpcMapper mapper) {
        this.getCartUseCase = getCartUseCase;
        this.addItemUseCase = addItemUseCase;
        this.updateItemUseCase = updateItemUseCase;
        this.removeItemUseCase = removeItemUseCase;
        this.clearCartUseCase = clearCartUseCase;
        this.refreshPricesUseCase = refreshPricesUseCase;
        this.applyPromoCodeUseCase = applyPromoCodeUseCase;
        this.removePromoCodeUseCase = removePromoCodeUseCase;
        this.mergeCartsUseCase = mergeCartsUseCase;
        this.createSnapshotUseCase = createSnapshotUseCase;
        this.mapper = mapper;
    }

    @Override
    public void getCart(GetCartRequest request, StreamObserver<GetCartResponse> responseObserver) {
        CartDto cart = getCartUseCase.getCart(mapper.toQuery(request));
        responseObserver.onNext(mapper.toResponse(cart));
        responseObserver.onCompleted();
    }

    @Override
    public void addItem(AddItemRequest request, StreamObserver<AddItemResponse> responseObserver) {
        AddItemResult result = addItemUseCase.addItem(mapper.toCommand(request));
        responseObserver.onNext(mapper.toResponse(result));
        responseObserver.onCompleted();
    }

    @Override
    public void updateItem(UpdateItemRequest request, StreamObserver<UpdateItemResponse> responseObserver) {
        CartDto cart = updateItemUseCase.updateItem(mapper.toCommand(request));
        responseObserver.onNext(mapper.toResponse(cart));
        responseObserver.onCompleted();
    }

    @Override
    public void removeItem(RemoveItemRequest request, StreamObserver<RemoveItemResponse> responseObserver) {
        CartDto cart = removeItemUseCase.removeItem(mapper.toCommand(request));
        responseObserver.onNext(mapper.toResponseRemoved(cart));
        responseObserver.onCompleted();
    }

    @Override
    public void clearCart(ClearCartRequest request, StreamObserver<ClearCartResponse> responseObserver) {
        CartDto cart = clearCartUseCase.clearCart(mapper.toCommand(request));
        responseObserver.onNext(mapper.toResponseCleared(cart));
        responseObserver.onCompleted();
    }

    @Override
    public void refreshPrices(RefreshPricesRequest request, StreamObserver<RefreshPricesResponse> responseObserver) {
        RefreshPricesResult result = refreshPricesUseCase.refreshPrices(mapper.toCommand(request));
        responseObserver.onNext(mapper.toResponse(result));
        responseObserver.onCompleted();
    }

    @Override
    public void applyPromoCode(ApplyPromoCodeRequest request, StreamObserver<ApplyPromoCodeResponse> responseObserver) {
        ApplyPromoCodeResult result = applyPromoCodeUseCase.applyPromoCode(mapper.toCommand(request));
        responseObserver.onNext(mapper.toResponse(result));
        responseObserver.onCompleted();
    }

    @Override
    public void removePromoCode(RemovePromoCodeRequest request,
                                StreamObserver<RemovePromoCodeResponse> responseObserver) {
        CartDto cart = removePromoCodeUseCase.removePromoCode(mapper.toCommand(request));
        responseObserver.onNext(mapper.toResponsePromoRemoved(cart));
        responseObserver.onCompleted();
    }

    @Override
    public void mergeCarts(MergeCartsRequest request, StreamObserver<MergeCartsResponse> responseObserver) {
        MergeCartsResult result = mergeCartsUseCase.mergeCarts(mapper.toCommand(request));
        responseObserver.onNext(mapper.toResponse(result));
        responseObserver.onCompleted();
    }

    @Override
    public void createSnapshot(CreateSnapshotRequest request,
                               StreamObserver<CreateSnapshotResponse> responseObserver) {
        CreateSnapshotResult result = createSnapshotUseCase.createSnapshot(mapper.toCommand(request));
        responseObserver.onNext(mapper.toResponse(result));
        responseObserver.onCompleted();
    }
}
