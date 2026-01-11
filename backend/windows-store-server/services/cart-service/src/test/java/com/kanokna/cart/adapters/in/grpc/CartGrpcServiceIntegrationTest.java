package com.kanokna.cart.adapters.in.grpc;

import com.kanokna.cart.application.dto.AddItemCommand;
import com.kanokna.cart.application.dto.AddItemResult;
import com.kanokna.cart.application.dto.ApplyPromoCodeCommand;
import com.kanokna.cart.application.dto.ApplyPromoCodeResult;
import com.kanokna.cart.application.dto.AppliedPromoCodeDto;
import com.kanokna.cart.application.dto.BomLineDto;
import com.kanokna.cart.application.dto.CartDto;
import com.kanokna.cart.application.dto.CartItemDto;
import com.kanokna.cart.application.dto.CartSnapshotDto;
import com.kanokna.cart.application.dto.ClearCartCommand;
import com.kanokna.cart.application.dto.CreateSnapshotCommand;
import com.kanokna.cart.application.dto.CreateSnapshotResult;
import com.kanokna.cart.application.dto.DimensionsDto;
import com.kanokna.cart.application.dto.GetCartQuery;
import com.kanokna.cart.application.dto.MergeCartsCommand;
import com.kanokna.cart.application.dto.MergeCartsResult;
import com.kanokna.cart.application.dto.RefreshPricesCommand;
import com.kanokna.cart.application.dto.RefreshPricesResult;
import com.kanokna.cart.application.dto.RemoveItemCommand;
import com.kanokna.cart.application.dto.RemovePromoCodeCommand;
import com.kanokna.cart.application.dto.SelectedOptionDto;
import com.kanokna.cart.application.dto.UpdateItemCommand;
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
import com.kanokna.cart.domain.model.CartStatus;
import com.kanokna.cart.domain.model.ValidationStatus;
import com.kanokna.cart.v1.AddItemRequest;
import com.kanokna.cart.v1.AddItemResponse;
import com.kanokna.cart.v1.ApplyPromoCodeRequest;
import com.kanokna.cart.v1.ApplyPromoCodeResponse;
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
import com.kanokna.catalog.v1.BillOfMaterials;
import com.kanokna.catalog.v1.BomLine;
import com.kanokna.catalog.v1.SelectedOption;
import com.kanokna.common.v1.Dimensions;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CartGrpcServiceIntegrationTest {
    @Test
    @DisplayName("TC-FUNC-CART-GET-001: GetCart maps request and response")
    void getCartMapsRequestAndResponse() {
        TestSetup setup = new TestSetup();
        CartDto cartDto = sampleCartDto();
        when(setup.getCartUseCase.getCart(any())).thenReturn(cartDto);

        GetCartRequest request = GetCartRequest.newBuilder()
            .setCustomerId("cust-1")
            .setSessionId("sess-1")
            .build();

        TestObserver<GetCartResponse> observer = new TestObserver<>();
        setup.service.getCart(request, observer);

        assertTrue(observer.completed);
        assertNotNull(observer.value);
        assertEquals("cart-1", observer.value.getCart().getCartId());
        assertEquals("WINDOW", observer.value.getCart().getItems(0).getProductFamily());

        ArgumentCaptor<GetCartQuery> captor = ArgumentCaptor.forClass(GetCartQuery.class);
        verify(setup.getCartUseCase).getCart(captor.capture());
        assertEquals("cust-1", captor.getValue().customerId());
        assertEquals("sess-1", captor.getValue().sessionId());
    }

    @Test
    @DisplayName("TC-FUNC-CART-ADD-001: AddItem maps request and response")
    void addItemMapsRequestAndResponse() {
        TestSetup setup = new TestSetup();
        CartDto cartDto = sampleCartDto();
        when(setup.addItemUseCase.addItem(any())).thenReturn(new AddItemResult(cartDto, "item-1"));

        AddItemRequest request = AddItemRequest.newBuilder()
            .setCustomerId("cust-2")
            .setProductTemplateId("T-1")
            .setProductName("Window")
            .setProductFamily("WINDOW")
            .setThumbnailUrl("http://example.com/thumb.png")
            .setDimensions(Dimensions.newBuilder().setWidthCm(120).setHeightCm(130).build())
            .addSelectedOptions(SelectedOption.newBuilder()
                .setOptionGroupId("OPT-GROUP")
                .setOptionId("OPT-1")
                .build())
            .setQuantity(2)
            .setQuoteId("QUOTE-1")
            .setResolvedBom(BillOfMaterials.newBuilder()
                .addLines(BomLine.newBuilder()
                    .setSku("SKU-1")
                    .setDescription("Line 1")
                    .setQuantity(1)
                    .build())
                .build())
            .build();

        TestObserver<AddItemResponse> observer = new TestObserver<>();
        setup.service.addItem(request, observer);

        assertTrue(observer.completed);
        assertNotNull(observer.value);
        assertEquals("item-1", observer.value.getAddedItemId());
        assertEquals("WINDOW", observer.value.getCart().getItems(0).getProductFamily());

        ArgumentCaptor<AddItemCommand> captor = ArgumentCaptor.forClass(AddItemCommand.class);
        verify(setup.addItemUseCase).addItem(captor.capture());
        AddItemCommand command = captor.getValue();
        assertEquals("cust-2", command.customerId());
        assertEquals("T-1", command.productTemplateId());
        assertEquals("Window", command.productName());
        assertEquals("WINDOW", command.productFamily());
        assertEquals("QUOTE-1", command.quoteId());
    }

    @Test
    @DisplayName("TC-FUNC-CART-UPDATE-001: UpdateItem maps request and response")
    void updateItemMapsRequestAndResponse() {
        TestSetup setup = new TestSetup();
        when(setup.updateItemUseCase.updateItem(any())).thenReturn(sampleCartDto());

        UpdateItemRequest request = UpdateItemRequest.newBuilder()
            .setCustomerId("cust-3")
            .setItemId("item-1")
            .setQuantity(3)
            .build();

        TestObserver<UpdateItemResponse> observer = new TestObserver<>();
        setup.service.updateItem(request, observer);

        assertTrue(observer.completed);
        assertNotNull(observer.value);

        ArgumentCaptor<UpdateItemCommand> captor = ArgumentCaptor.forClass(UpdateItemCommand.class);
        verify(setup.updateItemUseCase).updateItem(captor.capture());
        assertEquals("item-1", captor.getValue().itemId());
        assertEquals(3, captor.getValue().quantity());
    }

    @Test
    @DisplayName("TC-FUNC-CART-REMOVE-001: RemoveItem maps request and response")
    void removeItemMapsRequestAndResponse() {
        TestSetup setup = new TestSetup();
        when(setup.removeItemUseCase.removeItem(any())).thenReturn(sampleCartDto());

        RemoveItemRequest request = RemoveItemRequest.newBuilder()
            .setCustomerId("cust-4")
            .setItemId("item-1")
            .build();

        TestObserver<RemoveItemResponse> observer = new TestObserver<>();
        setup.service.removeItem(request, observer);

        assertTrue(observer.completed);
        assertNotNull(observer.value);

        ArgumentCaptor<RemoveItemCommand> captor = ArgumentCaptor.forClass(RemoveItemCommand.class);
        verify(setup.removeItemUseCase).removeItem(captor.capture());
        assertEquals("item-1", captor.getValue().itemId());
    }

    @Test
    @DisplayName("TC-FUNC-CART-CLEAR-001: ClearCart maps request and response")
    void clearCartMapsRequestAndResponse() {
        TestSetup setup = new TestSetup();
        when(setup.clearCartUseCase.clearCart(any())).thenReturn(sampleCartDto());

        ClearCartRequest request = ClearCartRequest.newBuilder()
            .setCustomerId("cust-5")
            .build();

        TestObserver<ClearCartResponse> observer = new TestObserver<>();
        setup.service.clearCart(request, observer);

        assertTrue(observer.completed);
        assertNotNull(observer.value);

        ArgumentCaptor<ClearCartCommand> captor = ArgumentCaptor.forClass(ClearCartCommand.class);
        verify(setup.clearCartUseCase).clearCart(captor.capture());
        assertEquals("cust-5", captor.getValue().customerId());
    }

    @Test
    @DisplayName("TC-FUNC-CART-PROMO-001: ApplyPromoCode maps request and response")
    void applyPromoCodeMapsRequestAndResponse() {
        TestSetup setup = new TestSetup();
        when(setup.applyPromoCodeUseCase.applyPromoCode(any()))
            .thenReturn(new ApplyPromoCodeResult(sampleCartDto(), true, null, null));

        ApplyPromoCodeRequest request = ApplyPromoCodeRequest.newBuilder()
            .setCustomerId("cust-6")
            .setPromoCode("PROMO10")
            .build();

        TestObserver<ApplyPromoCodeResponse> observer = new TestObserver<>();
        setup.service.applyPromoCode(request, observer);

        assertTrue(observer.completed);
        assertNotNull(observer.value);
        assertTrue(observer.value.getApplied());

        ArgumentCaptor<ApplyPromoCodeCommand> captor = ArgumentCaptor.forClass(ApplyPromoCodeCommand.class);
        verify(setup.applyPromoCodeUseCase).applyPromoCode(captor.capture());
        assertEquals("PROMO10", captor.getValue().promoCode());
    }

    @Test
    @DisplayName("TC-FUNC-CART-PROMO-REMOVE-001: RemovePromoCode maps request and response")
    void removePromoCodeMapsRequestAndResponse() {
        TestSetup setup = new TestSetup();
        when(setup.removePromoCodeUseCase.removePromoCode(any())).thenReturn(sampleCartDto());

        RemovePromoCodeRequest request = RemovePromoCodeRequest.newBuilder()
            .setCustomerId("cust-7")
            .build();

        TestObserver<RemovePromoCodeResponse> observer = new TestObserver<>();
        setup.service.removePromoCode(request, observer);

        assertTrue(observer.completed);
        assertNotNull(observer.value);

        ArgumentCaptor<RemovePromoCodeCommand> captor = ArgumentCaptor.forClass(RemovePromoCodeCommand.class);
        verify(setup.removePromoCodeUseCase).removePromoCode(captor.capture());
        assertEquals("cust-7", captor.getValue().customerId());
    }

    @Test
    @DisplayName("TC-FUNC-CART-MERGE-001: MergeCarts maps request and response")
    void mergeCartsMapsRequestAndResponse() {
        TestSetup setup = new TestSetup();
        MergeCartsResult result = new MergeCartsResult(sampleCartDto(), 2, 1, 1, true, "AUTHENTICATED");
        when(setup.mergeCartsUseCase.mergeCarts(any())).thenReturn(result);

        MergeCartsRequest request = MergeCartsRequest.newBuilder()
            .setCustomerId("cust-8")
            .setAnonymousSessionId("sess-8")
            .build();

        TestObserver<MergeCartsResponse> observer = new TestObserver<>();
        setup.service.mergeCarts(request, observer);

        assertTrue(observer.completed);
        assertNotNull(observer.value);
        assertEquals(2, observer.value.getItemsFromAnonymous());
        assertEquals("AUTHENTICATED", observer.value.getPromoCodeSource());

        ArgumentCaptor<MergeCartsCommand> captor = ArgumentCaptor.forClass(MergeCartsCommand.class);
        verify(setup.mergeCartsUseCase).mergeCarts(captor.capture());
        assertEquals("cust-8", captor.getValue().customerId());
        assertEquals("sess-8", captor.getValue().anonymousSessionId());
    }

    @Test
    @DisplayName("TC-FUNC-CART-REFRESH-001: RefreshPrices maps request and response")
    void refreshPricesMapsRequestAndResponse() {
        TestSetup setup = new TestSetup();
        RefreshPricesResult result = new RefreshPricesResult(
            sampleCartDto(),
            1,
            true,
            money("1000.00"),
            5.5d
        );
        when(setup.refreshPricesUseCase.refreshPrices(any())).thenReturn(result);

        RefreshPricesRequest request = RefreshPricesRequest.newBuilder()
            .setCustomerId("cust-9")
            .build();

        TestObserver<RefreshPricesResponse> observer = new TestObserver<>();
        setup.service.refreshPrices(request, observer);

        assertTrue(observer.completed);
        assertNotNull(observer.value);
        assertEquals(1, observer.value.getItemsUpdated());
        assertTrue(observer.value.getTotalChanged());

        ArgumentCaptor<RefreshPricesCommand> captor = ArgumentCaptor.forClass(RefreshPricesCommand.class);
        verify(setup.refreshPricesUseCase).refreshPrices(captor.capture());
        assertEquals("cust-9", captor.getValue().customerId());
    }

    @Test
    @DisplayName("TC-FUNC-CART-SNAP-001: CreateSnapshot maps request and response")
    void createSnapshotMapsRequestAndResponse() {
        TestSetup setup = new TestSetup();
        CreateSnapshotResult result = new CreateSnapshotResult(
            "snap-1",
            sampleSnapshotDto(),
            Instant.parse("2026-01-01T10:15:00Z"),
            false,
            money("1000.00")
        );
        when(setup.createSnapshotUseCase.createSnapshot(any())).thenReturn(result);

        CreateSnapshotRequest request = CreateSnapshotRequest.newBuilder()
            .setCustomerId("cust-10")
            .setAcknowledgePriceChanges(true)
            .build();

        TestObserver<CreateSnapshotResponse> observer = new TestObserver<>();
        setup.service.createSnapshot(request, observer);

        assertTrue(observer.completed);
        assertNotNull(observer.value);
        assertEquals("snap-1", observer.value.getSnapshotId());
        assertEquals("cart-1", observer.value.getCartSnapshot().getCartId());

        ArgumentCaptor<CreateSnapshotCommand> captor = ArgumentCaptor.forClass(CreateSnapshotCommand.class);
        verify(setup.createSnapshotUseCase).createSnapshot(captor.capture());
        assertEquals("cust-10", captor.getValue().customerId());
        assertTrue(captor.getValue().acknowledgePriceChanges());
    }

    private static CartDto sampleCartDto() {
        return new CartDto(
            "cart-1",
            "cust-1",
            "sess-1",
            CartStatus.ACTIVE,
            List.of(sampleItemDto()),
            money("2000.00"),
            money("100.00"),
            money("0.00"),
            money("1900.00"),
            new AppliedPromoCodeDto("PROMO10", money("100.00"), "Promo", Instant.parse("2026-01-01T10:00:00Z")),
            2,
            Instant.parse("2026-01-01T10:00:00Z"),
            Instant.parse("2026-01-01T10:05:00Z")
        );
    }

    private static CartSnapshotDto sampleSnapshotDto() {
        return new CartSnapshotDto(
            "snap-1",
            "cart-1",
            "cust-1",
            List.of(sampleItemDto()),
            money("2000.00"),
            money("100.00"),
            money("0.00"),
            money("1900.00"),
            new AppliedPromoCodeDto("PROMO10", money("100.00"), "Promo", Instant.parse("2026-01-01T10:00:00Z")),
            2,
            Instant.parse("2026-01-01T10:00:00Z"),
            Instant.parse("2026-01-01T10:15:00Z")
        );
    }

    private static CartItemDto sampleItemDto() {
        return new CartItemDto(
            "item-1",
            "T-1",
            "Window",
            "WINDOW",
            new DimensionsDto(120, 130),
            List.of(new SelectedOptionDto("OPT-GROUP", "OPT-1")),
            List.of(new BomLineDto("SKU-1", "Line 1", 1)),
            2,
            money("1000.00"),
            money("2000.00"),
            "QUOTE-1",
            Instant.parse("2026-01-01T10:30:00Z"),
            ValidationStatus.VALID,
            null,
            false,
            "hash-1",
            "http://example.com/thumb.png"
        );
    }

    private static Money money(String amount) {
        return Money.of(new BigDecimal(amount), Currency.RUB);
    }

    private static class TestSetup {
        private final GetCartUseCase getCartUseCase = mock(GetCartUseCase.class);
        private final AddItemUseCase addItemUseCase = mock(AddItemUseCase.class);
        private final UpdateItemUseCase updateItemUseCase = mock(UpdateItemUseCase.class);
        private final RemoveItemUseCase removeItemUseCase = mock(RemoveItemUseCase.class);
        private final ClearCartUseCase clearCartUseCase = mock(ClearCartUseCase.class);
        private final RefreshPricesUseCase refreshPricesUseCase = mock(RefreshPricesUseCase.class);
        private final ApplyPromoCodeUseCase applyPromoCodeUseCase = mock(ApplyPromoCodeUseCase.class);
        private final RemovePromoCodeUseCase removePromoCodeUseCase = mock(RemovePromoCodeUseCase.class);
        private final MergeCartsUseCase mergeCartsUseCase = mock(MergeCartsUseCase.class);
        private final CreateSnapshotUseCase createSnapshotUseCase = mock(CreateSnapshotUseCase.class);
        private final CartGrpcMapper mapper = new CartGrpcMapper();
        private final CartGrpcService service = new CartGrpcService(
            getCartUseCase,
            addItemUseCase,
            updateItemUseCase,
            removeItemUseCase,
            clearCartUseCase,
            refreshPricesUseCase,
            applyPromoCodeUseCase,
            removePromoCodeUseCase,
            mergeCartsUseCase,
            createSnapshotUseCase,
            mapper
        );
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
