package com.kanokna.pricing_service.domain.service;

import com.kanokna.pricing_service.domain.exception.InvalidPromoCodeException;
import com.kanokna.pricing_service.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PriceCalculationServiceTest {

    private static final String PRODUCT_ID = "WINDOW-STD";
    private static final String CURRENCY = "RUB";

    private PriceCalculationService service;

    @BeforeEach
    void setUp() {
        service = new PriceCalculationService(new DiscountService(), new TaxCalculationService(), new RoundingService());
    }

    @Test
    @DisplayName("TC-FUNC-CALC-001 / TC-PRC-012: Valid configuration returns quote with decision trace")
    void validConfigurationReturnsQuote() {
        PriceBook priceBook = priceBook(new BigDecimal("1000"), new BigDecimal("0.25"), List.of());

        Quote quote = service.calculateQuote(
            priceBook,
            List.of(),
            new BigDecimal("100"),
            new BigDecimal("100"),
            List.of(),
            null,
            taxRule(new BigDecimal("20")),
            5
        );

        assertNotNull(quote.getQuoteId());
        assertAmountEquals(new BigDecimal("1000"), quote.getBasePrice());
        assertAmountEquals(new BigDecimal("0"), quote.getDiscount());
        assertAmountEquals(new BigDecimal("200"), quote.getTax());
        assertAmountEquals(new BigDecimal("1200"), quote.getTotal());

        List<String> steps = quote.getDecisionTrace().stream().map(PricingDecision::getStep).toList();
        assertTrue(steps.containsAll(List.of(
            "BA-PRC-CALC-01",
            "BA-PRC-CALC-02",
            "BA-PRC-CALC-03",
            "BA-PRC-CALC-04",
            "BA-PRC-CALC-05",
            "BA-PRC-CALC-06",
            "BA-PRC-CALC-07",
            "BA-PRC-CALC-08",
            "BA-PRC-CALC-99"
        )));
    }

    @Test
    @DisplayName("TC-FUNC-CALC-002 / TC-PRC-001: Area calculated from dimensions")
    void areaCalculatedCorrectly() {
        PriceBook priceBook = priceBook(new BigDecimal("100"), new BigDecimal("0.25"), List.of());

        Quote quote = service.calculateQuote(
            priceBook,
            List.of(),
            new BigDecimal("200"),
            new BigDecimal("150"),
            List.of(),
            null,
            taxRule(new BigDecimal("0")),
            5
        );

        assertAmountEquals(new BigDecimal("300"), quote.getBasePrice());
    }

    @Test
    @DisplayName("TC-FUNC-CALC-003: Minimum area charge applied for small configurations")
    void minimumAreaChargeApplied() {
        PriceBook priceBook = priceBook(new BigDecimal("100"), new BigDecimal("1.0"), List.of());

        Quote quote = service.calculateQuote(
            priceBook,
            List.of(),
            new BigDecimal("50"),
            new BigDecimal("50"),
            List.of(),
            null,
            taxRule(new BigDecimal("0")),
            5
        );

        assertAmountEquals(new BigDecimal("100"), quote.getBasePrice());
    }

    @Test
    @DisplayName("TC-FUNC-CALC-004 / TC-PRC-002: Multiple ABSOLUTE premiums summed correctly")
    void absolutePremiumsSummed() {
        OptionPremium p1 = OptionPremium.absolute("OPT-A", "Handle", Money.of(new BigDecimal("100"), CURRENCY));
        OptionPremium p2 = OptionPremium.absolute("OPT-B", "Lock", Money.of(new BigDecimal("200"), CURRENCY));
        PriceBook priceBook = priceBook(new BigDecimal("1000"), new BigDecimal("0.25"), List.of(p1, p2));

        Quote quote = service.calculateQuote(
            priceBook,
            List.of("OPT-A", "OPT-B"),
            new BigDecimal("100"),
            new BigDecimal("100"),
            List.of(),
            null,
            taxRule(new BigDecimal("0")),
            5
        );

        assertEquals(2, quote.getOptionPremiums().size());
        assertAmountEquals(new BigDecimal("1300"), quote.getSubtotal());
    }

    @Test
    @DisplayName("TC-FUNC-CALC-005 / TC-PRC-003: Percentage premiums calculated on base price only")
    void percentagePremiumUsesBasePriceOnly() {
        OptionPremium absolute = OptionPremium.absolute("OPT-A", "Handle", Money.of(new BigDecimal("200"), CURRENCY));
        OptionPremium percent = OptionPremium.percentage("OPT-B", "Glass", new BigDecimal("10"));
        PriceBook priceBook = priceBook(new BigDecimal("1000"), new BigDecimal("0.25"), List.of(absolute, percent));

        Quote quote = service.calculateQuote(
            priceBook,
            List.of("OPT-A", "OPT-B"),
            new BigDecimal("100"),
            new BigDecimal("100"),
            List.of(),
            null,
            taxRule(new BigDecimal("0")),
            5
        );

        Money percentAmount = quote.getOptionPremiums().stream()
            .filter(line -> line.getOptionId().equals("OPT-B"))
            .findFirst()
            .orElseThrow()
            .getAmount();

        assertAmountEquals(new BigDecimal("100"), percentAmount);
        assertAmountEquals(new BigDecimal("1300"), quote.getSubtotal());
    }

    @Test
    @DisplayName("TC-FUNC-CALC-007: Promo code usage limits enforced")
    void promoCodeUsageLimitValidated() {
        PriceBook priceBook = priceBook(new BigDecimal("1000"), new BigDecimal("0.25"), List.of());
        PromoCode promo = promoCodePercent("WELCOME10", new BigDecimal("10"), 1, 1);

        assertThrows(InvalidPromoCodeException.class, () -> service.calculateQuote(
            priceBook,
            List.of(),
            new BigDecimal("100"),
            new BigDecimal("100"),
            List.of(),
            promo,
            taxRule(new BigDecimal("0")),
            5
        ));
    }

    @Test
    @DisplayName("TC-FUNC-CALC-008 / TC-PRC-006: 30% discount cap enforced")
    void discountCapEnforced() {
        PriceBook priceBook = priceBook(new BigDecimal("1000"), new BigDecimal("0.25"), List.of());
        Campaign campaign = activeCampaignPercent(new BigDecimal("25"));
        PromoCode promo = promoCodePercent("STACK20", new BigDecimal("20"), null, 0);

        Quote quote = service.calculateQuote(
            priceBook,
            List.of(),
            new BigDecimal("100"),
            new BigDecimal("100"),
            List.of(campaign),
            promo,
            taxRule(new BigDecimal("0")),
            5
        );

        assertAmountEquals(new BigDecimal("300"), quote.getDiscount());
    }

    @Test
    @DisplayName("TC-FUNC-CALC-009 / TC-PRC-007: Tax calculated on discounted subtotal")
    void taxCalculatedOnDiscountedSubtotal() {
        PriceBook priceBook = priceBook(new BigDecimal("1000"), new BigDecimal("0.25"), List.of());
        Campaign campaign = activeCampaignPercent(new BigDecimal("10"));

        Quote quote = service.calculateQuote(
            priceBook,
            List.of(),
            new BigDecimal("100"),
            new BigDecimal("100"),
            List.of(campaign),
            null,
            taxRule(new BigDecimal("20")),
            5
        );

        assertAmountEquals(new BigDecimal("180"), quote.getTax());
    }

    @Test
    @DisplayName("TC-FUNC-CALC-010: Rounding applied consistently")
    void roundingAppliedConsistently() {
        PriceBook priceBook = priceBook(new BigDecimal("100.005"), new BigDecimal("0.25"), List.of());

        Quote quote = service.calculateQuote(
            priceBook,
            List.of(),
            new BigDecimal("100"),
            new BigDecimal("100"),
            List.of(),
            null,
            taxRule(new BigDecimal("0")),
            5
        );

        assertAmountEquals(new BigDecimal("100.01"), quote.getTotal());
    }

    @Test
    @DisplayName("TC-PRC-010: Expired campaign not applied")
    void expiredCampaignNotApplied() {
        PriceBook priceBook = priceBook(new BigDecimal("1000"), new BigDecimal("0.25"), List.of());
        Campaign campaign = expiredCampaignPercent(new BigDecimal("15"));

        Quote quote = service.calculateQuote(
            priceBook,
            List.of(),
            new BigDecimal("100"),
            new BigDecimal("100"),
            List.of(campaign),
            null,
            taxRule(new BigDecimal("0")),
            5
        );

        assertAmountEquals(new BigDecimal("0"), quote.getDiscount());
    }

    private PriceBook priceBook(BigDecimal pricePerM2, BigDecimal minArea, List<OptionPremium> premiums) {
        BasePriceEntry basePriceEntry = BasePriceEntry.of(PRODUCT_ID, pricePerM2, minArea, null);
        PriceBook priceBook = PriceBook.create(PriceBookId.generate(), PRODUCT_ID, CURRENCY, basePriceEntry, "tester");
        for (OptionPremium premium : premiums) {
            priceBook.addOptionPremium(premium);
        }
        priceBook.publish();
        return priceBook;
    }

    private TaxRule taxRule(BigDecimal ratePercent) {
        return TaxRule.createVAT(TaxRuleId.generate(), "RU", "Russia", ratePercent);
    }

    private Campaign activeCampaignPercent(BigDecimal percent) {
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

    private PromoCode promoCodePercent(String code, BigDecimal percent, Integer usageLimit, int usageCount) {
        Instant now = Instant.now();
        return PromoCode.restore(
            PromoCodeId.generate(),
            code,
            "Promo",
            DiscountType.PERCENTAGE,
            percent,
            null,
            null,
            usageLimit,
            usageCount,
            now.minusSeconds(3600),
            now.plusSeconds(3600),
            true,
            now,
            "tester"
        );
    }

    private void assertAmountEquals(BigDecimal expected, Money actual) {
        assertEquals(0, expected.compareTo(actual.getAmount()));
    }
}
