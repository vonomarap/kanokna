package com.kanokna.pricing_service.adapters.out.redis;

import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.pricing_service.application.dto.CalculateQuoteCommand;
import com.kanokna.pricing_service.application.port.out.QuoteCache;
import com.kanokna.pricing_service.domain.model.*;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * Redis cache adapter for quotes.
 */
@Component
public class QuoteRedisCache implements QuoteCache {
    private static final String QUOTE_ID_PREFIX = "quote:id:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public QuoteRedisCache(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<Quote> get(PriceBook priceBook, CalculateQuoteCommand command) {
        QuoteCacheKey key = QuoteCacheKey.from(priceBook, command);
        String json = redisTemplate.opsForValue().get(key.value());
        if (json == null) {
            return Optional.empty();
        }
        try {
            QuoteCacheEntry entry = objectMapper.readValue(json, QuoteCacheEntry.class);
            return Optional.of(entry.toDomain());
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    @Override
    public void put(PriceBook priceBook, CalculateQuoteCommand command, Quote quote, int ttlMinutes) {
        QuoteCacheKey key = QuoteCacheKey.from(priceBook, command);
        QuoteCacheEntry entry = QuoteCacheEntry.fromDomain(quote);
        try {
            String json = objectMapper.writeValueAsString(entry);
            redisTemplate.opsForValue().set(key.value(), json, ttlMinutes, TimeUnit.MINUTES);
            redisTemplate.opsForValue().set(QUOTE_ID_PREFIX + quote.getQuoteId(), json, ttlMinutes, TimeUnit.MINUTES);
        } catch (Exception ignored) {
        }
    }

    @Override
    public void evictByProductTemplateId(String productTemplateId) {
        String pattern = "quote:product:" + productTemplateId + ":*";
        var keys = redisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) {
            return;
        }
        redisTemplate.delete(keys);
    }

    private static class QuoteCacheEntry {
        public String quoteId;
        public String productTemplateId;
        public BigDecimal basePrice;
        public BigDecimal discount;
        public BigDecimal subtotal;
        public BigDecimal tax;
        public BigDecimal total;
        public String currency;
        public Instant validUntil;
        public List<PremiumEntry> optionPremiums = new ArrayList<>();
        public List<DecisionEntry> decisionTrace = new ArrayList<>();

        static QuoteCacheEntry fromDomain(Quote quote) {
            QuoteCacheEntry entry = new QuoteCacheEntry();
            entry.quoteId = quote.getQuoteId().toString();
            entry.productTemplateId = quote.getProductTemplateId();
            entry.basePrice = quote.getBasePrice().getAmount();
            entry.discount = quote.getDiscount().getAmount();
            entry.subtotal = quote.getSubtotal().getAmount();
            entry.tax = quote.getTax().getAmount();
            entry.total = quote.getTotal().getAmount();
            entry.currency = quote.getTotal().getCurrency();
            entry.validUntil = quote.getValidUntil();
            for (PremiumLine line : quote.getOptionPremiums()) {
                entry.optionPremiums.add(PremiumEntry.fromDomain(line));
            }
            for (PricingDecision decision : quote.getDecisionTrace()) {
                entry.decisionTrace.add(DecisionEntry.fromDomain(decision));
            }
            return entry;
        }

        Quote toDomain() {
            List<PremiumLine> premiums = new ArrayList<>();
            for (PremiumEntry entry : optionPremiums) {
                premiums.add(PremiumLine.of(entry.optionId, entry.optionName, Money.of(entry.amount, currency)));
            }
            List<PricingDecision> decisions = new ArrayList<>();
            for (DecisionEntry entry : decisionTrace) {
                decisions.add(PricingDecision.of(entry.step, entry.ruleApplied, entry.result));
            }
            return Quote.builder()
                .quoteId(QuoteId.of(quoteId))
                .productTemplateId(productTemplateId)
                .basePrice(Money.of(basePrice, currency))
                .optionPremiums(premiums)
                .discount(Money.of(discount, currency))
                .subtotal(Money.of(subtotal, currency))
                .tax(Money.of(tax, currency))
                .total(Money.of(total, currency))
                .validUntil(validUntil)
                .decisionTrace(decisions)
                .build();
        }
    }

    private static class PremiumEntry {
        public String optionId;
        public String optionName;
        public BigDecimal amount;

        static PremiumEntry fromDomain(PremiumLine line) {
            PremiumEntry entry = new PremiumEntry();
            entry.optionId = line.getOptionId();
            entry.optionName = line.getOptionName();
            entry.amount = line.getAmount().getAmount();
            return entry;
        }
    }

    private static class DecisionEntry {
        public String step;
        public String ruleApplied;
        public String result;

        static DecisionEntry fromDomain(PricingDecision decision) {
            DecisionEntry entry = new DecisionEntry();
            entry.step = decision.getStep();
            entry.ruleApplied = decision.getRuleApplied();
            entry.result = decision.getResult();
            return entry;
        }
    }
}
