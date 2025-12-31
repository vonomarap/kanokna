package com.kanokna.pricing.domain.service;

import com.kanokna.pricing.domain.model.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Domain service for calculating and applying discounts.
 * Implements DEC-PRICING-DISCOUNT-PRECEDENCE: Campaign first, promo second, 30% cap.
 */
public class DiscountService {

    private static final BigDecimal MAX_DISCOUNT_PERCENT = new BigDecimal("30");

    public Money calculateTotalDiscount(Money subtotal, List<Campaign> campaigns,
                                       PromoCode promoCode, List<PricingDecision> decisionTrace) {
        String currency = subtotal.getCurrency();
        Money totalDiscount = Money.zero(currency);

        // BA-PRC-CALC-04: Apply campaign discount (best campaign by priority)
        Money campaignDiscount = applyBestCampaign(subtotal, campaigns, decisionTrace);
        totalDiscount = totalDiscount.add(campaignDiscount);

        // BA-PRC-CALC-05: Apply promo code discount
        Money promoDiscount = applyPromoCode(subtotal, promoCode, decisionTrace);
        totalDiscount = totalDiscount.add(promoDiscount);

        // Enforce 30% cap
        Money maxAllowedDiscount = subtotal.multiply(MAX_DISCOUNT_PERCENT.divide(new BigDecimal("100")));
        if (totalDiscount.isGreaterThan(maxAllowedDiscount)) {
            decisionTrace.add(PricingDecision.of(
                "BA-PRC-CALC-06",
                "DISCOUNT_CAP_APPLIED",
                String.format("original_discount=%.2f, capped_to=%.2f (30%% of subtotal)",
                    totalDiscount.getAmount(), maxAllowedDiscount.getAmount())
            ));
            return maxAllowedDiscount;
        }

        return totalDiscount;
    }

    private Money applyBestCampaign(Money subtotal, List<Campaign> campaigns,
                                   List<PricingDecision> decisionTrace) {
        if (campaigns == null || campaigns.isEmpty()) {
            decisionTrace.add(PricingDecision.of(
                "BA-PRC-CALC-04",
                "CAMPAIGN_DISCOUNT",
                "decision=NONE (no active campaigns)"
            ));
            return Money.zero(subtotal.getCurrency());
        }

        // Select best campaign by highest discount amount
        Campaign bestCampaign = campaigns.stream()
            .max(Comparator.comparing(c -> c.applyDiscount(subtotal).getAmount()))
            .orElse(null);

        if (bestCampaign == null) {
            return Money.zero(subtotal.getCurrency());
        }

        Money discount = bestCampaign.applyDiscount(subtotal);
        decisionTrace.add(PricingDecision.of(
            "BA-PRC-CALC-04",
            "CAMPAIGN_DISCOUNT",
            String.format("decision=APPLIED, campaignId=%s, discount=%.2f %s",
                bestCampaign.getId(), discount.getAmount(), subtotal.getCurrency())
        ));

        return discount;
    }

    private Money applyPromoCode(Money subtotal, PromoCode promoCode,
                                List<PricingDecision> decisionTrace) {
        if (promoCode == null) {
            decisionTrace.add(PricingDecision.of(
                "BA-PRC-CALC-05",
                "PROMO_CODE_DISCOUNT",
                "decision=NONE (no promo code provided)"
            ));
            return Money.zero(subtotal.getCurrency());
        }

        // BA-PROMO-VAL-01 through BA-PROMO-VAL-03: Validate promo code
        if (!promoCode.isValid(Instant.now(), subtotal)) {
            decisionTrace.add(PricingDecision.of(
                "BA-PRC-CALC-05",
                "PROMO_CODE_DISCOUNT",
                String.format("decision=INVALID, code=%s", promoCode.getCode())
            ));
            return Money.zero(subtotal.getCurrency());
        }

        // BA-PROMO-VAL-04: Calculate discount
        Money discount = promoCode.calculateDiscount(subtotal);
        decisionTrace.add(PricingDecision.of(
            "BA-PRC-CALC-05",
            "PROMO_CODE_DISCOUNT",
            String.format("decision=APPLIED, code=%s, discount=%.2f %s",
                promoCode.getCode(), discount.getAmount(), subtotal.getCurrency())
        ));

        return discount;
    }
}
