package com.kanokna.pricing.domain.service;

import com.kanokna.pricing.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiscountServiceTest {

    private static final String CURRENCY = "RUB";

    private DiscountService discountService;

    @BeforeEach
    void setUp() {
        discountService = new DiscountService();
    }

    @Test
    @DisplayName("TC-FUNC-CALC-006 / TC-PRC-004: Best campaign discount selected")
    void bestCampaignSelected() {
        Money subtotal = Money.of(new BigDecimal("1000"), CURRENCY);
        Campaign low = campaignPercent(new BigDecimal("5"));
        Campaign high = campaignPercent(new BigDecimal("20"));

        Money discount = discountService.calculateTotalDiscount(subtotal, List.of(low, high), null, new ArrayList<>());

        assertEquals(0, new BigDecimal("200").compareTo(discount.getAmount()));
    }

    @Test
    @DisplayName("TC-FUNC-CALC-008 / TC-PRC-006: 30% discount cap enforced")
    void capEnforced() {
        Money subtotal = Money.of(new BigDecimal("1000"), CURRENCY);
        Campaign campaign = campaignPercent(new BigDecimal("25"));
        PromoCode promo = promoPercent("STACK20", new BigDecimal("20"));

        Money discount = discountService.calculateTotalDiscount(subtotal, List.of(campaign), promo, new ArrayList<>());

        assertEquals(0, new BigDecimal("300").compareTo(discount.getAmount()));
    }

    @Test
    @DisplayName("TC-PRC-005: Valid promo code discount applied")
    void validPromoApplied() {
        Money subtotal = Money.of(new BigDecimal("1000"), CURRENCY);
        PromoCode promo = promoPercent("WELCOME10", new BigDecimal("10"));

        Money discount = discountService.calculateTotalDiscount(subtotal, List.of(), promo, new ArrayList<>());

        assertEquals(0, new BigDecimal("100").compareTo(discount.getAmount()));
    }

    @Test
    @DisplayName("TC-PRC-010: Expired campaign not applied")
    void expiredCampaignNotApplied() {
        Money subtotal = Money.of(new BigDecimal("1000"), CURRENCY);
        Campaign expired = expiredCampaignPercent(new BigDecimal("15"));

        Money discount = discountService.calculateTotalDiscount(subtotal, List.of(expired), null, new ArrayList<>());

        assertEquals(0, new BigDecimal("0").compareTo(discount.getAmount()));
    }

    private Campaign campaignPercent(BigDecimal percent) {
        Instant now = Instant.now();
        return Campaign.restore(
            CampaignId.generate(),
            "Campaign",
            null,
            CampaignRule.percentage(percent, null),
            Set.of(),
            now.minusSeconds(3600),
            now.plusSeconds(3600),
            CampaignStatus.ACTIVE,
            1,
            now,
            "tester"
        );
    }

    private Campaign expiredCampaignPercent(BigDecimal percent) {
        Instant now = Instant.now();
        return Campaign.restore(
            CampaignId.generate(),
            "Campaign",
            null,
            CampaignRule.percentage(percent, null),
            Set.of(),
            now.minusSeconds(7200),
            now.minusSeconds(3600),
            CampaignStatus.EXPIRED,
            1,
            now.minusSeconds(7200),
            "tester"
        );
    }

    private PromoCode promoPercent(String code, BigDecimal percent) {
        Instant now = Instant.now();
        return PromoCode.restore(
            PromoCodeId.generate(),
            code,
            "Promo",
            DiscountType.PERCENTAGE,
            percent,
            null,
            null,
            null,
            0,
            now.minusSeconds(3600),
            now.plusSeconds(3600),
            true,
            now,
            "tester"
        );
    }
}
