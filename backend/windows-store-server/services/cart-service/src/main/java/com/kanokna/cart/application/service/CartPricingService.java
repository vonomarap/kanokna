package com.kanokna.cart.application.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.kanokna.cart.adapters.config.CartProperties;
import com.kanokna.cart.application.port.out.PricingPort;
import com.kanokna.cart.application.service.dto.PriceRefreshResult;
import com.kanokna.cart.domain.model.AppliedPromoCode;
import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.domain.model.CartItem;
import com.kanokna.cart.domain.model.ConfigurationSnapshot;
import com.kanokna.cart.domain.model.PriceQuoteReference;
import com.kanokna.cart.domain.service.CartTotalsCalculator;
import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;

/**
 * MODULE_CONTRACT id="MC-cart-pricing"
 * LAYER="application.service"
 * INTENT="Handle all pricing operations: quote fetching, price refresh, staleness checks"
 * LINKS="RequirementsAnalysis.xml#UC-CART-MANAGE;DevelopmentPlan.xml#DP-SVC-pricing-service"
 *
 * Service responsible for cart pricing operations.
 * Delegates to pricing-service for actual price calculations.
 */
@Service
public class CartPricingService {

    private static final Logger log = LoggerFactory.getLogger(CartPricingService.class);
    private static final String SERVICE = "cart-service";
    private static final String USE_CASE = "UC-CART-MANAGE";

    private final PricingPort pricingPort;
    private final CartTotalsCalculator totalsCalculator;
    private final CartProperties properties;

    public CartPricingService(
            PricingPort pricingPort,
            CartTotalsCalculator totalsCalculator,
            CartProperties properties) {
        this.pricingPort = pricingPort;
        this.totalsCalculator = totalsCalculator;
        this.properties = properties;
    }

    /**
     * Fetches a price quote for a configuration.
     *
     * @param snapshot the configuration snapshot
     * @param currency the currency for pricing
     * @return the price quote result
     */
    public QuoteResult fetchQuote(ConfigurationSnapshot snapshot, Currency currency) {
        log.atDebug()
            .addKeyValue("svc", SERVICE)
            .addKeyValue("uc", USE_CASE)
            .addKeyValue("block", "BA-CART-PRICE-01")
            .addKeyValue("state", "FETCH_QUOTE")
            .addKeyValue("productTemplateId", snapshot.productTemplateId())
            .addKeyValue("currency", currency)
            .log("Fetching price quote");

        try {
            PricingPort.PriceQuote quote = pricingPort.calculateQuote(snapshot, currency);

            if (quote == null || !quote.available()) {
                log.atWarn()
                    .addKeyValue("svc", SERVICE)
                    .addKeyValue("uc", USE_CASE)
                    .addKeyValue("block", "BA-CART-PRICE-01")
                    .addKeyValue("state", "UNAVAILABLE")
                    .addKeyValue("productTemplateId", snapshot.productTemplateId())
                    .log("Pricing service unavailable");
                return QuoteResult.unavailable();
            }

            if (quote.quoteId() == null) {
                log.atWarn()
                    .addKeyValue("svc", SERVICE)
                    .addKeyValue("uc", USE_CASE)
                    .addKeyValue("block", "BA-CART-PRICE-01")
                    .addKeyValue("state", "NO_QUOTE_ID")
                    .addKeyValue("productTemplateId", snapshot.productTemplateId())
                    .log("Quote returned without ID");
                return QuoteResult.unavailable();
            }

            Instant now = Instant.now();
            if (quote.validUntil() != null && quote.validUntil().isBefore(now)) {
                log.atWarn()
                    .addKeyValue("svc", SERVICE)
                    .addKeyValue("uc", USE_CASE)
                    .addKeyValue("block", "BA-CART-PRICE-01")
                    .addKeyValue("state", "EXPIRED")
                    .addKeyValue("quoteId", quote.quoteId())
                    .addKeyValue("validUntil", quote.validUntil())
                    .log("Quote already expired");
                return QuoteResult.expired(quote.quoteId());
            }

            log.atDebug()
                .addKeyValue("svc", SERVICE)
                .addKeyValue("uc", USE_CASE)
                .addKeyValue("block", "BA-CART-PRICE-01")
                .addKeyValue("state", "SUCCESS")
                .addKeyValue("quoteId", quote.quoteId())
                .addKeyValue("unitPrice", quote.unitPrice())
                .log("Quote fetched successfully");

            return QuoteResult.success(quote.quoteId(), quote.unitPrice(), quote.validUntil());

        } catch (Exception ex) {
            log.atError()
                .addKeyValue("svc", SERVICE)
                .addKeyValue("uc", USE_CASE)
                .addKeyValue("block", "BA-CART-PRICE-01")
                .addKeyValue("state", "ERROR")
                .addKeyValue("productTemplateId", snapshot.productTemplateId())
                .setCause(ex)
                .log("Error fetching price quote");
            return QuoteResult.unavailable();
        }
    }

    /**
     * FUNCTION_CONTRACT id="FC-cart-pricing-refreshAllPrices"
     * Refreshes prices for all items in a cart.
     *
     * @param cart the cart to refresh prices for
     * @return refresh result with summary
     */
    public PriceRefreshResult refreshAllPrices(Cart cart) {
        log.atInfo()
            .addKeyValue("svc", SERVICE)
            .addKeyValue("uc", USE_CASE)
            .addKeyValue("block", "BA-CART-REFRESH-01")
            .addKeyValue("state", "START")
            .addKeyValue("cartId", cart.cartId().toString())
            .addKeyValue("itemCount", cart.items().size())
            .log("Starting price refresh for cart");

        Money previousTotal = cart.totals().total();
        int successCount = 0;
        int failCount = 0;
        int itemsUpdated = 0;
        Instant now = Instant.now();
        Currency currency = resolveCurrency(cart);

        // BA-CART-REFRESH-02: Iterate items and call pricing-service.CalculateQuote for each
        for (CartItem item : cart.items()) {
            QuoteResult quote = fetchQuote(item.configurationSnapshot(), currency);

            if (!quote.available()) {
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

        log.atInfo()
            .addKeyValue("svc", SERVICE)
            .addKeyValue("uc", USE_CASE)
            .addKeyValue("block", "BA-CART-REFRESH-02")
            .addKeyValue("state", "QUOTES_FETCHED")
            .addKeyValue("cartId", cart.cartId().toString())
            .addKeyValue("successCount", successCount)
            .addKeyValue("failCount", failCount)
            .log("Price quotes fetched");

        Money newTotal = previousTotal;
        double changePercent = 0.0;
        boolean totalChanged = false;

        if (successCount > 0) {
            // BA-CART-REFRESH-05: Recalculate cart totals
            Money subtotal = totalsCalculator.calculateTotals(cart.items(), null, null).subtotal();

            // BA-CART-REFRESH-06: Recalculate promo discount if applied
            AppliedPromoCode refreshedPromo = refreshPromoDiscount(cart.appliedPromoCode(), subtotal);
            if (refreshedPromo != null) {
                cart.recalculatePromoDiscount(refreshedPromo, totalsCalculator, null);
            } else {
                cart.calculateTotals(totalsCalculator, null);
            }

            newTotal = cart.totals().total();
            changePercent = calculatePercentChange(previousTotal, newTotal);
            totalChanged = previousTotal.compareTo(newTotal) != 0;
        }

        log.atInfo()
            .addKeyValue("svc", SERVICE)
            .addKeyValue("uc", USE_CASE)
            .addKeyValue("block", "BA-CART-REFRESH-05")
            .addKeyValue("state", "TOTALS_RECALCULATED")
            .addKeyValue("cartId", cart.cartId().toString())
            .addKeyValue("previousTotal", previousTotal)
            .addKeyValue("newTotal", newTotal)
            .addKeyValue("changePercent", changePercent)
            .log("Cart totals recalculated");

        return new PriceRefreshResult(
            previousTotal,
            newTotal,
            changePercent,
            itemsUpdated,
            successCount,
            failCount,
            totalChanged
        );
    }

    /**
     * Checks if any items in cart have stale prices.
     *
     * @param cart the cart to check
     * @return true if any prices are stale
     */
    public boolean checkPriceStaleness(Cart cart) {
        Instant now = Instant.now();
        for (CartItem item : cart.items()) {
            if (item.quoteReference().isStale(now)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if price change exceeds threshold.
     *
     * @param previousTotal previous total amount
     * @param newTotal new total amount
     * @return true if change exceeds configured threshold
     */
    public boolean isPriceChangeSignificant(Money previousTotal, Money newTotal) {
        double changePercent = calculatePercentChange(previousTotal, newTotal);
        return changePercent > properties.behavior().priceChangeThresholdPercent();
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
            return promoCode; // Keep existing
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

    private Currency resolveCurrency(Cart cart) {
        if (cart == null || cart.totals() == null || cart.totals().subtotal() == null) {
            return Currency.RUB;
        }
        return cart.totals().subtotal().getCurrency();
    }

    private double calculatePercentChange(Money previousTotal, Money newTotal) {
        if (previousTotal == null || newTotal == null) {
            return 0.0;
        }
        BigDecimal previous = previousTotal.getAmount().abs();
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            return newTotal.getAmount().compareTo(BigDecimal.ZERO) == 0 ? 0.0 : 100.0;
        }
        BigDecimal difference = newTotal.getAmount().subtract(previousTotal.getAmount()).abs();
        return difference
            .divide(previous, MathContext.DECIMAL64)
            .multiply(BigDecimal.valueOf(100))
            .doubleValue();
    }

    /**
     * Result of price quote fetch.
     */
    public record QuoteResult(
        boolean available,
        boolean expired,
        String quoteId,
        Money unitPrice,
        Instant validUntil
    ) {
        public static QuoteResult unavailable() {
            return new QuoteResult(false, false, null, null, null);
        }

        public static QuoteResult expired(String quoteId) {
            return new QuoteResult(false, true, quoteId, null, null);
        }

        public static QuoteResult success(String quoteId, Money unitPrice, Instant validUntil) {
            return new QuoteResult(true, false, quoteId, unitPrice, validUntil);
        }
    }
}
