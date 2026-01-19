package com.kanokna.cart.application.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kanokna.cart.adapters.config.CartProperties;
import com.kanokna.cart.application.dto.AddItemCommand;
import com.kanokna.cart.application.dto.AddItemResult;
import com.kanokna.cart.application.dto.ApplyPromoCodeCommand;
import com.kanokna.cart.application.dto.ApplyPromoCodeResult;
import com.kanokna.cart.application.dto.BomLineDto;
import com.kanokna.cart.application.dto.CartDto;
import com.kanokna.cart.application.dto.ClearCartCommand;
import com.kanokna.cart.application.dto.CreateSnapshotCommand;
import com.kanokna.cart.application.dto.CreateSnapshotResult;
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
import com.kanokna.cart.application.port.out.CartRepository;
import com.kanokna.cart.application.port.out.CartSnapshotRepository;
import com.kanokna.cart.application.port.out.CatalogConfigurationPort;
import com.kanokna.cart.application.port.out.EventPublisher;
import com.kanokna.cart.application.port.out.SessionCartStore;
import com.kanokna.cart.application.service.dto.ApplyPromoResult;
import com.kanokna.cart.application.service.dto.MergeResult;
import com.kanokna.cart.application.service.dto.PriceRefreshResult;
import com.kanokna.cart.domain.event.CartCheckedOutEvent;
import com.kanokna.cart.domain.event.CartClearedEvent;
import com.kanokna.cart.domain.event.CartCreatedEvent;
import com.kanokna.cart.domain.event.CartItemAddedEvent;
import com.kanokna.cart.domain.event.CartItemRemovedEvent;
import com.kanokna.cart.domain.event.CartItemUpdatedEvent;
import com.kanokna.cart.domain.event.CartMergedEvent;
import com.kanokna.cart.domain.event.CartPricesRefreshedEvent;
import com.kanokna.cart.domain.event.PromoCodeAppliedEvent;
import com.kanokna.cart.domain.event.PromoCodeRemovedEvent;
import com.kanokna.cart.domain.exception.CartDomainErrors;
import com.kanokna.cart.domain.model.AppliedPromoCode;
import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.domain.model.CartId;
import com.kanokna.cart.domain.model.CartItem;
import com.kanokna.cart.domain.model.CartItemId;
import com.kanokna.cart.domain.model.CartSnapshot;
import com.kanokna.cart.domain.model.CartStatus;
import com.kanokna.cart.domain.model.CartTotals;
import com.kanokna.cart.domain.model.ConfigurationSnapshot;
import com.kanokna.cart.domain.model.PriceQuoteReference;
import com.kanokna.cart.domain.model.ValidationStatus;
import com.kanokna.cart.domain.service.CartMergeService;
import com.kanokna.cart.domain.service.CartTotalsCalculator;
import com.kanokna.cart.domain.service.ConfigurationHashService;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;

/**
 * MODULE_CONTRACT id="MC-cart-orchestrator"
 * LAYER="application.service"
 * INTENT="Orchestrate cart use cases by delegating to specialized sub-services"
 * LINKS="RequirementsAnalysis.xml#UC-CART-MANAGE;RequirementsAnalysis.xml#UC-CART-APPLY-PROMO;RequirementsAnalysis.xml#UC-ORDER-PLACE"
 *
 * Orchestrator service that coordinates cart operations.
 * Delegates specialized logic to sub-services while managing transactions and events.
 */
@Service
@Transactional
public class CartApplicationService implements
        GetCartUseCase, AddItemUseCase, UpdateItemUseCase, RemoveItemUseCase,
        ClearCartUseCase, RefreshPricesUseCase, ApplyPromoCodeUseCase,
        RemovePromoCodeUseCase, MergeCartsUseCase, CreateSnapshotUseCase {

    private static final Logger log = LoggerFactory.getLogger(CartApplicationService.class);
    private static final String SVC = "cart-service";

    // Repositories and ports
    private final CartRepository cartRepository;
    private final CartSnapshotRepository snapshotRepository;
    private final EventPublisher eventPublisher;
    private final SessionCartStore sessionCartStore;
    private final CatalogConfigurationPort catalogPort;

    // Sub-services
    private final CartItemValidationService validationService;
    private final CartPricingService pricingService;
    private final CartMergingService mergingService;
    private final CartCheckoutService checkoutService;
    private final CartPromoCodeService promoCodeService;

    // Domain services and config
    private final CartTotalsCalculator totalsCalculator;
    private final ConfigurationHashService hashService;
    private final CartProperties properties;

    public CartApplicationService(
            CartRepository cartRepository,
            CartSnapshotRepository snapshotRepository,
            EventPublisher eventPublisher,
            SessionCartStore sessionCartStore,
            CatalogConfigurationPort catalogPort,
            CartItemValidationService validationService,
            CartPricingService pricingService,
            CartMergingService mergingService,
            CartCheckoutService checkoutService,
            CartPromoCodeService promoCodeService,
            CartTotalsCalculator totalsCalculator,
            ConfigurationHashService hashService,
            CartProperties properties) {
        this.cartRepository = cartRepository;
        this.snapshotRepository = snapshotRepository;
        this.eventPublisher = eventPublisher;
        this.sessionCartStore = sessionCartStore;
        this.catalogPort = catalogPort;
        this.validationService = validationService;
        this.pricingService = pricingService;
        this.mergingService = mergingService;
        this.checkoutService = checkoutService;
        this.promoCodeService = promoCodeService;
        this.totalsCalculator = totalsCalculator;
        this.hashService = hashService;
        this.properties = properties;
    }

    @Override
    public CartDto getCart(GetCartQuery query) {
        String customerId = normalize(query.customerId());
        String sessionId = normalize(query.sessionId());
        requireCustomerOrSession(customerId, sessionId);

        log.atDebug().addKeyValue("svc", SVC).addKeyValue("uc", "UC-CART-MANAGE")
            .addKeyValue("customerId", customerId).addKeyValue("sessionId", sessionId)
            .log("Getting cart");

        Cart cart = findOrCreateCart(customerId, sessionId);
        var revalidation = validationService.revalidateCartItems(cart);

        return CartDtoMapper.toDto(cart, revalidation);
    }

    @Override
    public AddItemResult addItem(AddItemCommand cmd) {
        String customerId = normalize(cmd.customerId());
        String sessionId = normalize(cmd.sessionId());
        requireCustomerOrSession(customerId, sessionId);
        validateQuantity(cmd.quantity());
        requireProductName(cmd.productName());

        log.atInfo().addKeyValue("svc", SVC).addKeyValue("uc", "UC-CART-MANAGE")
            .addKeyValue("productTemplateId", cmd.productTemplateId())
            .addKeyValue("quantity", cmd.quantity())
            .log("Adding item to cart");

        // Resolve or create cart
        Cart cart = findOrCreateCart(customerId, sessionId);
        boolean isNewCart = cart.version() == 0 && cart.items().isEmpty();

        // Validate configuration
        ConfigurationSnapshot snapshot = buildSnapshot(cmd);
        var validation = validationService.validateConfiguration(snapshot);
        if (!validation.available()) {
            throw CartDomainErrors.catalogUnavailable("Catalog service unavailable");
        }
        if (!validation.valid()) {
            throw CartDomainErrors.invalidConfigurationWithErrors("Configuration validation failed", validation.errors());
        }

        // Get price quote
        var quote = pricingService.fetchQuote(snapshot, resolveCurrency(cart));
        if (!quote.available()) {
            throw CartDomainErrors.pricingUnavailable("Pricing service unavailable");
        }
        if (quote.expired()) {
            throw CartDomainErrors.quoteExpired(quote.quoteId());
        }

        // Create and add item
        String configHash = hashService.computeHash(snapshot.productTemplateId(),
            snapshot.widthCm(), snapshot.heightCm(), snapshot.selectedOptions());

        CartItem item = CartItem.create(snapshot.productTemplateId(), cmd.productName().strip(),
            normalizeFamily(cmd.productFamily()), snapshot, configHash, cmd.quantity(),
            quote.unitPrice(), new PriceQuoteReference(quote.quoteId(), quote.validUntil()),
            ValidationStatus.VALID, null, cmd.thumbnailUrl(), false, Instant.now());

        CartItem merged = cart.addItem(item, totalsCalculator, null);
        Cart saved = cartRepository.save(cart);

        // Publish events
        if (isNewCart) {
            eventPublisher.publish("cart.created", CartCreatedEvent.create(saved));
            if (hasText(sessionId) && !hasText(customerId)) {
                sessionCartStore.storeCartId(sessionId, saved.cartId().toString());
            }
        }
        eventPublisher.publish("cart.item.added", CartItemAddedEvent.create(saved, merged));

        log.atInfo().addKeyValue("svc", SVC).addKeyValue("cartId", saved.cartId())
            .addKeyValue("itemId", merged.itemId()).log("Item added");

        return new AddItemResult(CartDtoMapper.toDto(saved), merged.itemId().toString());
    }

    @Override
    public CartDto updateItem(UpdateItemCommand cmd) {
        Cart cart = resolveExistingCart(cmd.customerId(), cmd.sessionId());
        validateQuantity(cmd.quantity());

        CartItemId itemId = parseItemId(cmd.itemId());
        cart.findItem(itemId).orElseThrow(() -> CartDomainErrors.itemNotFound(cmd.itemId()));

        int oldQty = cart.findItem(itemId).map(CartItem::quantity).orElse(0);
        cart.updateItemQuantity(itemId, cmd.quantity(), totalsCalculator, null);

        Cart saved = cartRepository.save(cart);
        CartItem updated = saved.findItem(itemId).orElseThrow();
        eventPublisher.publish("cart.item.updated",
            CartItemUpdatedEvent.create(saved, updated, oldQty, updated.lineTotal()));

        return CartDtoMapper.toDto(saved);
    }

    @Override
    public CartDto removeItem(RemoveItemCommand cmd) {
        Cart cart = resolveExistingCart(cmd.customerId(), cmd.sessionId());
        CartItemId itemId = parseItemId(cmd.itemId());
        CartItem item = cart.findItem(itemId)
            .orElseThrow(() -> CartDomainErrors.itemNotFound(cmd.itemId()));

        cart.removeItem(itemId, totalsCalculator, null);
        Cart saved = cartRepository.save(cart);
        eventPublisher.publish("cart.item.removed", CartItemRemovedEvent.create(saved, item));

        return CartDtoMapper.toDto(saved);
    }

    @Override
    public CartDto clearCart(ClearCartCommand cmd) {
        Cart cart = resolveExistingCart(cmd.customerId(), cmd.sessionId());
        int itemsRemoved = cart.items().size();
        Money clearedSubtotal = cart.totals().subtotal();
        boolean hadPromo = cart.appliedPromoCode() != null;

        cart.clear(totalsCalculator);
        Cart saved = cartRepository.save(cart);
        eventPublisher.publish("cart.cleared",
            CartClearedEvent.create(saved, itemsRemoved, clearedSubtotal, hadPromo));

        return CartDtoMapper.toDto(saved);
    }

    @Override
    public RefreshPricesResult refreshPrices(RefreshPricesCommand cmd) {
        Cart cart = resolveExistingCart(cmd.customerId(), cmd.sessionId());
        if (cart.items().isEmpty()) {
            throw CartDomainErrors.emptyCart();
        }

        PriceRefreshResult result = pricingService.refreshAllPrices(cart);
        if (result.isTotalFailure()) {
            throw CartDomainErrors.pricingUnavailable("Pricing service unavailable");
        }
        if (result.isPartial()) {
            throw CartDomainErrors.pricingPartial("Some items failed to refresh");
        }

        Cart saved = cartRepository.save(cart);
        Money change = result.newTotal().subtract(result.previousTotal());
        eventPublisher.publish("cart.prices.refreshed",
            CartPricesRefreshedEvent.create(saved, result.itemsUpdated(), result.previousTotal(),
                result.newTotal(), change, result.changePercent(), "MANUAL"));

        return new RefreshPricesResult(CartDtoMapper.toDto(saved), result.itemsUpdated(),
            result.totalChanged(), result.previousTotal(), result.changePercent());
    }

    @Override
    public ApplyPromoCodeResult applyPromoCode(ApplyPromoCodeCommand cmd) {
        Cart cart = resolveExistingCart(cmd.customerId(), cmd.sessionId());
        if (cart.items().isEmpty()) {
            throw CartDomainErrors.emptyCart();
        }

        ApplyPromoResult result = promoCodeService.applyPromoCode(cart, cmd.promoCode());
        if (!result.success()) {
            return new ApplyPromoCodeResult(CartDtoMapper.toDto(cart), false,
                result.errorMessage(), result.errorCode());
        }

        Cart saved = cartRepository.save(cart);
        eventPublisher.publish("cart.promo.applied",
            PromoCodeAppliedEvent.create(saved, result.appliedPromoCode()));

        return new ApplyPromoCodeResult(CartDtoMapper.toDto(saved), true, null, null);
    }

    @Override
    public CartDto removePromoCode(RemovePromoCodeCommand cmd) {
        Cart cart = resolveExistingCart(cmd.customerId(), cmd.sessionId());
        AppliedPromoCode removed = promoCodeService.removePromoCode(cart);

        if (removed == null) {
            return CartDtoMapper.toDto(cart);
        }

        Cart saved = cartRepository.save(cart);
        eventPublisher.publish("cart.promo.removed",
            PromoCodeRemovedEvent.create(saved, removed.code(), removed.discountAmount()));

        return CartDtoMapper.toDto(saved);
    }

    @Override
    public MergeCartsResult mergeCarts(MergeCartsCommand cmd) {
        String customerId = normalize(cmd.customerId());
        String sessionId = normalize(cmd.anonymousSessionId());
        if (!hasText(customerId) || !hasText(sessionId)) {
            throw CartDomainErrors.missingRequiredParameters(
                "customerId", "anonymousSessionId");
        }

        Cart target = findOrCreateCart(customerId, null);
        Cart source = cartRepository.findBySessionId(sessionId)
            .orElseThrow(() -> CartDomainErrors.anonymousCartNotFound(sessionId));

        var mergeResult = mergingService.mergeCarts(source, target);
        PriceRefreshResult priceResult = pricingService.refreshAllPrices(target);
        if (priceResult.isTotalFailure()) {
            throw CartDomainErrors.pricingUnavailable("Pricing service unavailable");
        }

        source.clear(totalsCalculator);
        source.markMerged();
        cartRepository.save(source);
        Cart savedTarget = cartRepository.save(target);
        sessionCartStore.removeCartId(sessionId);

        eventPublisher.publish("cart.merged",
            CartMergedEvent.create(source, savedTarget,
                toEventMergeResult(mergeResult)));

        return new MergeCartsResult(CartDtoMapper.toDto(savedTarget), mergeResult.itemsFromSource(),
            mergeResult.itemsMerged(), mergeResult.itemsAdded(),
            mergeResult.promoCodePreserved(), mergeResult.promoCodeSource());
    }

    @Override
    public CreateSnapshotResult createSnapshot(CreateSnapshotCommand cmd) {
        String customerId = normalize(cmd.customerId());
        if (!hasText(customerId)) {
            throw CartDomainErrors.anonymousNotAllowed();
        }

        Cart cart = cartRepository.findByCustomerId(customerId)
            .orElseThrow(CartDomainErrors::emptyCart);
        if (cart.items().isEmpty()) {
            throw CartDomainErrors.emptyCart();
        }

        var result = checkoutService.createSnapshot(cart, cmd.acknowledgePriceChanges());
        if (!result.success()) {
            return handleSnapshotFailure(cart, result);
        }

        cart.markCheckedOut();
        CartSnapshot saved = snapshotRepository.save(result.snapshot());
        Cart savedCart = cartRepository.save(cart);
        eventPublisher.publish("cart.checkout", CartCheckedOutEvent.create(savedCart, saved));

        return new CreateSnapshotResult(saved.snapshotId().toString(),
            CartDtoMapper.toSnapshotDto(saved), saved.validUntil(),
            result.priceResult().totalChanged(), result.priceResult().previousTotal());
    }

    // === Helper methods ===

    private Cart findOrCreateCart(String customerId, String sessionId) {
        if (hasText(customerId)) {
            return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> createCart(customerId, null));
        }
        return cartRepository.findBySessionId(sessionId)
            .orElseGet(() -> Cart.createForSession(sessionId, defaultCurrency()));
    }

    private Cart resolveExistingCart(String customerId, String sessionId) {
        String cid = normalize(customerId);
        String sid = normalize(sessionId);
        requireCustomerOrSession(cid, sid);
        if (hasText(cid)) {
            return cartRepository.findByCustomerId(cid)
                .orElseThrow(() -> CartDomainErrors.cartNotFoundForCustomer(cid));
        }
        return cartRepository.findBySessionId(sid)
            .orElseThrow(() -> CartDomainErrors.cartNotFoundForSession(sid));
    }

    private Cart createCart(String customerId, String sessionId) {
        CartId cartId = hasText(customerId) ? stableCartId(customerId) : CartId.generate();
        return Cart.rehydrate(cartId, customerId, sessionId, CartStatus.ACTIVE, null,
            CartTotals.empty(defaultCurrency()), List.of(), Instant.now(), Instant.now(), 0);
    }

    private CartId stableCartId(String customerId) {
        try {
            return CartId.of(UUID.fromString(customerId));
        } catch (IllegalArgumentException e) {
            return CartId.of(UUID.nameUUIDFromBytes(customerId.getBytes(StandardCharsets.UTF_8)));
        }
    }

    private ConfigurationSnapshot buildSnapshot(AddItemCommand cmd) {
        if (!hasText(cmd.productTemplateId())) {
            throw CartDomainErrors.missingProductTemplateId();
        }
        if (cmd.dimensions() == null || cmd.dimensions().widthCm() == null) {
            throw CartDomainErrors.missingDimensions();
        }
        return new ConfigurationSnapshot(cmd.productTemplateId(),
            cmd.dimensions().widthCm(), cmd.dimensions().heightCm(),
            toOptionSnapshots(cmd.selectedOptions()), toBomSnapshots(cmd.resolvedBom()));
    }

    private List<ConfigurationSnapshot.SelectedOptionSnapshot> toOptionSnapshots(List<SelectedOptionDto> opts) {
        if (opts == null) return List.of();
        return opts.stream().map(o -> new ConfigurationSnapshot.SelectedOptionSnapshot(o.optionGroupId(), o.optionId())).toList();
    }

    private List<ConfigurationSnapshot.BomLineSnapshot> toBomSnapshots(List<BomLineDto> bom) {
        if (bom == null) return List.of();
        return bom.stream().map(b -> new ConfigurationSnapshot.BomLineSnapshot(b.sku(), b.description(), b.quantity())).toList();
    }

    private Currency resolveCurrency(Cart cart) {
        return cart.totals() != null && cart.totals().subtotal() != null
            ? cart.totals().subtotal().getCurrency() : defaultCurrency();
    }

    private CartItemId parseItemId(String itemId) {
        try {
            return CartItemId.of(itemId);
        } catch (RuntimeException e) {
            throw CartDomainErrors.itemNotFound(itemId);
        }
    }

    private void requireCustomerOrSession(String customerId, String sessionId) {
        if (!hasText(customerId) && !hasText(sessionId)) {
            throw CartDomainErrors.missingRequiredParameters("customerId", "sessionId");
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity < 1) throw CartDomainErrors.invalidQuantity(quantity);
        if (quantity > properties.limits().maxQuantityPerItem()) {
            throw CartDomainErrors.quantityOutOfRange(quantity, 1, properties.limits().maxQuantityPerItem());
        }
    }

    private void requireProductName(String name) {
        if (!hasText(name)) throw CartDomainErrors.missingProductName();
    }

    private String normalizeFamily(String family) {
        if (!hasText(family)) return null;
        String norm = family.strip().toUpperCase();
        if (!properties.behavior().allowedProductFamilies().contains(norm)) {
            throw CartDomainErrors.unsupportedProductFamily(family);
        }
        return norm;
    }

    private CartMergeService.MergeResult toEventMergeResult(MergeResult mergeResult) {
        return new CartMergeService.MergeResult(
            mergeResult.itemsFromSource(),
            mergeResult.itemsMerged(),
            mergeResult.itemsAdded(),
            mergeResult.promoCodePreserved(),
            mergeResult.promoCodeSource()
        );
    }

    private Currency defaultCurrency() {
        return properties.defaults().defaultCurrency();
    }

    private CreateSnapshotResult handleSnapshotFailure(Cart cart, CartCheckoutService.SnapshotCreationResult result) {
        switch (result.failureReason()) {
            case INVALID_ITEMS -> throw CartDomainErrors.invalidItems(result.validationResult().invalidItemCount());
            case PRICING_FAILED -> throw CartDomainErrors.pricingUnavailable("Pricing service unavailable");
            case REQUIRES_ACKNOWLEDGMENT -> throw CartDomainErrors.priceChangeRequiresAcknowledgment(result.priceResult().changePercent());
            default -> throw new IllegalStateException("Snapshot creation failed");
        }
    }

    private String normalize(String value) {
        if (value == null) return null;
        String trimmed = value.strip();
        return trimmed.isBlank() ? null : trimmed;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
