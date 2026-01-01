package com.kanokna.pricing_service.domain.service;

import com.kanokna.pricing_service.domain.exception.InvalidPromoCodeException;
import com.kanokna.pricing_service.domain.model.Campaign;
import com.kanokna.pricing_service.domain.model.Money;
import com.kanokna.pricing_service.domain.model.PricingDecision;
import com.kanokna.pricing_service.domain.model.PromoCode;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Domain service for calculating and applying discounts.
 * Implements DEC-PRICING-DISCOUNT-PRECEDENCE: Campaign first, promo second, 30% cap.
 */
public class DiscountService {
    private static final BigDecimal MAX_DISCOUNT_PERCENT = new BigDecimal("30");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    public Money calculateTotalDiscount(Money subtotal, List<Campaign> campaigns,
                                       PromoCode promoCode, List<PricingDecision> decisionTrace) {
        Money campaignDiscount = applyBestCampaign(subtotal, campaigns, decisionTrace);
        Money promoDiscount = applyPromoCode(subtotal, promoCode, decisionTrace);

        Money totalDiscount = campaignDiscount.add(promoDiscount);

        Money maxAllowedDiscount = subtotal.multiply(MAX_DISCOUNT_PERCENT.divide(ONE_HUNDRED, 4, RoundingMode.HALF_UP));
        if (totalDiscount.isGreaterThan(maxAllowedDiscount)) {
            return maxAllowedDiscount;
        }

        return totalDiscount;
    }

    private Money applyBestCampaign(Money subtotal, List<Campaign> campaigns,
                                   List<PricingDecision> decisionTrace) {
        if (campaigns == null || campaigns.isEmpty()) {
            decisionTrace.add(PricingDecision.of(
                "BA-PRC-CALC-04",
                "CAMPAIGN",
                "eventType=PRICING_STEP decision=NONE keyValues=campaignId=NONE,discount_rub=0"
            ));
            return Money.zero(subtotal.getCurrency());
        }

        Campaign bestCampaign = campaigns.stream()
            .max(Comparator.comparing(c -> c.applyDiscount(subtotal).getAmount()))
            .orElse(null);

        if (bestCampaign == null) {
            decisionTrace.add(PricingDecision.of(
                "BA-PRC-CALC-04",
                "CAMPAIGN",
                "eventType=PRICING_STEP decision=NONE keyValues=campaignId=NONE,discount_rub=0"
            ));
            return Money.zero(subtotal.getCurrency());
        }

        Money discount = bestCampaign.applyDiscount(subtotal);
        decisionTrace.add(PricingDecision.of(
            "BA-PRC-CALC-04",
            "CAMPAIGN",
            String.format("eventType=PRICING_STEP decision=APPLIED keyValues=campaignId=%s,discount_rub=%s",
                bestCampaign.getId(), discount.getAmount().toPlainString())
        ));

        return discount;
    }

    private Money applyPromoCode(Money subtotal, PromoCode promoCode,
                                List<PricingDecision> decisionTrace) {
        if (promoCode == null) {
            decisionTrace.add(PricingDecision.of(
                "BA-PRC-CALC-05",
                "PROMO",
                "eventType=PRICING_STEP decision=NONE keyValues=promoCode=NONE,discount_rub=0"
            ));
            return Money.zero(subtotal.getCurrency());
        }

        Instant now = Instant.now();
        if (!promoCode.isValid(now, subtotal)) {
            decisionTrace.add(PricingDecision.of(
                "BA-PRC-CALC-05",
                "PROMO",
                String.format("eventType=PRICING_STEP decision=INVALID keyValues=promoCode=%s,discount_rub=0",
                    promoCode.getCode())
            ));
            throw new InvalidPromoCodeException(promoCode.getCode(), "Promo code invalid or expired");
        }

        Money discount = promoCode.calculateDiscount(subtotal);
        decisionTrace.add(PricingDecision.of(
            "BA-PRC-CALC-05",
            "PROMO",
            String.format("eventType=PRICING_STEP decision=APPLIED keyValues=promoCode=%s,discount_rub=%s",
                promoCode.getCode(), discount.getAmount().toPlainString())
        ));

        return discount;
    }
}