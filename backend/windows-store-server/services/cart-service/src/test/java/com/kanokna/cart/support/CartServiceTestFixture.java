package com.kanokna.cart.support;

import com.kanokna.cart.adapters.config.CartProperties;
import com.kanokna.cart.application.dto.AddItemCommand;
import com.kanokna.cart.application.dto.BomLineDto;
import com.kanokna.cart.application.dto.DimensionsDto;
import com.kanokna.cart.application.dto.SelectedOptionDto;
import com.kanokna.cart.application.port.out.CartRepository;
import com.kanokna.cart.application.port.out.CartSnapshotRepository;
import com.kanokna.cart.application.port.out.CatalogConfigurationPort;
import com.kanokna.cart.application.port.out.EventPublisher;
import com.kanokna.cart.application.port.out.PricingPort;
import com.kanokna.cart.application.port.out.SessionCartStore;
import com.kanokna.cart.application.service.CartApplicationService;
import com.kanokna.cart.domain.model.AppliedPromoCode;
import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.domain.model.CartItem;
import com.kanokna.cart.domain.model.CartSnapshot;
import com.kanokna.cart.domain.model.ConfigurationSnapshot;
import com.kanokna.cart.domain.model.PriceQuoteReference;
import com.kanokna.cart.domain.model.ValidationStatus;
import com.kanokna.cart.domain.service.CartMergeService;
import com.kanokna.cart.domain.service.CartTotalsCalculator;
import com.kanokna.cart.domain.service.ConfigurationHashService;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;

public final class CartServiceTestFixture {
    private CartServiceTestFixture() {
    }

    public static CartProperties cartProperties() {
        CartProperties props = new CartProperties();
        props.setAnonymousTtl(Duration.ofDays(7));
        props.setAbandonedThreshold(Duration.ofHours(72));
        props.setSnapshotValidity(Duration.ofMinutes(15));
        return props;
    }

    public static DimensionsDto dimensions() {
        return new DimensionsDto(120, 130);
    }

    public static ConfigurationSnapshot snapshot(String productTemplateId) {
        return new ConfigurationSnapshot(
            productTemplateId,
            120,
            130,
            List.of(new ConfigurationSnapshot.SelectedOptionSnapshot("OPT-GROUP", "OPT-1")),
            List.of(new ConfigurationSnapshot.BomLineSnapshot("SKU-1", "Line 1", 1))
        );
    }

    public static CartItem item(
        String templateId,
        String name,
        String family,
        int quantity,
        Money unitPrice,
        String hash,
        Instant validUntil
    ) {
        return CartItem.create(
            templateId,
            name,
            family,
            snapshot(templateId),
            hash,
            quantity,
            unitPrice,
            new PriceQuoteReference("QUOTE-" + templateId, validUntil),
            ValidationStatus.VALID,
            null,
            "http://example.com/item.png",
            false,
            Instant.now()
        );
    }

    public static Cart cartWithItems(String customerId, CartTotalsCalculator calculator, CartItem... items) {
        Cart cart = Cart.createForCustomer(customerId, Currency.RUB);
        for (CartItem item : items) {
            cart.addItem(item, calculator, null);
        }
        return cart;
    }

    public static Cart cartWithSession(String sessionId, CartTotalsCalculator calculator, CartItem... items) {
        Cart cart = Cart.createForSession(sessionId, Currency.RUB);
        for (CartItem item : items) {
            cart.addItem(item, calculator, null);
        }
        return cart;
    }

    public static AppliedPromoCode promo(String code, Money discount) {
        return new AppliedPromoCode(code, discount, null, Instant.now());
    }

    public static AddItemCommand addItemCommand(
        String customerId,
        String sessionId,
        String templateId,
        String productName,
        String family,
        int quantity,
        String quoteId
    ) {
        return new AddItemCommand(
            customerId,
            sessionId,
            templateId,
            productName,
            family,
            "http://example.com/thumb.png",
            dimensions(),
            List.of(new SelectedOptionDto("OPT-GROUP", "OPT-1")),
            quantity,
            quoteId,
            List.of(new BomLineDto("SKU-1", "Line 1", 1))
        );
    }

    public static Money money(String amount) {
        return Money.of(new BigDecimal(amount), Currency.RUB);
    }

    public static class TestContext {
        public final InMemoryCartRepository cartRepository = new InMemoryCartRepository();
        public final InMemoryCartSnapshotRepository snapshotRepository = new InMemoryCartSnapshotRepository();
        public final FakeCatalogConfigurationPort catalogPort = new FakeCatalogConfigurationPort();
        public final FakePricingPort pricingPort = new FakePricingPort();
        public final RecordingEventPublisher eventPublisher = new RecordingEventPublisher();
        public final InMemorySessionCartStore sessionCartStore = new InMemorySessionCartStore();
        public final CartTotalsCalculator totalsCalculator = new CartTotalsCalculator();
        public final ConfigurationHashService configurationHashService = new ConfigurationHashService();
        public final CartMergeService mergeService = new CartMergeService();
        public final CartProperties properties = cartProperties();
        public final CartApplicationService service = new CartApplicationService(
            cartRepository,
            snapshotRepository,
            catalogPort,
            pricingPort,
            eventPublisher,
            sessionCartStore,
            totalsCalculator,
            configurationHashService,
            mergeService,
            properties
        );
    }

    public static final class RecordingEventPublisher implements EventPublisher {
        private final List<EventRecord> events = new ArrayList<>();

        @Override
        public <T> void publish(String topic, T event) {
            events.add(new EventRecord(topic, event));
        }

        public List<EventRecord> events() {
            return events;
        }
    }

    public record EventRecord(String topic, Object event) {
    }

    public static final class InMemoryCartRepository implements CartRepository {
        private final Map<String, Cart> byCustomer = new HashMap<>();
        private final Map<String, Cart> bySession = new HashMap<>();

        @Override
        public Optional<Cart> findByCustomerId(String customerId) {
            return Optional.ofNullable(byCustomer.get(customerId));
        }

        @Override
        public Optional<Cart> findBySessionId(String sessionId) {
            return Optional.ofNullable(bySession.get(sessionId));
        }

        @Override
        public Cart save(Cart cart) {
            if (cart.customerId() != null) {
                byCustomer.put(cart.customerId(), cart);
            }
            if (cart.sessionId() != null) {
                bySession.put(cart.sessionId(), cart);
            }
            return cart;
        }
    }

    public static final class InMemoryCartSnapshotRepository implements CartSnapshotRepository {
        private final Map<String, CartSnapshot> snapshots = new HashMap<>();

        @Override
        public CartSnapshot save(CartSnapshot snapshot) {
            snapshots.put(snapshot.snapshotId().toString(), snapshot);
            return snapshot;
        }

        public Optional<CartSnapshot> findById(String snapshotId) {
            return Optional.ofNullable(snapshots.get(snapshotId));
        }
    }

    public static final class InMemorySessionCartStore implements SessionCartStore {
        private final Map<String, String> sessions = new HashMap<>();

        @Override
        public Optional<String> findCartId(String sessionId) {
            return Optional.ofNullable(sessions.get(sessionId));
        }

        @Override
        public void storeCartId(String sessionId, String cartId) {
            sessions.put(sessionId, cartId);
        }

        @Override
        public void removeCartId(String sessionId) {
            sessions.remove(sessionId);
        }
    }

    public static final class FakeCatalogConfigurationPort implements CatalogConfigurationPort {
        private boolean available = true;
        private boolean valid = true;
        private List<String> errors = List.of();
        private List<ConfigurationSnapshot.BomLineSnapshot> resolvedBom = List.of();
        private RuntimeException failure;

        public void setAvailable(boolean available) {
            this.available = available;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public void setErrors(List<String> errors) {
            this.errors = errors == null ? List.of() : errors;
        }

        public void setResolvedBom(List<ConfigurationSnapshot.BomLineSnapshot> resolvedBom) {
            this.resolvedBom = resolvedBom == null ? List.of() : resolvedBom;
        }

        public void setFailure(RuntimeException failure) {
            this.failure = failure;
        }

        @Override
        public ValidationResult validateConfiguration(ConfigurationSnapshot snapshot) {
            if (failure != null) {
                throw failure;
            }
            return new ValidationResult(available, valid, errors, resolvedBom);
        }
    }

    public static final class FakePricingPort implements PricingPort {
        private boolean available = true;
        private PriceQuote defaultQuote = new PriceQuote(
            true,
            "QUOTE-DEFAULT",
            Money.of(new BigDecimal("1000.00"), Currency.RUB),
            Instant.now().plusSeconds(3600)
        );
        private PromoValidationResult defaultPromo = new PromoValidationResult(
            true,
            true,
            Money.zero(Currency.RUB),
            null,
            null
        );
        private final Map<String, Queue<PriceQuote>> quotesByTemplate = new HashMap<>();
        private final Map<String, PromoValidationResult> promoByCode = new HashMap<>();
        private int calculateQuoteCalls;

        public void setAvailable(boolean available) {
            this.available = available;
        }

        public void setDefaultQuote(PriceQuote defaultQuote) {
            this.defaultQuote = defaultQuote;
        }

        public void setDefaultPromo(PromoValidationResult defaultPromo) {
            this.defaultPromo = defaultPromo;
        }

        public void queueQuote(String templateId, PriceQuote quote) {
            quotesByTemplate.computeIfAbsent(templateId, key -> new ArrayDeque<>()).add(quote);
        }

        public void setPromoResult(String promoCode, PromoValidationResult result) {
            promoByCode.put(promoCode, result);
        }

        public int calculateQuoteCalls() {
            return calculateQuoteCalls;
        }

        @Override
        public PriceQuote calculateQuote(ConfigurationSnapshot snapshot, Currency currency) {
            calculateQuoteCalls++;
            if (!available) {
                return PriceQuote.unavailable();
            }
            Queue<PriceQuote> queue = quotesByTemplate.get(snapshot.productTemplateId());
            if (queue != null && !queue.isEmpty()) {
                return queue.remove();
            }
            return defaultQuote;
        }

        @Override
        public PromoValidationResult validatePromoCode(String promoCode, Money subtotal) {
            if (!available) {
                return PromoValidationResult.unavailable();
            }
            return promoByCode.getOrDefault(promoCode, defaultPromo);
        }
    }
}
