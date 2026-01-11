package com.kanokna.cart.application.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kanokna.cart.adapters.config.CartProperties;
import com.kanokna.cart.application.dto.AddItemCommand;
import com.kanokna.cart.application.dto.AddItemResult;
import com.kanokna.cart.application.dto.AppliedPromoCodeDto;
import com.kanokna.cart.application.dto.ApplyPromoCodeCommand;
import com.kanokna.cart.application.dto.ApplyPromoCodeResult;
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
import com.kanokna.cart.application.port.out.CartRepository;
import com.kanokna.cart.application.port.out.CartSnapshotRepository;
import com.kanokna.cart.application.port.out.CatalogConfigurationPort;
import com.kanokna.cart.application.port.out.EventPublisher;
import com.kanokna.cart.application.port.out.PricingPort;
import com.kanokna.cart.application.port.out.SessionCartStore;
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
import com.kanokna.cart.domain.model.CartSnapshotItem;
import com.kanokna.cart.domain.model.CartStatus;
import com.kanokna.cart.domain.model.CartTotals;
import com.kanokna.cart.domain.model.ConfigurationSnapshot;
import com.kanokna.cart.domain.model.PriceQuoteReference;
import com.kanokna.cart.domain.model.SnapshotId;
import com.kanokna.cart.domain.model.ValidationStatus;
import com.kanokna.cart.domain.service.CartMergeService;
import com.kanokna.cart.domain.service.CartTotalsCalculator;
import com.kanokna.cart.domain.service.ConfigurationHashService;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;

/**
 * Application service implementing cart use cases.
 */
@Service
@Transactional
public class CartApplicationService implements
    GetCartUseCase,
    AddItemUseCase,
    UpdateItemUseCase,
    RemoveItemUseCase,
    ClearCartUseCase,
    RefreshPricesUseCase,
    ApplyPromoCodeUseCase,
    RemovePromoCodeUseCase,
    MergeCartsUseCase,
    CreateSnapshotUseCase {

    private static final Logger log = LoggerFactory.getLogger(CartApplicationService.class);
    private static final String SERVICE = "cart-service";
    private static final String USE_CASE_MANAGE = "UC-CART-MANAGE";
    private static final String USE_CASE_PROMO = "UC-CART-APPLY-PROMO";
    private static final String USE_CASE_ORDER = "UC-ORDER-PLACE";
    private static final double PRICE_CHANGE_THRESHOLD_PERCENT = 1.0d;
    private static final String TOPIC_CART_CREATED = "cart.created";
    private static final String TOPIC_CART_ITEM_ADDED = "cart.item.added";
    private static final String TOPIC_CART_ITEM_UPDATED = "cart.item.updated";
    private static final String TOPIC_CART_ITEM_REMOVED = "cart.item.removed";
    private static final String TOPIC_CART_PROMO_APPLIED = "cart.promo.applied";
    private static final String TOPIC_CART_PROMO_REMOVED = "cart.promo.removed";
    private static final String TOPIC_CART_MERGED = "cart.merged";
    private static final String TOPIC_CART_CLEARED = "cart.cleared";
    private static final String TOPIC_CART_CHECKOUT = "cart.checkout";
    private static final String TOPIC_CART_PRICES_REFRESHED = "cart.prices.refreshed";

    private final CartRepository cartRepository;
    private final CartSnapshotRepository cartSnapshotRepository;
    private final CatalogConfigurationPort catalogConfigurationPort;
    private final PricingPort pricingPort;
    private final EventPublisher eventPublisher;
    private final SessionCartStore sessionCartStore;
    private final CartTotalsCalculator totalsCalculator;
    private final ConfigurationHashService configurationHashService;
    private final CartMergeService mergeService;
    private final CartProperties properties;

    public CartApplicationService(
        CartRepository cartRepository,
        CartSnapshotRepository cartSnapshotRepository,
        CatalogConfigurationPort catalogConfigurationPort,
        PricingPort pricingPort,
        EventPublisher eventPublisher,
        SessionCartStore sessionCartStore,
        CartTotalsCalculator totalsCalculator,
        ConfigurationHashService configurationHashService,
        CartMergeService mergeService,
        CartProperties properties
    ) {
        this.cartRepository = cartRepository;
        this.cartSnapshotRepository = cartSnapshotRepository;
        this.catalogConfigurationPort = catalogConfigurationPort;
        this.pricingPort = pricingPort;
        this.eventPublisher = eventPublisher;
        this.sessionCartStore = sessionCartStore;
        this.totalsCalculator = totalsCalculator;
        this.configurationHashService = configurationHashService;
        this.mergeService = mergeService;
        this.properties = properties;
    }

    /* <FUNCTION_CONTRACT
          id="FC-cart-getCart"
          LAYER="application.service"
          INTENT="Retrieve cart with current validation status and price staleness flags"
          INPUT="GetCartCommand (customerId or sessionId)"
          OUTPUT="CartResponse"
          SIDE_EFFECTS="Lazy revalidation of item configurations"
          LINKS="RequirementsAnalysis.xml#UC-CART-MANAGE">

          <PRECONDITIONS>
            <Item>customerId or sessionId is provided (not both empty)</Item>
            <Item>Caller is authorized (owns cart or is anonymous with valid session)</Item>
          </PRECONDITIONS>

          <POSTCONDITIONS>
            <Item>If cart exists, returned with items and totals</Item>
            <Item>If cart doesn't exist, empty cart returned (created on demand)</Item>
            <Item>Each item has validation_status populated</Item>
            <Item>Each item has price_stale flag populated</Item>
          </POSTCONDITIONS>

          <INVARIANTS>
            <Item>Read-only operation does not modify cart state</Item>
            <Item>Revalidation uses circuit breaker for catalog-service calls</Item>
          </INVARIANTS>

          <ERROR_HANDLING>
            <Item type="BUSINESS" code="ERR-CART-UNAUTHORIZED">Caller not authorized for cart</Item>
            <Item type="TECHNICAL" code="ERR-CART-CATALOG-UNAVAILABLE">Catalog service unavailable for
              revalidation</Item>
          </ERROR_HANDLING>

          <BLOCK_ANCHORS>
            <Item id="BA-CART-GET-01">Resolve cart ID from customerId or sessionId</Item>
            <Item id="BA-CART-GET-02">Load cart from repository</Item>
            <Item id="BA-CART-GET-03">Revalidate item configurations (lazy)</Item>
            <Item id="BA-CART-GET-04">Check price staleness for each item</Item>
            <Item id="BA-CART-GET-05">Return cart response</Item>
          </BLOCK_ANCHORS>

          <LOGGING>
            <Item>[SVC=cart-service][UC=UC-CART-MANAGE][BLOCK=BA-CART-GET-01][STATE=RESOLVE_CART]
              eventType=GET_CART_REQUEST keyValues=customerId,sessionId</Item>
            <Item>[SVC=cart-service][UC=UC-CART-MANAGE][BLOCK=BA-CART-GET-03][STATE=REVALIDATE]
              eventType=ITEM_REVALIDATION keyValues=itemCount,validCount,invalidCount</Item>
            <Item>[SVC=cart-service][UC=UC-CART-MANAGE][BLOCK=BA-CART-GET-05][STATE=COMPLETE]
              eventType=GET_CART_COMPLETE keyValues=cartId,itemCount,total</Item>
          </LOGGING>

          <TESTS>
            <Case id="TC-FUNC-CART-GET-001">Get existing cart returns all items with totals</Case>
            <Case id="TC-FUNC-CART-GET-002">Get non-existent cart returns empty cart</Case>
            <Case id="TC-FUNC-CART-GET-003">Items with stale prices marked appropriately</Case>
            <Case id="TC-FUNC-CART-GET-004">Invalid configurations flagged in response</Case>
            <Case id="TC-FUNC-CART-GET-005">Catalog service timeout uses last known validation status</Case>
          </TESTS>
        </FUNCTION_CONTRACT> */
    @Override
    public CartDto getCart(GetCartQuery query) {
        String customerId = normalize(query.customerId());
        String sessionId = normalize(query.sessionId());
        if (!hasText(customerId) && !hasText(sessionId)) {
            throw new IllegalArgumentException("customerId or sessionId must be provided");
        }

        // BA-CART-GET-01: Resolve cart ID from customerId or sessionId
        log.info(logLine(
            USE_CASE_MANAGE,
            "BA-CART-GET-01",
            "RESOLVE_CART",
            "GET_CART_REQUEST",
            "REQUEST",
            "customerId=" + customerId + ",sessionId=" + sessionId
        ));

        Optional<Cart> cartOptional = hasText(customerId)
            ? cartRepository.findByCustomerId(customerId)
            : cartRepository.findBySessionId(sessionId);

        // BA-CART-GET-02: Load cart from repository
        Cart cart = cartOptional.orElseGet(() -> hasText(customerId)
            ? Cart.createForCustomer(customerId, Currency.RUB)
            : Cart.createForSession(sessionId, Currency.RUB));

        List<CartItemDto> items = new ArrayList<>();
        int validCount = 0;
        int invalidCount = 0;
        Instant now = Instant.now();
        for (CartItem item : cart.items()) {
            ValidationStatus validationStatus = item.validationStatus();
            String validationMessage = item.validationMessage();
            CatalogConfigurationPort.ValidationResult validationResult;
            try {
                validationResult = catalogConfigurationPort.validateConfiguration(item.configurationSnapshot());
            } catch (Exception ex) {
                validationResult = CatalogConfigurationPort.ValidationResult.unavailable();
            }
            if (validationResult != null && validationResult.available()) {
                if (validationResult.valid()) {
                    validationStatus = ValidationStatus.VALID;
                    validationMessage = null;
                    validCount++;
                } else {
                    validationStatus = ValidationStatus.INVALID;
                    validationMessage = formatValidationMessage(validationResult.errors());
                    invalidCount++;
                }
            } else {
                validationStatus = ValidationStatus.UNKNOWN;
                validationMessage = "Catalog validation unavailable";
                invalidCount++;
            }

            // BA-CART-GET-04: Check price staleness for each item
            boolean priceStale = item.quoteReference().isStale(now);
            items.add(toDto(item, validationStatus, validationMessage, priceStale));
        }

        // BA-CART-GET-03: Revalidate item configurations (lazy)
        log.info(logLine(
            USE_CASE_MANAGE,
            "BA-CART-GET-03",
            "REVALIDATE",
            "ITEM_REVALIDATION",
            "COMPLETE",
            "itemCount=" + cart.items().size() + ",validCount=" + validCount + ",invalidCount=" + invalidCount
        ));

        // BA-CART-GET-05: Return cart response
        log.info(logLine(
            USE_CASE_MANAGE,
            "BA-CART-GET-05",
            "COMPLETE",
            "GET_CART_COMPLETE",
            "SUCCESS",
            "cartId=" + cart.cartId() + ",itemCount=" + cart.totals().itemCount()
                + ",total=" + cart.totals().total()
        ));

        return toDto(cart, items);
    }

    /* <FUNCTION_CONTRACT
          id="FC-cart-addItem"
          LAYER="application.service"
          INTENT="Add a configured item to cart with price from pricing-service"
          INPUT="AddItemCommand (customerId/sessionId, productTemplateId, productName, thumbnailUrl, productFamily, dimensions, options, quantity, quoteId, resolvedBom)"
          OUTPUT="CartResponse with added item"
          SIDE_EFFECTS="Creates cart if not exists; persists item; publishes CartItemAddedEvent"
          LINKS="RequirementsAnalysis.xml#UC-CART-MANAGE;DevelopmentPlan.xml#Flow-Config-Pricing">

          <PRECONDITIONS>
            <Item>Configuration is valid (must have been validated by catalog-configuration-service)</Item>
            <Item>quoteId is valid and not expired (from pricing-service)</Item>
            <Item>quantity >= 1</Item>
            <Item>productName is not empty</Item>
            <Item>productFamily is one of: WINDOW, DOOR, ACCESSORY</Item>
          </PRECONDITIONS>

          <POSTCONDITIONS>
            <Item>Item added to cart with configuration snapshot</Item>
            <Item>Cart totals recalculated</Item>
            <Item>CartItemAddedEvent published</Item>
            <Item>Cart updated_at timestamp updated</Item>
          </POSTCONDITIONS>

          <INVARIANTS>
            <Item>Only valid configurations can be added</Item>
            <Item>Item stores snapshot of configuration and price at add time</Item>
            <Item>configuration_hash computed for merge comparison</Item>
          </INVARIANTS>

          <ERROR_HANDLING>
            <Item type="BUSINESS" code="ERR-CART-INVALID-CONFIG">Configuration validation failed</Item>
            <Item type="BUSINESS" code="ERR-CART-QUOTE-EXPIRED">Quote has expired</Item>
            <Item type="BUSINESS" code="ERR-CART-INVALID-QUANTITY">Quantity less than 1</Item>
            <Item type="TECHNICAL" code="ERR-CART-CATALOG-UNAVAILABLE">Cannot validate configuration</Item>
            <Item type="TECHNICAL" code="ERR-CART-PRICING-UNAVAILABLE">Cannot refresh price</Item>
          </ERROR_HANDLING>

          <BLOCK_ANCHORS>
            <Item id="BA-CART-ADD-01">Resolve or create cart</Item>
            <Item id="BA-CART-ADD-02">Validate configuration via catalog-configuration-service</Item>
            <Item id="BA-CART-ADD-03">Get/verify price quote from pricing-service</Item>
            <Item id="BA-CART-ADD-04">Create CartItem with configuration snapshot</Item>
            <Item id="BA-CART-ADD-05">Compute configuration hash</Item>
            <Item id="BA-CART-ADD-06">Recalculate cart totals</Item>
            <Item id="BA-CART-ADD-07">Persist cart and publish event</Item>
          </BLOCK_ANCHORS>

          <LOGGING>
            <Item>[SVC=cart-service][UC=UC-CART-MANAGE][BLOCK=BA-CART-ADD-01][STATE=RESOLVE_CART]
              eventType=ADD_ITEM_REQUEST keyValues=customerId,productTemplateId,quantity</Item>
            <Item>[SVC=cart-service][UC=UC-CART-MANAGE][BLOCK=BA-CART-ADD-02][STATE=VALIDATE_CONFIG]
              eventType=CONFIG_VALIDATION decision=VALID|INVALID keyValues=productTemplateId</Item>
            <Item>[SVC=cart-service][UC=UC-CART-MANAGE][BLOCK=BA-CART-ADD-03][STATE=GET_PRICE]
              eventType=PRICE_QUOTE_CHECK keyValues=quoteId,valid</Item>
            <Item>[SVC=cart-service][UC=UC-CART-MANAGE][BLOCK=BA-CART-ADD-07][STATE=COMPLETE]
              eventType=ITEM_ADDED decision=SUCCESS keyValues=cartId,itemId,unitPrice,lineTotal</Item>
          </LOGGING>

          <TESTS>
            <Case id="TC-FUNC-CART-ADD-001">Add valid item creates cart item with snapshot</Case>
            <Case id="TC-FUNC-CART-ADD-002">Add item to existing cart appends item</Case>
            <Case id="TC-FUNC-CART-ADD-003">Invalid configuration rejected</Case>
            <Case id="TC-FUNC-CART-ADD-004">Expired quote triggers fresh price fetch</Case>
            <Case id="TC-FUNC-CART-ADD-005">Cart totals recalculated after add</Case>
            <Case id="TC-FUNC-CART-ADD-006">CartItemAddedEvent published to Kafka</Case>
          </TESTS>
        </FUNCTION_CONTRACT> */
    @Override
    public AddItemResult addItem(AddItemCommand command) {
        String customerId = normalize(command.customerId());
        String sessionId = normalize(command.sessionId());
        if (!hasText(customerId) && !hasText(sessionId)) {
            throw new IllegalArgumentException("customerId or sessionId must be provided");
        }
        // BA-CART-UPDATE-02: Validate quantity
        if (command.quantity() < 1) {
            throw CartDomainErrors.invalidQuantity(command.quantity());
        }
        String productName = requireProductName(command.productName());
        String productFamily = normalizeProductFamily(command.productFamily());

        // BA-CART-ADD-01: Resolve or create cart
        log.info(logLine(
            USE_CASE_MANAGE,
            "BA-CART-ADD-01",
            "RESOLVE_CART",
            "ADD_ITEM_REQUEST",
            "REQUEST",
            "customerId=" + customerId + ",productTemplateId=" + command.productTemplateId()
                + ",quantity=" + command.quantity()
        ));

        Cart cart;
        boolean created = false;
        if (hasText(customerId)) {
            cart = cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> createCustomerCart(customerId, Currency.RUB));
            created = cart.version() == 0 && cart.items().isEmpty();
        } else {
            cart = cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> Cart.createForSession(sessionId, Currency.RUB));
            created = cart.version() == 0 && cart.items().isEmpty();
        }

        ConfigurationSnapshot baseSnapshot = buildSnapshot(
            command.productTemplateId(),
            command.dimensions(),
            command.selectedOptions(),
            command.resolvedBom()
        );
        CatalogConfigurationPort.ValidationResult validationResult;
        try {
            validationResult = catalogConfigurationPort.validateConfiguration(baseSnapshot);
        } catch (Exception ex) {
            validationResult = CatalogConfigurationPort.ValidationResult.unavailable();
        }

        boolean configValid = validationResult != null && validationResult.available() && validationResult.valid();
        // BA-CART-ADD-02: Validate configuration via catalog-configuration-service
        log.info(logLine(
            USE_CASE_MANAGE,
            "BA-CART-ADD-02",
            "VALIDATE_CONFIG",
            "CONFIG_VALIDATION",
            configValid ? "VALID" : "INVALID",
            "productTemplateId=" + command.productTemplateId()
        ));
        if (validationResult == null || !validationResult.available()) {
            throw CartDomainErrors.catalogUnavailable("Catalog service unavailable", null);
        }
        if (!validationResult.valid()) {
            throw CartDomainErrors.invalidConfiguration("Configuration validation failed");
        }

        List<ConfigurationSnapshot.BomLineSnapshot> resolvedBom = validationResult.resolvedBom();
        if (resolvedBom == null || resolvedBom.isEmpty()) {
            resolvedBom = baseSnapshot.resolvedBom();
        }
        ConfigurationSnapshot snapshot = new ConfigurationSnapshot(
            baseSnapshot.productTemplateId(),
            baseSnapshot.widthCm(),
            baseSnapshot.heightCm(),
            baseSnapshot.selectedOptions(),
            resolvedBom
        );

        PricingPort.PriceQuote quote = pricingPort.calculateQuote(snapshot, resolveCurrency(cart));
        boolean quoteValid = quote != null && quote.available() && quote.quoteId() != null;
        // BA-CART-ADD-03: Get/verify price quote from pricing-service
        log.info(logLine(
            USE_CASE_MANAGE,
            "BA-CART-ADD-03",
            "GET_PRICE",
            "PRICE_QUOTE_CHECK",
            quoteValid ? "VALID" : "INVALID",
            "quoteId=" + (quote == null ? null : quote.quoteId()) + ",valid=" + quoteValid
        ));
        if (quote == null || !quote.available()) {
            throw CartDomainErrors.pricingUnavailable("Pricing service unavailable", null);
        }
        if (quote.quoteId() == null) {
            throw CartDomainErrors.pricingUnavailable("Pricing quote missing", null);
        }
        if (quote.validUntil() != null && quote.validUntil().isBefore(Instant.now())) {
            throw CartDomainErrors.quoteExpired(quote.quoteId());
        }

        // BA-CART-ADD-05: Compute configuration hash
        String configurationHash = configurationHashService.computeHash(
            baseSnapshot.productTemplateId(),
            baseSnapshot.widthCm(),
            baseSnapshot.heightCm(),
            baseSnapshot.selectedOptions()
        );

        // BA-CART-ADD-04: Create CartItem with configuration snapshot
        CartItem item = CartItem.create(
            baseSnapshot.productTemplateId(),
            productName,
            productFamily,
            snapshot,
            configurationHash,
            command.quantity(),
            quote.unitPrice(),
            new PriceQuoteReference(quote.quoteId(), quote.validUntil()),
            ValidationStatus.VALID,
            null,
            command.thumbnailUrl(),
            false,
            Instant.now()
        );

        // BA-CART-ADD-06: Recalculate cart totals
        CartItem mergedItem = cart.addItem(item, totalsCalculator, null);

        Cart saved = cartRepository.save(cart);
        if (created) {
            eventPublisher.publish(TOPIC_CART_CREATED, CartCreatedEvent.create(saved));
            if (!hasText(customerId) && hasText(sessionId)) {
                sessionCartStore.storeCartId(sessionId, saved.cartId().toString());
            }
        }
        eventPublisher.publish(TOPIC_CART_ITEM_ADDED, CartItemAddedEvent.create(saved, mergedItem));

        // BA-CART-ADD-07: Persist cart and publish event
        log.info(logLine(
            USE_CASE_MANAGE,
            "BA-CART-ADD-07",
            "COMPLETE",
            "ITEM_ADDED",
            "SUCCESS",
            "cartId=" + saved.cartId() + ",itemId=" + mergedItem.itemId()
                + ",unitPrice=" + mergedItem.unitPrice() + ",lineTotal=" + mergedItem.lineTotal()
        ));

        return new AddItemResult(toDto(saved), mergedItem.itemId().toString());
    }

    /* <FUNCTION_CONTRACT
          id="FC-cart-updateItem"
          LAYER="application.service"
          INTENT="Update cart item quantity and recalculate totals"
          INPUT="UpdateItemCommand (customerId/sessionId, itemId, newQuantity)"
          OUTPUT="CartResponse"
          SIDE_EFFECTS="Persists change; publishes CartItemUpdatedEvent"
          LINKS="RequirementsAnalysis.xml#UC-CART-MANAGE">

          <PRECONDITIONS>
            <Item>Item exists in cart</Item>
            <Item>newQuantity >= 1</Item>
          </PRECONDITIONS>

          <POSTCONDITIONS>
            <Item>Item quantity updated</Item>
            <Item>Line total = unit_price * newQuantity</Item>
            <Item>Cart totals recalculated</Item>
            <Item>CartItemUpdatedEvent published</Item>
          </POSTCONDITIONS>

          <ERROR_HANDLING>
            <Item type="BUSINESS" code="ERR-CART-ITEM-NOT-FOUND">Item not found in cart</Item>
            <Item type="BUSINESS" code="ERR-CART-INVALID-QUANTITY">Quantity less than 1</Item>
          </ERROR_HANDLING>

          <BLOCK_ANCHORS>
            <Item id="BA-CART-UPDATE-01">Load cart and find item</Item>
            <Item id="BA-CART-UPDATE-02">Validate quantity</Item>
            <Item id="BA-CART-UPDATE-03">Update quantity and line total</Item>
            <Item id="BA-CART-UPDATE-04">Recalculate cart totals</Item>
            <Item id="BA-CART-UPDATE-05">Persist and publish event</Item>
          </BLOCK_ANCHORS>

          <LOGGING>
            <Item>[SVC=cart-service][UC=UC-CART-MANAGE][BLOCK=BA-CART-UPDATE-01][STATE=LOAD_ITEM]
              eventType=UPDATE_ITEM_REQUEST keyValues=cartId,itemId,newQuantity</Item>
            <Item>[SVC=cart-service][UC=UC-CART-MANAGE][BLOCK=BA-CART-UPDATE-05][STATE=COMPLETE]
              eventType=ITEM_UPDATED decision=SUCCESS keyValues=oldQuantity,newQuantity,newLineTotal</Item>
          </LOGGING>

          <TESTS>
            <Case id="TC-FUNC-CART-UPDATE-001">Update quantity recalculates line total</Case>
            <Case id="TC-FUNC-CART-UPDATE-002">Cart totals recalculated after update</Case>
            <Case id="TC-FUNC-CART-UPDATE-003">Non-existent item returns error</Case>
            <Case id="TC-FUNC-CART-UPDATE-004">Quantity 0 rejected (use remove instead)</Case>
          </TESTS>
        </FUNCTION_CONTRACT> */
    @Override
    public CartDto updateItem(UpdateItemCommand command) {
        String customerId = normalize(command.customerId());
        String sessionId = normalize(command.sessionId());
        if (!hasText(customerId) && !hasText(sessionId)) {
            throw new IllegalArgumentException("customerId or sessionId must be provided");
        }
        if (command.quantity() < 1) {
            throw CartDomainErrors.invalidQuantity(command.quantity());
        }

        Cart cart = resolveExistingCart(customerId, sessionId);
        CartItemId itemId = CartItemId.of(command.itemId());
        CartItem item = cart.findItem(itemId)
            .orElseThrow(() -> CartDomainErrors.itemNotFound(command.itemId()));

        // BA-CART-UPDATE-01: Load cart and find item
        log.info(logLine(
            USE_CASE_MANAGE,
            "BA-CART-UPDATE-01",
            "LOAD_ITEM",
            "UPDATE_ITEM_REQUEST",
            "REQUEST",
            "cartId=" + cart.cartId() + ",itemId=" + itemId + ",newQuantity=" + command.quantity()
        ));

        int oldQuantity = item.quantity();
        Money oldLineTotal = item.lineTotal();

        // BA-CART-UPDATE-03: Update quantity and line total
        cart.updateItemQuantity(itemId, command.quantity(), totalsCalculator, null);

        // BA-CART-UPDATE-04: Recalculate cart totals
        Cart saved = cartRepository.save(cart);
        eventPublisher.publish(TOPIC_CART_ITEM_UPDATED, CartItemUpdatedEvent.create(saved, item, oldQuantity, oldLineTotal));

        // BA-CART-UPDATE-05: Persist and publish event
        log.info(logLine(
            USE_CASE_MANAGE,
            "BA-CART-UPDATE-05",
            "COMPLETE",
            "ITEM_UPDATED",
            "SUCCESS",
            "oldQuantity=" + oldQuantity + ",newQuantity=" + command.quantity()
                + ",newLineTotal=" + item.lineTotal()
        ));

        return toDto(saved);
    }

    /* <FUNCTION_CONTRACT
          id="FC-cart-removeItem"
          LAYER="application.service"
          INTENT="Remove item from cart and recalculate totals"
          INPUT="RemoveItemCommand (customerId/sessionId, itemId)"
          OUTPUT="CartResponse"
          SIDE_EFFECTS="Deletes item; publishes CartItemRemovedEvent"
          LINKS="RequirementsAnalysis.xml#UC-CART-MANAGE">

          <PRECONDITIONS>
            <Item>Cart exists</Item>
            <Item>Item exists in cart</Item>
          </PRECONDITIONS>

          <POSTCONDITIONS>
            <Item>Item removed from cart</Item>
            <Item>Cart totals recalculated</Item>
            <Item>CartItemRemovedEvent published</Item>
          </POSTCONDITIONS>

          <ERROR_HANDLING>
            <Item type="BUSINESS" code="ERR-CART-ITEM-NOT-FOUND">Item not found in cart</Item>
          </ERROR_HANDLING>

          <BLOCK_ANCHORS>
            <Item id="BA-CART-REMOVE-01">Load cart and find item</Item>
            <Item id="BA-CART-REMOVE-02">Remove item from cart</Item>
            <Item id="BA-CART-REMOVE-03">Recalculate cart totals</Item>
            <Item id="BA-CART-REMOVE-04">Persist and publish event</Item>
          </BLOCK_ANCHORS>

          <LOGGING>
            <Item>[SVC=cart-service][UC=UC-CART-MANAGE][BLOCK=BA-CART-REMOVE-01][STATE=LOAD_ITEM]
              eventType=REMOVE_ITEM_REQUEST keyValues=cartId,itemId</Item>
            <Item>[SVC=cart-service][UC=UC-CART-MANAGE][BLOCK=BA-CART-REMOVE-04][STATE=COMPLETE]
              eventType=ITEM_REMOVED decision=SUCCESS keyValues=cartId,itemId</Item>
          </LOGGING>

          <TESTS>
            <Case id="TC-FUNC-CART-REMOVE-001">Remove item recalculates totals</Case>
            <Case id="TC-FUNC-CART-REMOVE-002">Remove last item results in empty cart</Case>
            <Case id="TC-FUNC-CART-REMOVE-003">Non-existent item returns error</Case>
          </TESTS>
        </FUNCTION_CONTRACT> */
    @Override
    public CartDto removeItem(RemoveItemCommand command) {
        String customerId = normalize(command.customerId());
        String sessionId = normalize(command.sessionId());
        if (!hasText(customerId) && !hasText(sessionId)) {
            throw new IllegalArgumentException("customerId or sessionId must be provided");
        }

        Cart cart = resolveExistingCart(customerId, sessionId);
        CartItemId itemId = CartItemId.of(command.itemId());
        CartItem item = cart.findItem(itemId)
            .orElseThrow(() -> CartDomainErrors.itemNotFound(command.itemId()));

        // BA-CART-REMOVE-01: Load cart and find item
        log.info(logLine(
            USE_CASE_MANAGE,
            "BA-CART-REMOVE-01",
            "LOAD_ITEM",
            "REMOVE_ITEM_REQUEST",
            "REQUEST",
            "cartId=" + cart.cartId() + ",itemId=" + itemId
        ));

        // BA-CART-REMOVE-02: Remove item from cart
        cart.removeItem(itemId, totalsCalculator, null);

        // BA-CART-REMOVE-03: Recalculate cart totals
        Cart saved = cartRepository.save(cart);
        eventPublisher.publish(TOPIC_CART_ITEM_REMOVED, CartItemRemovedEvent.create(saved, item));

        // BA-CART-REMOVE-04: Persist and publish event
        log.info(logLine(
            USE_CASE_MANAGE,
            "BA-CART-REMOVE-04",
            "COMPLETE",
            "ITEM_REMOVED",
            "SUCCESS",
            "cartId=" + saved.cartId() + ",itemId=" + itemId
        ));

        return toDto(saved);
    }

    /* <FUNCTION_CONTRACT
          id="FC-cart-clearCart"
          LAYER="application.service"
          INTENT="Remove all items from the cart"
          INPUT="ClearCartCommand (customerId or sessionId)"
          OUTPUT="CartResponse (empty cart)"
          SIDE_EFFECTS="Deletes all items; removes promo code; publishes CartClearedEvent"
          LINKS="RequirementsAnalysis.xml#UC-CART-MANAGE">

          <PRECONDITIONS>
            <Item>customerId or sessionId is provided</Item>
            <Item>Caller is authorized (owns cart or is anonymous with valid session)</Item>
          </PRECONDITIONS>

          <POSTCONDITIONS>
            <Item>All items removed from cart</Item>
            <Item>Applied promo code removed</Item>
            <Item>Cart totals reset to zero</Item>
            <Item>Cart status remains ACTIVE</Item>
            <Item>CartClearedEvent published</Item>
          </POSTCONDITIONS>

          <INVARIANTS>
            <Item>Cart entity is preserved (not deleted), only emptied</Item>
            <Item>Cart updated_at timestamp updated</Item>
          </INVARIANTS>

          <ERROR_HANDLING>
            <Item type="BUSINESS" code="ERR-CART-NOT-FOUND">Cart not found</Item>
            <Item type="BUSINESS" code="ERR-CART-UNAUTHORIZED">Caller not authorized for cart</Item>
          </ERROR_HANDLING>

          <BLOCK_ANCHORS>
            <Item id="BA-CART-CLEAR-01">Resolve cart from customerId or sessionId</Item>
            <Item id="BA-CART-CLEAR-02">Validate cart exists and caller authorized</Item>
            <Item id="BA-CART-CLEAR-03">Remove all items from cart</Item>
            <Item id="BA-CART-CLEAR-04">Remove applied promo code</Item>
            <Item id="BA-CART-CLEAR-05">Reset cart totals to zero</Item>
            <Item id="BA-CART-CLEAR-06">Persist and publish CartClearedEvent</Item>
          </BLOCK_ANCHORS>

          <LOGGING>
            <Item>[SVC=cart-service][UC=UC-CART-MANAGE][BLOCK=BA-CART-CLEAR-01][STATE=RESOLVE_CART]
              eventType=CLEAR_CART_REQUEST keyValues=customerId,sessionId</Item>
            <Item>[SVC=cart-service][UC=UC-CART-MANAGE][BLOCK=BA-CART-CLEAR-03][STATE=CLEAR_ITEMS]
              eventType=ITEMS_CLEARED keyValues=cartId,itemsRemoved</Item>
            <Item>[SVC=cart-service][UC=UC-CART-MANAGE][BLOCK=BA-CART-CLEAR-06][STATE=COMPLETE]
              eventType=CART_CLEARED decision=SUCCESS keyValues=cartId</Item>
          </LOGGING>

          <TESTS>
            <Case id="TC-FUNC-CART-CLEAR-001">Clear cart removes all items</Case>
            <Case id="TC-FUNC-CART-CLEAR-002">Clear cart removes applied promo code</Case>
            <Case id="TC-FUNC-CART-CLEAR-003">Clear cart resets totals to zero</Case>
            <Case id="TC-FUNC-CART-CLEAR-004">Clear empty cart is idempotent (no error)</Case>
            <Case id="TC-FUNC-CART-CLEAR-005">CartClearedEvent published with item count</Case>
          </TESTS>
        </FUNCTION_CONTRACT> */
    @Override
    public CartDto clearCart(ClearCartCommand command) {
        String customerId = normalize(command.customerId());
        String sessionId = normalize(command.sessionId());
        if (!hasText(customerId) && !hasText(sessionId)) {
            throw new IllegalArgumentException("customerId or sessionId must be provided");
        }

        // BA-CART-CLEAR-02: Validate cart exists and caller authorized
        Cart cart = resolveExistingCart(customerId, sessionId);

        // BA-CART-CLEAR-01: Resolve cart from customerId or sessionId
        log.info(logLine(
            USE_CASE_MANAGE,
            "BA-CART-CLEAR-01",
            "RESOLVE_CART",
            "CLEAR_CART_REQUEST",
            "REQUEST",
            "customerId=" + customerId + ",sessionId=" + sessionId
        ));

        int itemsRemoved = cart.items().size();
        Money clearedSubtotal = cart.totals().subtotal();
        boolean promoRemoved = cart.appliedPromoCode() != null;

        // BA-CART-CLEAR-03: Remove all items from cart
        // BA-CART-CLEAR-04: Remove applied promo code
        // BA-CART-CLEAR-05: Reset cart totals to zero
        cart.clear(totalsCalculator);

        // BA-CART-CLEAR-03: Remove all items from cart
        log.info(logLine(
            USE_CASE_MANAGE,
            "BA-CART-CLEAR-03",
            "CLEAR_ITEMS",
            "ITEMS_CLEARED",
            "CLEARED",
            "cartId=" + cart.cartId() + ",itemsRemoved=" + itemsRemoved
        ));

        Cart saved = cartRepository.save(cart);
        eventPublisher.publish(
            TOPIC_CART_CLEARED,
            CartClearedEvent.create(saved, itemsRemoved, clearedSubtotal, promoRemoved)
        );

        // BA-CART-CLEAR-06: Persist and publish CartClearedEvent
        log.info(logLine(
            USE_CASE_MANAGE,
            "BA-CART-CLEAR-06",
            "COMPLETE",
            "CART_CLEARED",
            "SUCCESS",
            "cartId=" + saved.cartId()
        ));

        return toDto(saved);
    }

    /* <FUNCTION_CONTRACT
          id="FC-cart-refreshPrices"
          LAYER="application.service"
          INTENT="Refresh price quotes for all cart items from pricing-service"
          INPUT="RefreshPricesCommand (customerId or sessionId)"
          OUTPUT="RefreshPricesResponse (cart with updated prices, change indicators)"
          SIDE_EFFECTS="Updates item prices; recalculates totals; publishes CartPricesRefreshedEvent"
          LINKS="RequirementsAnalysis.xml#UC-CART-MANAGE;Technology.xml#DEC-CART-PRICE-LOCK;DevelopmentPlan.xml#DP-SVC-pricing-service">

          <PRECONDITIONS>
            <Item>customerId or sessionId is provided</Item>
            <Item>Cart exists and has at least one item</Item>
          </PRECONDITIONS>

          <POSTCONDITIONS>
            <Item>All item prices refreshed from pricing-service.CalculateQuote</Item>
            <Item>quote_id and quote_valid_until updated per item</Item>
            <Item>price_stale flag set to false for all items</Item>
            <Item>Cart totals recalculated</Item>
            <Item>If promo code applied, discount recalculated with new subtotal</Item>
            <Item>CartPricesRefreshedEvent published</Item>
          </POSTCONDITIONS>

          <INVARIANTS>
            <Item>Item configurations unchanged (only prices updated)</Item>
            <Item>Promo code remains applied (discount amount may change)</Item>
            <Item>Circuit breaker applied to pricing-service calls</Item>
          </INVARIANTS>

          <ERROR_HANDLING>
            <Item type="BUSINESS" code="ERR-CART-EMPTY">Cannot refresh prices for empty cart</Item>
            <Item type="BUSINESS" code="ERR-CART-NOT-FOUND">Cart not found</Item>
            <Item type="TECHNICAL" code="ERR-CART-PRICING-UNAVAILABLE">Pricing service unavailable</Item>
            <Item type="TECHNICAL" code="ERR-CART-PRICING-PARTIAL">Some items failed to refresh (partial
              success)</Item>
          </ERROR_HANDLING>

          <BLOCK_ANCHORS>
            <Item id="BA-CART-REFRESH-01">Load cart and validate not empty</Item>
            <Item id="BA-CART-REFRESH-02">Iterate items and call pricing-service.CalculateQuote for each</Item>
            <Item id="BA-CART-REFRESH-03">Update item prices, quote_id, quote_valid_until</Item>
            <Item id="BA-CART-REFRESH-04">Clear price_stale flags</Item>
            <Item id="BA-CART-REFRESH-05">Recalculate cart totals</Item>
            <Item id="BA-CART-REFRESH-06">Recalculate promo discount if applied</Item>
            <Item id="BA-CART-REFRESH-07">Persist and publish CartPricesRefreshedEvent</Item>
          </BLOCK_ANCHORS>

          <LOGGING>
            <Item>[SVC=cart-service][UC=UC-CART-MANAGE][BLOCK=BA-CART-REFRESH-01][STATE=LOAD_CART]
              eventType=REFRESH_PRICES_REQUEST keyValues=customerId,sessionId,itemCount</Item>
            <Item>[SVC=cart-service][UC=UC-CART-MANAGE][BLOCK=BA-CART-REFRESH-02][STATE=FETCH_QUOTES]
              eventType=PRICING_CALLS keyValues=itemCount,successCount,failCount</Item>
            <Item>[SVC=cart-service][UC=UC-CART-MANAGE][BLOCK=BA-CART-REFRESH-05][STATE=RECALCULATE]
              eventType=TOTALS_RECALCULATED keyValues=previousTotal,newTotal,changePercent</Item>
            <Item>[SVC=cart-service][UC=UC-CART-MANAGE][BLOCK=BA-CART-REFRESH-07][STATE=COMPLETE]
              eventType=PRICES_REFRESHED decision=SUCCESS keyValues=itemsUpdated,totalChanged</Item>
          </LOGGING>

          <TESTS>
            <Case id="TC-FUNC-CART-REFRESH-001">Refresh updates all item prices from pricing-service</Case>
            <Case id="TC-FUNC-CART-REFRESH-002">Refresh clears price_stale flags</Case>
            <Case id="TC-FUNC-CART-REFRESH-003">Refresh recalculates cart totals</Case>
            <Case id="TC-FUNC-CART-REFRESH-004">Promo discount recalculated with new subtotal</Case>
            <Case id="TC-FUNC-CART-REFRESH-005">Pricing service timeout uses circuit breaker fallback</Case>
            <Case id="TC-FUNC-CART-REFRESH-006">Empty cart returns error</Case>
            <Case id="TC-FUNC-CART-REFRESH-007">Response includes total change indicators</Case>
          </TESTS>
        </FUNCTION_CONTRACT> */
    @Override
    public RefreshPricesResult refreshPrices(RefreshPricesCommand command) {
        String customerId = normalize(command.customerId());
        String sessionId = normalize(command.sessionId());
        if (!hasText(customerId) && !hasText(sessionId)) {
            throw new IllegalArgumentException("customerId or sessionId must be provided");
        }

        Cart cart = resolveExistingCart(customerId, sessionId);
        if (cart.items().isEmpty()) {
            throw CartDomainErrors.emptyCart();
        }

        // BA-CART-REFRESH-01: Load cart and validate not empty
        log.info(logLine(
            USE_CASE_MANAGE,
            "BA-CART-REFRESH-01",
            "LOAD_CART",
            "REFRESH_PRICES_REQUEST",
            "REQUEST",
            "customerId=" + customerId + ",sessionId=" + sessionId + ",itemCount=" + cart.items().size()
        ));

        RefreshOutcome outcome = refreshPricesInternal(cart);

        // BA-CART-REFRESH-02: Iterate items and call pricing-service.CalculateQuote for each
        log.info(logLine(
            USE_CASE_MANAGE,
            "BA-CART-REFRESH-02",
            "FETCH_QUOTES",
            "PRICING_CALLS",
            "COMPLETE",
            "itemCount=" + cart.items().size()
                + ",successCount=" + outcome.successCount()
                + ",failCount=" + outcome.failCount()
        ));

        if (outcome.failCount() > 0) {
            if (outcome.successCount() == 0) {
                throw CartDomainErrors.pricingUnavailable("Pricing service unavailable", null);
            }
            throw CartDomainErrors.pricingPartial("Some cart items failed to refresh");
        }

        // BA-CART-REFRESH-05: Recalculate cart totals
        log.info(logLine(
            USE_CASE_MANAGE,
            "BA-CART-REFRESH-05",
            "RECALCULATE",
            "TOTALS_RECALCULATED",
            "COMPLETE",
            "previousTotal=" + outcome.previousTotal()
                + ",newTotal=" + outcome.newTotal()
                + ",changePercent=" + outcome.changePercent()
        ));

        Cart saved = cartRepository.save(cart);
        Money totalChange = outcome.newTotal().subtract(outcome.previousTotal());
        eventPublisher.publish(
            TOPIC_CART_PRICES_REFRESHED,
            CartPricesRefreshedEvent.create(
                saved,
                outcome.itemsUpdated(),
                outcome.previousTotal(),
                outcome.newTotal(),
                totalChange,
                outcome.changePercent(),
                "MANUAL"
            )
        );

        // BA-CART-REFRESH-07: Persist and publish CartPricesRefreshedEvent
        log.info(logLine(
            USE_CASE_MANAGE,
            "BA-CART-REFRESH-07",
            "COMPLETE",
            "PRICES_REFRESHED",
            "SUCCESS",
            "itemsUpdated=" + outcome.itemsUpdated() + ",totalChanged=" + outcome.totalChanged()
        ));

        return new RefreshPricesResult(
            toDto(saved),
            outcome.itemsUpdated(),
            outcome.totalChanged(),
            outcome.previousTotal(),
            outcome.changePercent()
        );
    }

    /* <FUNCTION_CONTRACT
          id="FC-cart-applyPromoCode"
          LAYER="application.service"
          INTENT="Validate and apply promo code to cart"
          INPUT="ApplyPromoCodeCommand (customerId/sessionId, promoCode)"
          OUTPUT="ApplyPromoCodeResponse"
          SIDE_EFFECTS="Updates cart with discount if valid; publishes PromoCodeAppliedEvent"
          LINKS="RequirementsAnalysis.xml#UC-CART-APPLY-PROMO;DevelopmentPlan.xml#Flow-Cart-PromoCode">

          <PRECONDITIONS>
            <Item>Cart exists and has at least one item</Item>
            <Item>promoCode is not empty</Item>
          </PRECONDITIONS>

          <POSTCONDITIONS>
            <Item>If valid: promo code applied and discount applied to cart totals</Item>
            <Item>If invalid: error returned with reason</Item>
            <Item>PromoCodeAppliedEvent published on success</Item>
          </POSTCONDITIONS>

          <INVARIANTS>
            <Item>Only one promo code can be active at a time</Item>
            <Item>Discount cannot exceed subtotal</Item>
          </INVARIANTS>

          <ERROR_HANDLING>
            <Item type="BUSINESS" code="ERR-CART-PROMO-INVALID">Promo code is invalid or expired</Item>
            <Item type="BUSINESS" code="ERR-CART-PROMO-MIN-NOT-MET">Minimum order amount not met</Item>
            <Item type="BUSINESS" code="ERR-CART-EMPTY">Cannot apply promo to empty cart</Item>
            <Item type="TECHNICAL" code="ERR-CART-PRICING-UNAVAILABLE">Pricing service unavailable</Item>
          </ERROR_HANDLING>

          <BLOCK_ANCHORS>
            <Item id="BA-CART-PROMO-01">Load cart and validate not empty</Item>
            <Item id="BA-CART-PROMO-02">Call pricing-service.ValidatePromoCode</Item>
            <Item id="BA-CART-PROMO-03">Apply discount to cart</Item>
            <Item id="BA-CART-PROMO-04">Recalculate totals</Item>
            <Item id="BA-CART-PROMO-05">Persist and publish event</Item>
          </BLOCK_ANCHORS>

          <LOGGING>
            <Item>[SVC=cart-service][UC=UC-CART-APPLY-PROMO][BLOCK=BA-CART-PROMO-01][STATE=LOAD_CART]
              eventType=APPLY_PROMO_REQUEST keyValues=cartId,promoCode</Item>
            <Item>[SVC=cart-service][UC=UC-CART-APPLY-PROMO][BLOCK=BA-CART-PROMO-02][STATE=VALIDATE_PROMO]
              eventType=PROMO_VALIDATION decision=VALID|INVALID keyValues=promoCode,discountAmount</Item>
            <Item>[SVC=cart-service][UC=UC-CART-APPLY-PROMO][BLOCK=BA-CART-PROMO-05][STATE=COMPLETE]
              eventType=PROMO_APPLIED decision=SUCCESS keyValues=promoCode,discount,newTotal</Item>
          </LOGGING>

          <TESTS>
            <Case id="TC-FUNC-CART-PROMO-001">Valid promo code applies discount</Case>
            <Case id="TC-FUNC-CART-PROMO-002">Invalid promo code returns error with reason</Case>
            <Case id="TC-FUNC-CART-PROMO-003">Promo code replaces existing promo</Case>
            <Case id="TC-FUNC-CART-PROMO-004">Empty cart rejects promo application</Case>
            <Case id="TC-FUNC-CART-PROMO-005">Minimum order requirement checked</Case>
          </TESTS>
        </FUNCTION_CONTRACT> */
    @Override
    public ApplyPromoCodeResult applyPromoCode(ApplyPromoCodeCommand command) {
        String customerId = normalize(command.customerId());
        String sessionId = normalize(command.sessionId());
        if (!hasText(customerId) && !hasText(sessionId)) {
            throw new IllegalArgumentException("customerId or sessionId must be provided");
        }

        Cart cart = resolveExistingCart(customerId, sessionId);
        if (cart.items().isEmpty()) {
            throw CartDomainErrors.emptyCart();
        }

        // BA-CART-PROMO-01: Load cart and validate not empty
        log.info(logLine(
            USE_CASE_PROMO,
            "BA-CART-PROMO-01",
            "LOAD_CART",
            "APPLY_PROMO_REQUEST",
            "REQUEST",
            "cartId=" + cart.cartId() + ",promoCode=" + command.promoCode()
        ));

        PricingPort.PromoValidationResult validationResult = pricingPort.validatePromoCode(
            command.promoCode(),
            cart.totals().subtotal()
        );
        if (validationResult == null || !validationResult.available()) {
            throw CartDomainErrors.pricingUnavailable("Pricing service unavailable", null);
        }

        boolean valid = validationResult.valid();
        Money discount = validationResult.discountAmount();
        if (discount == null) {
            discount = Money.zero(cart.totals().subtotal().getCurrency());
        }

        // BA-CART-PROMO-02: Call pricing-service.ValidatePromoCode
        log.info(logLine(
            USE_CASE_PROMO,
            "BA-CART-PROMO-02",
            "VALIDATE_PROMO",
            "PROMO_VALIDATION",
            valid ? "VALID" : "INVALID",
            "promoCode=" + command.promoCode() + ",discountAmount=" + discount
        ));

        if (!valid) {
            String errorCode = mapPromoErrorCode(validationResult.errorCode());
            String errorMessage = validationResult.errorMessage() != null
                ? validationResult.errorMessage()
                : "Promo code invalid";
            return new ApplyPromoCodeResult(toDto(cart), false, errorMessage, errorCode);
        }

        AppliedPromoCode promoCode = new AppliedPromoCode(
            command.promoCode(),
            discount,
            validationResult.errorMessage(),
            Instant.now()
        );

        // BA-CART-PROMO-03: Apply discount to cart
        // BA-CART-PROMO-04: Recalculate totals
        cart.applyPromoCode(promoCode, totalsCalculator, null);
        Cart saved = cartRepository.save(cart);
        eventPublisher.publish(TOPIC_CART_PROMO_APPLIED, PromoCodeAppliedEvent.create(saved, promoCode));

        // BA-CART-PROMO-05: Persist and publish event
        log.info(logLine(
            USE_CASE_PROMO,
            "BA-CART-PROMO-05",
            "COMPLETE",
            "PROMO_APPLIED",
            "SUCCESS",
            "promoCode=" + promoCode.code() + ",discount=" + promoCode.discountAmount()
                + ",newTotal=" + saved.totals().total()
        ));

        return new ApplyPromoCodeResult(toDto(saved), true, null, null);
    }

    /* <FUNCTION_CONTRACT
          id="FC-cart-removePromoCode"
          LAYER="application.service"
          INTENT="Remove applied promotional code from cart"
          INPUT="RemovePromoCodeCommand (customerId/sessionId)"
          OUTPUT="CartResponse"
          SIDE_EFFECTS="Updates cart; publishes PromoCodeRemovedEvent"
          LINKS="RequirementsAnalysis.xml#UC-CART-APPLY-PROMO">

          <PRECONDITIONS>
            <Item>Cart exists</Item>
            <Item>Promo code is currently applied</Item>
          </PRECONDITIONS>

          <POSTCONDITIONS>
            <Item>Promo code removed</Item>
            <Item>Discount set to zero</Item>
            <Item>Totals recalculated</Item>
            <Item>PromoCodeRemovedEvent published</Item>
          </POSTCONDITIONS>

          <ERROR_HANDLING>
            <Item type="BUSINESS" code="ERR-CART-NO-PROMO">No promo code applied to remove</Item>
          </ERROR_HANDLING>

          <BLOCK_ANCHORS>
            <Item id="BA-CART-PROMO-REMOVE-01">Load cart</Item>
            <Item id="BA-CART-PROMO-REMOVE-02">Remove promo code</Item>
            <Item id="BA-CART-PROMO-REMOVE-03">Recalculate totals</Item>
            <Item id="BA-CART-PROMO-REMOVE-04">Persist and publish event</Item>
          </BLOCK_ANCHORS>

          <TESTS>
            <Case id="TC-FUNC-CART-PROMO-REMOVE-001">Remove promo code zeroes discount</Case>
            <Case id="TC-FUNC-CART-PROMO-REMOVE-002">No promo applied returns appropriate response</Case>
          </TESTS>
        </FUNCTION_CONTRACT> */
    @Override
    public CartDto removePromoCode(RemovePromoCodeCommand command) {
        String customerId = normalize(command.customerId());
        String sessionId = normalize(command.sessionId());
        if (!hasText(customerId) && !hasText(sessionId)) {
            throw new IllegalArgumentException("customerId or sessionId must be provided");
        }

        // BA-CART-PROMO-REMOVE-01: Load cart
        Cart cart = resolveExistingCart(customerId, sessionId);
        AppliedPromoCode promoCode = cart.appliedPromoCode();
        if (promoCode == null) {
            return toDto(cart);
        }

        Money removedDiscount = promoCode.discountAmount();
        // BA-CART-PROMO-REMOVE-02: Remove promo code
        // BA-CART-PROMO-REMOVE-03: Recalculate totals
        cart.removePromoCode(totalsCalculator, null);

        // BA-CART-PROMO-REMOVE-04: Persist and publish event
        Cart saved = cartRepository.save(cart);
        eventPublisher.publish(
            TOPIC_CART_PROMO_REMOVED,
            PromoCodeRemovedEvent.create(saved, promoCode.code(), removedDiscount)
        );

        return toDto(saved);
    }

    /* <FUNCTION_CONTRACT
          id="FC-cart-mergeCarts"
          LAYER="application.service"
          INTENT="Merge anonymous cart into authenticated cart on login"
          INPUT="MergeCartsCommand (customerId, anonymousSessionId)"
          OUTPUT="MergeCartsResponse"
          SIDE_EFFECTS="Merges items; deletes anonymous cart; publishes CartMergedEvent"
          LINKS="RequirementsAnalysis.xml#UC-CART-MANAGE;Technology.xml#DEC-CART-MERGE">

          <PRECONDITIONS>
            <Item>customerId identifies authenticated cart</Item>
            <Item>anonymousSessionId identifies anonymous cart</Item>
            <Item>At least one of the carts has items</Item>
          </PRECONDITIONS>

          <POSTCONDITIONS>
            <Item>Items from anonymous cart merged into authenticated cart</Item>
            <Item>Same configuration items have quantities summed</Item>
            <Item>All prices refreshed after merge</Item>
            <Item>Anonymous cart deleted</Item>
            <Item>CartMergedEvent published</Item>
          </POSTCONDITIONS>

          <INVARIANTS>
            <Item>Merge uses configuration_hash for item matching</Item>
            <Item>Authenticated cart promo code takes precedence</Item>
            <Item>Merge is idempotent (re-merge has no effect)</Item>
          </INVARIANTS>

          <ERROR_HANDLING>
            <Item type="BUSINESS" code="ERR-CART-ANONYMOUS-NOT-FOUND">Anonymous cart not found</Item>
            <Item type="TECHNICAL" code="ERR-CART-PRICING-UNAVAILABLE">Cannot refresh prices</Item>
          </ERROR_HANDLING>

          <BLOCK_ANCHORS>
            <Item id="BA-CART-MERGE-01">Load both carts</Item>
            <Item id="BA-CART-MERGE-02">Match items by configuration_hash</Item>
            <Item id="BA-CART-MERGE-03">Merge items (sum quantities for matches, add new)</Item>
            <Item id="BA-CART-MERGE-04">Resolve promo code precedence</Item>
            <Item id="BA-CART-MERGE-05">Refresh all prices</Item>
            <Item id="BA-CART-MERGE-06">Delete anonymous cart</Item>
            <Item id="BA-CART-MERGE-07">Persist and publish event</Item>
          </BLOCK_ANCHORS>

          <LOGGING>
            <Item>[SVC=cart-service][UC=UC-CART-MANAGE][BLOCK=BA-CART-MERGE-01][STATE=LOAD_CARTS]
              eventType=MERGE_CARTS_REQUEST keyValues=customerId,sessionId</Item>
            <Item>[SVC=cart-service][UC=UC-CART-MANAGE][BLOCK=BA-CART-MERGE-03][STATE=MERGE_ITEMS]
              eventType=ITEMS_MERGED keyValues=itemsMerged,itemsAdded</Item>
            <Item>[SVC=cart-service][UC=UC-CART-MANAGE][BLOCK=BA-CART-MERGE-07][STATE=COMPLETE]
              eventType=CARTS_MERGED decision=SUCCESS keyValues=sourceCartId,targetCartId,totalItems</Item>
          </LOGGING>

          <TESTS>
            <Case id="TC-FUNC-CART-MERGE-001">Merge combines items from both carts</Case>
            <Case id="TC-FUNC-CART-MERGE-002">Same configuration items have quantities summed</Case>
            <Case id="TC-FUNC-CART-MERGE-003">Anonymous cart deleted after merge</Case>
            <Case id="TC-FUNC-CART-MERGE-004">Authenticated promo code preserved</Case>
            <Case id="TC-FUNC-CART-MERGE-005">Anonymous promo adopted if no auth promo</Case>
          </TESTS>
        </FUNCTION_CONTRACT> */
    @Override
    public MergeCartsResult mergeCarts(MergeCartsCommand command) {
        String customerId = normalize(command.customerId());
        String sessionId = normalize(command.anonymousSessionId());
        if (!hasText(customerId) || !hasText(sessionId)) {
            throw new IllegalArgumentException("customerId and anonymousSessionId must be provided");
        }

        Cart target = cartRepository.findByCustomerId(customerId)
            .orElseGet(() -> createCustomerCart(customerId, Currency.RUB));
        Cart source = cartRepository.findBySessionId(sessionId)
            .orElseThrow(() -> CartDomainErrors.anonymousCartNotFound(sessionId));

        // BA-CART-MERGE-01: Load both carts
        log.info(logLine(
            USE_CASE_MANAGE,
            "BA-CART-MERGE-01",
            "LOAD_CARTS",
            "MERGE_CARTS_REQUEST",
            "REQUEST",
            "customerId=" + customerId + ",sessionId=" + sessionId
        ));

        // BA-CART-MERGE-02: Match items by configuration_hash
        // BA-CART-MERGE-04: Resolve promo code precedence
        CartMergeService.MergeResult mergeResult = mergeService.merge(source, target, totalsCalculator);

        // BA-CART-MERGE-03: Merge items (sum quantities for matches, add new)
        log.info(logLine(
            USE_CASE_MANAGE,
            "BA-CART-MERGE-03",
            "MERGE_ITEMS",
            "ITEMS_MERGED",
            "MERGED",
            "itemsMerged=" + mergeResult.itemsMerged() + ",itemsAdded=" + mergeResult.itemsAdded()
        ));

        // BA-CART-MERGE-05: Refresh all prices
        RefreshOutcome outcome = refreshPricesInternal(target);
        if (outcome.failCount() > 0) {
            throw CartDomainErrors.pricingUnavailable("Pricing service unavailable", null);
        }

        // BA-CART-MERGE-06: Delete anonymous cart
        source.clear(totalsCalculator);
        source.markMerged();
        Cart savedSource = cartRepository.save(source);
        Cart savedTarget = cartRepository.save(target);
        sessionCartStore.removeCartId(sessionId);
        eventPublisher.publish(
            TOPIC_CART_MERGED,
            CartMergedEvent.create(savedSource, savedTarget, mergeResult)
        );

        // BA-CART-MERGE-07: Persist and publish event
        log.info(logLine(
            USE_CASE_MANAGE,
            "BA-CART-MERGE-07",
            "COMPLETE",
            "CARTS_MERGED",
            "SUCCESS",
            "sourceCartId=" + savedSource.cartId()
                + ",targetCartId=" + savedTarget.cartId()
                + ",totalItems=" + savedTarget.totals().itemCount()
        ));

        return new MergeCartsResult(
            toDto(savedTarget),
            mergeResult.itemsFromAnonymous(),
            mergeResult.itemsMerged(),
            mergeResult.itemsAdded(),
            mergeResult.promoCodePreserved(),
            mergeResult.promoCodeSource()
        );
    }

    /* <FUNCTION_CONTRACT
          id="FC-cart-createSnapshot"
          LAYER="application.service"
          INTENT="Create immutable cart snapshot for checkout handoff to order-service"
          INPUT="CreateSnapshotCommand (customerId)"
          OUTPUT="CartSnapshotResponse (snapshotId, cart_snapshot, valid_until)"
          SIDE_EFFECTS="Creates snapshot; clears cart; publishes CartCheckedOutEvent"
          LINKS="RequirementsAnalysis.xml#UC-ORDER-PLACE;DevelopmentPlan.xml#Flow-Checkout-Payment">

          <PRECONDITIONS>
            <Item>Customer is authenticated</Item>
            <Item>Cart has at least one item</Item>
            <Item>All items have VALID configuration status</Item>
          </PRECONDITIONS>

          <POSTCONDITIONS>
            <Item>Immutable snapshot created with fresh prices</Item>
            <Item>Original cart cleared (status CHECKED_OUT)</Item>
            <Item>Snapshot has validity period (for order creation)</Item>
            <Item>CartCheckedOutEvent published</Item>
          </POSTCONDITIONS>

          <INVARIANTS>
            <Item>Snapshot is immutable after creation</Item>
            <Item>All prices refreshed at snapshot time</Item>
            <Item>Snapshot contains complete configuration for order creation</Item>
          </INVARIANTS>

          <ERROR_HANDLING>
            <Item type="BUSINESS" code="ERR-CART-EMPTY">Cannot create snapshot of empty cart</Item>
            <Item type="BUSINESS" code="ERR-CART-INVALID-ITEMS">Cart contains invalid configurations</Item>
            <Item type="BUSINESS" code="ERR-CART-ANONYMOUS">Anonymous users must authenticate first</Item>
            <Item type="TECHNICAL" code="ERR-CART-PRICING-UNAVAILABLE">Cannot refresh prices</Item>
          </ERROR_HANDLING>

          <BLOCK_ANCHORS>
            <Item id="BA-CART-SNAP-01">Load cart and validate not empty</Item>
            <Item id="BA-CART-SNAP-02">Validate all configurations</Item>
            <Item id="BA-CART-SNAP-03">Refresh all prices</Item>
            <Item id="BA-CART-SNAP-04">Create immutable snapshot</Item>
            <Item id="BA-CART-SNAP-05">Clear original cart (set status CHECKED_OUT)</Item>
            <Item id="BA-CART-SNAP-06">Persist and publish event</Item>
          </BLOCK_ANCHORS>

          <LOGGING>
            <Item>[SVC=cart-service][UC=UC-ORDER-PLACE][BLOCK=BA-CART-SNAP-01][STATE=LOAD_CART]
              eventType=CREATE_SNAPSHOT_REQUEST keyValues=customerId,itemCount</Item>
            <Item>[SVC=cart-service][UC=UC-ORDER-PLACE][BLOCK=BA-CART-SNAP-02][STATE=VALIDATE_ALL]
              eventType=SNAPSHOT_VALIDATION decision=PASS|FAIL keyValues=validCount,invalidCount</Item>
            <Item>[SVC=cart-service][UC=UC-ORDER-PLACE][BLOCK=BA-CART-SNAP-03][STATE=REFRESH_PRICES]
              eventType=PRICES_REFRESHED keyValues=priceChanges,totalChange</Item>
            <Item>[SVC=cart-service][UC=UC-ORDER-PLACE][BLOCK=BA-CART-SNAP-06][STATE=COMPLETE]
              eventType=SNAPSHOT_CREATED decision=SUCCESS keyValues=snapshotId,total,validUntil</Item>
          </LOGGING>

          <TESTS>
            <Case id="TC-FUNC-CART-SNAP-001">Snapshot created with fresh prices</Case>
            <Case id="TC-FUNC-CART-SNAP-002">Original cart cleared after snapshot</Case>
            <Case id="TC-FUNC-CART-SNAP-003">Invalid items prevent snapshot creation</Case>
            <Case id="TC-FUNC-CART-SNAP-004">Anonymous cart rejected</Case>
            <Case id="TC-FUNC-CART-SNAP-005">Snapshot has validity period</Case>
          </TESTS>
        </FUNCTION_CONTRACT> */
    @Override
    public CreateSnapshotResult createSnapshot(CreateSnapshotCommand command) {
        String customerId = normalize(command.customerId());
        if (!hasText(customerId)) {
            throw CartDomainErrors.anonymousNotAllowed();
        }

        Cart cart = cartRepository.findByCustomerId(customerId)
            .orElseThrow(CartDomainErrors::emptyCart);
        if (cart.items().isEmpty()) {
            throw CartDomainErrors.emptyCart();
        }

        // BA-CART-SNAP-01: Load cart and validate not empty
        log.info(logLine(
            USE_CASE_ORDER,
            "BA-CART-SNAP-01",
            "LOAD_CART",
            "CREATE_SNAPSHOT_REQUEST",
            "REQUEST",
            "customerId=" + customerId + ",itemCount=" + cart.items().size()
        ));

        int validCount = 0;
        int invalidCount = 0;
        for (CartItem item : cart.items()) {
            CatalogConfigurationPort.ValidationResult validationResult;
            try {
                validationResult = catalogConfigurationPort.validateConfiguration(item.configurationSnapshot());
            } catch (Exception ex) {
                validationResult = CatalogConfigurationPort.ValidationResult.unavailable();
            }
            if (validationResult != null && validationResult.available() && validationResult.valid()) {
                validCount++;
            } else {
                invalidCount++;
            }
        }

        // BA-CART-SNAP-02: Validate all configurations
        log.info(logLine(
            USE_CASE_ORDER,
            "BA-CART-SNAP-02",
            "VALIDATE_ALL",
            "SNAPSHOT_VALIDATION",
            invalidCount == 0 ? "PASS" : "FAIL",
            "validCount=" + validCount + ",invalidCount=" + invalidCount
        ));
        if (invalidCount > 0) {
            throw CartDomainErrors.invalidItems("Cart contains invalid configurations");
        }

        RefreshOutcome outcome = refreshPricesInternal(cart);
        if (outcome.failCount() > 0) {
            throw CartDomainErrors.pricingUnavailable("Pricing service unavailable", null);
        }

        Money totalChange = outcome.newTotal().subtract(outcome.previousTotal());
        // BA-CART-SNAP-03: Refresh all prices
        log.info(logLine(
            USE_CASE_ORDER,
            "BA-CART-SNAP-03",
            "REFRESH_PRICES",
            "PRICES_REFRESHED",
            "COMPLETE",
            "priceChanges=" + outcome.itemsUpdated() + ",totalChange=" + totalChange
        ));

        double changePercent = outcome.changePercent();
        if (changePercent > PRICE_CHANGE_THRESHOLD_PERCENT && !command.acknowledgePriceChanges()) {
            throw new IllegalStateException("Price changes require acknowledgment");
        }

        Duration validity = properties.getSnapshotValidity() == null
            ? Duration.ofMinutes(15)
            : properties.getSnapshotValidity();
        // BA-CART-SNAP-04: Create immutable snapshot
        CartSnapshot snapshot = cart.createSnapshot(SnapshotId.generate(), validity, Instant.now());
        // BA-CART-SNAP-05: Clear original cart (set status CHECKED_OUT)
        cart.markCheckedOut();

        CartSnapshot savedSnapshot = cartSnapshotRepository.save(snapshot);
        Cart savedCart = cartRepository.save(cart);
        eventPublisher.publish(TOPIC_CART_CHECKOUT, CartCheckedOutEvent.create(savedCart, savedSnapshot));

        // BA-CART-SNAP-06: Persist and publish event
        log.info(logLine(
            USE_CASE_ORDER,
            "BA-CART-SNAP-06",
            "COMPLETE",
            "SNAPSHOT_CREATED",
            "SUCCESS",
            "snapshotId=" + savedSnapshot.snapshotId() + ",total=" + savedSnapshot.totals().total()
                + ",validUntil=" + savedSnapshot.validUntil()
        ));

        return new CreateSnapshotResult(
            savedSnapshot.snapshotId().toString(),
            toSnapshotDto(savedSnapshot),
            savedSnapshot.validUntil(),
            outcome.totalChanged(),
            outcome.previousTotal()
        );
    }

    private Cart resolveExistingCart(String customerId, String sessionId) {
        if (hasText(customerId)) {
            return cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> CartDomainErrors.cartNotFound("Cart not found for customerId=" + customerId));
        }
        if (hasText(sessionId)) {
            return cartRepository.findBySessionId(sessionId)
                .orElseThrow(() -> CartDomainErrors.cartNotFound("Cart not found for sessionId=" + sessionId));
        }
        throw new IllegalArgumentException("customerId or sessionId must be provided");
    }

    private Cart createCustomerCart(String customerId, Currency currency) {
        CartId cartId = resolveCustomerCartId(customerId);
        Instant now = Instant.now();
        return Cart.rehydrate(
            cartId,
            customerId,
            null,
            CartStatus.ACTIVE,
            null,
            CartTotals.empty(currency),
            List.of(),
            now,
            now,
            0
        );
    }

    private CartId resolveCustomerCartId(String customerId) {
        try {
            return CartId.of(UUID.fromString(customerId));
        } catch (IllegalArgumentException ex) {
            UUID stableId = UUID.nameUUIDFromBytes(customerId.getBytes(StandardCharsets.UTF_8));
            return CartId.of(stableId);
        }
    }

    private ConfigurationSnapshot buildSnapshot(String productTemplateId,
                                                DimensionsDto dimensions,
                                                List<SelectedOptionDto> selectedOptions,
                                                List<BomLineDto> resolvedBom) {
        if (!hasText(productTemplateId)) {
            throw CartDomainErrors.invalidConfiguration("productTemplateId is required");
        }
        if (dimensions == null || dimensions.widthCm() == null || dimensions.heightCm() == null) {
            throw CartDomainErrors.invalidConfiguration("Dimensions are required");
        }
        List<ConfigurationSnapshot.SelectedOptionSnapshot> optionSnapshots = toSelectedOptionSnapshots(selectedOptions);
        List<ConfigurationSnapshot.BomLineSnapshot> bomSnapshots = toBomLineSnapshots(resolvedBom);
        return new ConfigurationSnapshot(
            productTemplateId,
            dimensions.widthCm(),
            dimensions.heightCm(),
            optionSnapshots,
            bomSnapshots
        );
    }

    private List<ConfigurationSnapshot.SelectedOptionSnapshot> toSelectedOptionSnapshots(
        List<SelectedOptionDto> selectedOptions
    ) {
        if (selectedOptions == null || selectedOptions.isEmpty()) {
            return List.of();
        }
        List<ConfigurationSnapshot.SelectedOptionSnapshot> snapshots = new ArrayList<>();
        for (SelectedOptionDto option : selectedOptions) {
            snapshots.add(new ConfigurationSnapshot.SelectedOptionSnapshot(
                option.optionGroupId(),
                option.optionId()
            ));
        }
        return snapshots;
    }

    private List<ConfigurationSnapshot.BomLineSnapshot> toBomLineSnapshots(List<BomLineDto> resolvedBom) {
        if (resolvedBom == null || resolvedBom.isEmpty()) {
            return List.of();
        }
        List<ConfigurationSnapshot.BomLineSnapshot> snapshots = new ArrayList<>();
        for (BomLineDto line : resolvedBom) {
            snapshots.add(new ConfigurationSnapshot.BomLineSnapshot(
                line.sku(),
                line.description(),
                line.quantity()
            ));
        }
        return snapshots;
    }

    private CartDto toDto(Cart cart) {
        List<CartItemDto> items = cart.items().stream()
            .map(this::toDto)
            .toList();
        return toDto(cart, items);
    }

    private CartDto toDto(Cart cart, List<CartItemDto> items) {
        return new CartDto(
            cart.cartId().toString(),
            cart.customerId(),
            cart.sessionId(),
            cart.status(),
            items,
            cart.totals().subtotal(),
            cart.totals().discount(),
            cart.totals().tax(),
            cart.totals().total(),
            toDto(cart.appliedPromoCode()),
            cart.totals().itemCount(),
            cart.createdAt(),
            cart.updatedAt()
        );
    }

    private CartItemDto toDto(CartItem item) {
        return toDto(item, item.validationStatus(), item.validationMessage(), item.priceStale());
    }

    private CartItemDto toDto(CartItem item,
                              ValidationStatus validationStatus,
                              String validationMessage,
                              boolean priceStale) {
        ConfigurationSnapshot snapshot = item.configurationSnapshot();
        List<SelectedOptionDto> options = snapshot.selectedOptions().stream()
            .map(option -> new SelectedOptionDto(option.optionGroupId(), option.optionId()))
            .toList();
        List<BomLineDto> bomLines = snapshot.resolvedBom().stream()
            .map(line -> new BomLineDto(line.sku(), line.description(), line.quantity()))
            .toList();
        return new CartItemDto(
            item.itemId().toString(),
            item.productTemplateId(),
            item.productName(),
            item.productFamily(),
            new DimensionsDto(snapshot.widthCm(), snapshot.heightCm()),
            options,
            bomLines,
            item.quantity(),
            item.unitPrice(),
            item.lineTotal(),
            item.quoteReference().quoteId(),
            item.quoteReference().validUntil(),
            validationStatus,
            validationMessage,
            priceStale,
            item.configurationHash(),
            item.thumbnailUrl()
        );
    }

    private CartSnapshotDto toSnapshotDto(CartSnapshot snapshot) {
        List<CartItemDto> items = snapshot.items().stream()
            .map(this::toSnapshotItemDto)
            .toList();
        return new CartSnapshotDto(
            snapshot.snapshotId().toString(),
            snapshot.cartId().toString(),
            snapshot.customerId(),
            items,
            snapshot.totals().subtotal(),
            snapshot.totals().discount(),
            snapshot.totals().tax(),
            snapshot.totals().total(),
            toDto(snapshot.appliedPromoCode()),
            snapshot.totals().itemCount(),
            snapshot.createdAt(),
            snapshot.validUntil()
        );
    }

    private CartItemDto toSnapshotItemDto(CartSnapshotItem item) {
        ConfigurationSnapshot snapshot = item.configurationSnapshot();
        List<SelectedOptionDto> options = snapshot.selectedOptions().stream()
            .map(option -> new SelectedOptionDto(option.optionGroupId(), option.optionId()))
            .toList();
        List<BomLineDto> bomLines = snapshot.resolvedBom().stream()
            .map(line -> new BomLineDto(line.sku(), line.description(), line.quantity()))
            .toList();
        return new CartItemDto(
            item.itemId(),
            item.productTemplateId(),
            item.productName(),
            item.productFamily(),
            new DimensionsDto(snapshot.widthCm(), snapshot.heightCm()),
            options,
            bomLines,
            item.quantity(),
            item.unitPrice(),
            item.lineTotal(),
            item.quoteReference().quoteId(),
            item.quoteReference().validUntil(),
            ValidationStatus.VALID,
            null,
            false,
            item.configurationHash(),
            item.thumbnailUrl()
        );
    }

    private AppliedPromoCodeDto toDto(AppliedPromoCode promoCode) {
        if (promoCode == null) {
            return null;
        }
        return new AppliedPromoCodeDto(
            promoCode.code(),
            promoCode.discountAmount(),
            promoCode.description(),
            promoCode.appliedAt()
        );
    }

    private RefreshOutcome refreshPricesInternal(Cart cart) {
        Money previousTotal = cart.totals().total();
        int successCount = 0;
        int failCount = 0;
        int itemsUpdated = 0;
        Instant now = Instant.now();
        Currency currency = resolveCurrency(cart);

        for (CartItem item : cart.items()) {
            PricingPort.PriceQuote quote = pricingPort.calculateQuote(item.configurationSnapshot(), currency);
            if (quote == null || !quote.available() || quote.quoteId() == null) {
                failCount++;
                continue;
            }
            if (quote.validUntil() != null && quote.validUntil().isBefore(now)) {
                failCount++;
                continue;
            }
            Money oldLineTotal = item.lineTotal();
            // BA-CART-REFRESH-03: Update item prices, quote_id, quote_valid_until
            item.updatePrice(quote.unitPrice(), new PriceQuoteReference(quote.quoteId(), quote.validUntil()));
            // BA-CART-REFRESH-04: Clear price_stale flags
            item.clearPriceStale();
            if (oldLineTotal.compareTo(item.lineTotal()) != 0) {
                itemsUpdated++;
            }
            successCount++;
        }

        Money newTotal = previousTotal;
        double changePercent = 0.0d;
        boolean totalChanged = false;
        if (successCount > 0) {
            Money subtotal = totalsCalculator.calculateTotals(cart.items(), null, null).subtotal();
            AppliedPromoCode refreshedPromo = refreshPromoDiscount(cart.appliedPromoCode(), subtotal);
            if (refreshedPromo != null) {
                // BA-CART-REFRESH-06: Recalculate promo discount if applied
                cart.recalculatePromoDiscount(refreshedPromo, totalsCalculator, null);
            } else {
                // BA-CART-REFRESH-05: Recalculate cart totals
                cart.calculateTotals(totalsCalculator, null);
            }
            newTotal = cart.totals().total();
            changePercent = calculatePercentChange(previousTotal, newTotal);
            totalChanged = previousTotal.compareTo(newTotal) != 0;
        }

        return new RefreshOutcome(
            previousTotal,
            newTotal,
            changePercent,
            itemsUpdated,
            successCount,
            failCount,
            totalChanged
        );
    }

    private AppliedPromoCode refreshPromoDiscount(AppliedPromoCode promoCode, Money subtotal) {
        if (promoCode == null) {
            return null;
        }
        PricingPort.PromoValidationResult validationResult = pricingPort.validatePromoCode(
            promoCode.code(),
            subtotal
        );
        if (validationResult == null || !validationResult.available()) {
            return promoCode;
        }
        if (!validationResult.valid()) {
            return new AppliedPromoCode(
                promoCode.code(),
                Money.zero(subtotal.getCurrency()),
                validationResult.errorMessage(),
                promoCode.appliedAt()
            );
        }
        Money discount = validationResult.discountAmount();
        if (discount == null) {
            discount = Money.zero(subtotal.getCurrency());
        }
        return new AppliedPromoCode(
            promoCode.code(),
            discount,
            promoCode.description(),
            promoCode.appliedAt()
        );
    }

    private String requireProductName(String productName) {
        if (!hasText(productName)) {
            throw CartDomainErrors.invalidConfiguration("productName is required");
        }
        return productName.strip();
    }

    private String normalizeProductFamily(String productFamily) {
        if (!hasText(productFamily)) {
            return null;
        }
        String normalized = productFamily.strip().toUpperCase(Locale.ROOT);
        if (!List.of("WINDOW", "DOOR", "ACCESSORY").contains(normalized)) {
            throw CartDomainErrors.invalidConfiguration("Unsupported product family: " + productFamily);
        }
        return normalized;
    }

    private Currency resolveCurrency(Cart cart) {
        if (cart == null || cart.totals() == null || cart.totals().subtotal() == null) {
            return Currency.RUB;
        }
        return cart.totals().subtotal().getCurrency();
    }

    private double calculatePercentChange(Money previousTotal, Money newTotal) {
        if (previousTotal == null || newTotal == null) {
            return 0.0d;
        }
        BigDecimal previous = previousTotal.getAmount().abs();
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return newTotal.getAmount().compareTo(BigDecimal.ZERO) == 0 ? 0.0d : 100.0d;
        }
        BigDecimal difference = newTotal.getAmount().subtract(previousTotal.getAmount()).abs();
        return difference
            .divide(previous, MathContext.DECIMAL64)
            .multiply(BigDecimal.valueOf(100))
            .doubleValue();
    }

    private String formatValidationMessage(List<String> errors) {
        if (errors == null || errors.isEmpty()) {
            return null;
        }
        return String.join("; ", errors);
    }

    private String mapPromoErrorCode(String errorCode) {
        if ("ERR-PROMO-MIN-SUBTOTAL".equals(errorCode)) {
            return "ERR-CART-PROMO-MIN-NOT-MET";
        }
        return "ERR-CART-PROMO-INVALID";
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.strip();
        return trimmed.isBlank() ? null : trimmed;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String logLine(String useCase,
                           String block,
                           String state,
                           String eventType,
                           String decision,
                           String keyValues) {
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

    private record RefreshOutcome(
        Money previousTotal,
        Money newTotal,
        double changePercent,
        int itemsUpdated,
        int successCount,
        int failCount,
        boolean totalChanged
    ) {
    }
}
