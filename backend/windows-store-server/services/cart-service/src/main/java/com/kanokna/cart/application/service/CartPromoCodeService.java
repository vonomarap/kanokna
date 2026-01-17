package com.kanokna.cart.application.service;

import com.kanokna.cart.application.port.out.PricingPort;
import com.kanokna.cart.application.service.dto.ApplyPromoResult;
import com.kanokna.cart.domain.model.AppliedPromoCode;
import com.kanokna.cart.domain.model.Cart;
import com.kanokna.cart.domain.service.CartTotalsCalculator;
import com.kanokna.shared.money.Money;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * MODULE_CONTRACT id="MC-cart-promo-code"
 * LAYER="application.service"
 * INTENT="Handle promotional code validation, application, and removal"
 * LINKS="RequirementsAnalysis.xml#UC-CART-APPLY-PROMO;DevelopmentPlan.xml#Flow-Cart-PromoCode"
 *
 * Service responsible for promo code operations.
 * Delegates validation to pricing-service and applies discounts to cart.
 */
@Service
public class CartPromoCodeService {

    private static final Logger log = LoggerFactory.getLogger(CartPromoCodeService.class);
    private static final String SERVICE = "cart-service";
    private static final String USE_CASE = "UC-CART-APPLY-PROMO";

    private final PricingPort pricingPort;
    private final CartTotalsCalculator totalsCalculator;

    public CartPromoCodeService(PricingPort pricingPort, CartTotalsCalculator totalsCalculator) {
        this.pricingPort = pricingPort;
        this.totalsCalculator = totalsCalculator;
    }

    /**
     * FUNCTION_CONTRACT id="FC-cart-promo-applyPromoCode"
     * Validates and applies a promo code to the cart.
     *
     * @param cart the cart to apply promo to
     * @param promoCode the promo code string
     * @return result of the application attempt
     */
    public ApplyPromoResult applyPromoCode(Cart cart, String promoCode) {
        // BA-CART-PROMO-01: Load cart and validate not empty
        log.atInfo()
            .addKeyValue("svc", SERVICE)
            .addKeyValue("uc", USE_CASE)
            .addKeyValue("block", "BA-CART-PROMO-01")
            .addKeyValue("state", "START")
            .addKeyValue("cartId", cart.cartId().toString())
            .addKeyValue("promoCode", promoCode)
            .log("Applying promo code");

        // BA-CART-PROMO-02: Call pricing-service.ValidatePromoCode
        PricingPort.PromoValidationResult validationResult = pricingPort.validatePromoCode(
            promoCode,
            cart.totals().subtotal()
        );

        if (validationResult == null || !validationResult.available()) {
            log.atWarn()
                .addKeyValue("svc", SERVICE)
                .addKeyValue("uc", USE_CASE)
                .addKeyValue("block", "BA-CART-PROMO-02")
                .addKeyValue("state", "UNAVAILABLE")
                .addKeyValue("promoCode", promoCode)
                .log("Pricing service unavailable for promo validation");
            return ApplyPromoResult.unavailable();
        }

        boolean valid = validationResult.valid();
        Money discount = validationResult.discountAmount();
        if (discount == null) {
            discount = Money.zero(cart.totals().subtotal().getCurrency());
        }

        log.atInfo()
            .addKeyValue("svc", SERVICE)
            .addKeyValue("uc", USE_CASE)
            .addKeyValue("block", "BA-CART-PROMO-02")
            .addKeyValue("state", valid ? "VALID" : "INVALID")
            .addKeyValue("promoCode", promoCode)
            .addKeyValue("discountAmount", discount)
            .addKeyValue("errorCode", validationResult.errorCode())
            .log("Promo code validation result");

        if (!valid) {
            String errorCode = mapPromoErrorCode(validationResult.errorCode());
            String errorMessage = validationResult.errorMessage() != null
                ? validationResult.errorMessage()
                : "Promo code invalid";
            return ApplyPromoResult.failure(errorCode, errorMessage);
        }

        // BA-CART-PROMO-03: Apply discount to cart
        AppliedPromoCode appliedPromo = new AppliedPromoCode(
            promoCode,
            discount,
            validationResult.errorMessage(), // Can be description for valid codes
            Instant.now()
        );

        // BA-CART-PROMO-04: Recalculate totals
        cart.applyPromoCode(appliedPromo, totalsCalculator, null);

        log.atInfo()
            .addKeyValue("svc", SERVICE)
            .addKeyValue("uc", USE_CASE)
            .addKeyValue("block", "BA-CART-PROMO-05")
            .addKeyValue("state", "APPLIED")
            .addKeyValue("promoCode", promoCode)
            .addKeyValue("discount", appliedPromo.discountAmount())
            .addKeyValue("newTotal", cart.totals().total())
            .log("Promo code applied successfully");

        return ApplyPromoResult.success(appliedPromo);
    }

    /**
     * Removes promo code from cart.
     *
     * @param cart the cart to remove promo from
     * @return the removed promo code or null if none was applied
     */
    public AppliedPromoCode removePromoCode(Cart cart) {
        AppliedPromoCode existing = cart.appliedPromoCode();

        if (existing == null) {
            log.atDebug()
                .addKeyValue("svc", SERVICE)
                .addKeyValue("uc", USE_CASE)
                .addKeyValue("block", "BA-CART-PROMO-REMOVE")
                .addKeyValue("state", "NONE_APPLIED")
                .addKeyValue("cartId", cart.cartId().toString())
                .log("No promo code to remove");
            return null;
        }

        cart.removePromoCode(totalsCalculator, null);

        log.atInfo()
            .addKeyValue("svc", SERVICE)
            .addKeyValue("uc", USE_CASE)
            .addKeyValue("block", "BA-CART-PROMO-REMOVE")
            .addKeyValue("state", "REMOVED")
            .addKeyValue("cartId", cart.cartId().toString())
            .addKeyValue("removedCode", existing.code())
            .addKeyValue("removedDiscount", existing.discountAmount())
            .log("Promo code removed");

        return existing;
    }

    /**
     * Validates a promo code without applying it.
     *
     * @param promoCode the code to validate
     * @param subtotal the cart subtotal for minimum checks
     * @return validation result
     */
    public PromoValidationSummary validatePromoCode(String promoCode, Money subtotal) {
        log.atDebug()
            .addKeyValue("svc", SERVICE)
            .addKeyValue("uc", USE_CASE)
            .addKeyValue("block", "BA-CART-PROMO-VALIDATE")
            .addKeyValue("state", "START")
            .addKeyValue("promoCode", promoCode)
            .addKeyValue("subtotal", subtotal)
            .log("Validating promo code");

        PricingPort.PromoValidationResult result = pricingPort.validatePromoCode(promoCode, subtotal);

        if (result == null || !result.available()) {
            return PromoValidationSummary.unavailable();
        }

        if (!result.valid()) {
            return PromoValidationSummary.invalid(
                mapPromoErrorCode(result.errorCode()),
                result.errorMessage()
            );
        }

        return PromoValidationSummary.valid(result.discountAmount());
    }

    private String mapPromoErrorCode(String errorCode) {
        if ("ERR-PROMO-MIN-SUBTOTAL".equals(errorCode)) {
            return "ERR-CART-PROMO-MIN-NOT-MET";
        }
        return "ERR-CART-PROMO-INVALID";
    }

    /**
     * Summary of promo code validation.
     */
    public record PromoValidationSummary(
        boolean available,
        boolean valid,
        Money discountAmount,
        String errorCode,
        String errorMessage
    ) {
        public static PromoValidationSummary unavailable() {
            return new PromoValidationSummary(false, false, null, "ERR-CART-PRICING-UNAVAILABLE", "Pricing service unavailable");
        }

        public static PromoValidationSummary valid(Money discount) {
            return new PromoValidationSummary(true, true, discount, null, null);
        }

        public static PromoValidationSummary invalid(String errorCode, String errorMessage) {
            return new PromoValidationSummary(true, false, null, errorCode, errorMessage);
        }
    }
}
