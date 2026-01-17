package com.kanokna.pricing.adapters.out.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.pricing.application.dto.CalculateQuoteCommand;
import com.kanokna.pricing.domain.model.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuoteCacheIntegrationTest {

    private static final GenericContainer<?> redis = new GenericContainer<>(
        DockerImageName.parse("redis:7-alpine")
    ).withExposedPorts(6379);

    private static LettuceConnectionFactory connectionFactory;
    private static StringRedisTemplate redisTemplate;

    private QuoteRedisCache cache;

    @BeforeAll
    static void setupRedis() {
        if (!redis.isRunning()) {
            redis.start();
        }
        connectionFactory = new LettuceConnectionFactory(redis.getHost(), redis.getMappedPort(6379));
        connectionFactory.afterPropertiesSet();
        redisTemplate = new StringRedisTemplate(connectionFactory);
        redisTemplate.afterPropertiesSet();
    }

    @AfterAll
    static void tearDownRedis() {
        if (connectionFactory != null) {
            connectionFactory.destroy();
        }
        if (redis.isRunning()) {
            redis.stop();
        }
    }

    @BeforeEach
    void setUp() {
        cache = new QuoteRedisCache(redisTemplate, new ObjectMapper().findAndRegisterModules());
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    @Test
    @DisplayName("TC-PRC-011: Quote cached and retrieved on repeat request (Redis)")
    void quoteCachedAndRetrieved() {
        PriceBook priceBook = priceBook();
        CalculateQuoteCommand command = command();
        Quote quote = quote();

        cache.put(priceBook, command, quote, 5);

        Quote cached = cache.get(priceBook, command).orElse(null);

        assertNotNull(cached);
        assertEquals(quote.getQuoteId(), cached.getQuoteId());
        assertEquals(quote.getTotal().getAmount(), cached.getTotal().getAmount());
        assertEquals(quote.getOptionPremiums().size(), cached.getOptionPremiums().size());
        assertEquals(quote.getDecisionTrace().size(), cached.getDecisionTrace().size());
    }

    @Test
    @DisplayName("Evict by product template removes cached quotes")
    void evictByProductTemplateRemovesEntries() {
        PriceBook priceBook = priceBook();
        CalculateQuoteCommand command = command();
        Quote quote = quote();

        cache.put(priceBook, command, quote, 5);
        cache.evictByProductTemplateId(priceBook.getProductTemplateId());

        assertTrue(cache.get(priceBook, command).isEmpty());
    }

    private PriceBook priceBook() {
        BasePriceEntry basePriceEntry = BasePriceEntry.of(
            "WINDOW-STD",
            new BigDecimal("1000"),
            new BigDecimal("0.25"),
            null
        );
        PriceBook priceBook = PriceBook.create(
            PriceBookId.generate(),
            "WINDOW-STD",
            "RUB",
            basePriceEntry,
            "tester"
        );
        priceBook.addOptionPremium(
            OptionPremium.absolute("OPT-A", "Handle", Money.of(new BigDecimal("50"), "RUB"))
        );
        priceBook.publish();
        return priceBook;
    }

    private CalculateQuoteCommand command() {
        CalculateQuoteCommand command = new CalculateQuoteCommand();
        command.setProductTemplateId("WINDOW-STD");
        command.setWidthCm(new BigDecimal("100"));
        command.setHeightCm(new BigDecimal("100"));
        command.setResolvedBom(List.of("OPT-A", "OPT-B"));
        command.setCurrency("RUB");
        command.setPromoCode("PROMO10");
        command.setRegion("RU");
        return command;
    }

    private Quote quote() {
        return Quote.builder()
            .quoteId(QuoteId.generate())
            .productTemplateId("WINDOW-STD")
            .basePrice(Money.of(new BigDecimal("1000"), "RUB"))
            .optionPremiums(List.of(
                PremiumLine.of("OPT-A", "Handle", Money.of(new BigDecimal("50"), "RUB"))
            ))
            .discount(Money.of(new BigDecimal("100"), "RUB"))
            .subtotal(Money.of(new BigDecimal("950"), "RUB"))
            .tax(Money.of(new BigDecimal("190"), "RUB"))
            .total(Money.of(new BigDecimal("1140"), "RUB"))
            .validUntil(Instant.now().plusSeconds(300))
            .decisionTrace(List.of(
                PricingDecision.of("BA-PRC-CALC-02", "BASE_PRICE", "eventType=PRICING_STEP decision=CALCULATED keyValues=area_m2=1")
            ))
            .build();
    }
}
