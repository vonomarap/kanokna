package com.kanokna.pricing_service.adapters.out.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kanokna.pricing_service.adapters.out.persistence.jpa.PriceBookJpaEntity;
import com.kanokna.pricing_service.domain.exception.PricingDomainException;
import com.kanokna.pricing_service.domain.model.OptionPremiumKey;
import com.kanokna.pricing_service.domain.model.PriceBook;
import com.kanokna.pricing_service.domain.model.PriceBookStatus;
import com.kanokna.shared.core.Id;
import com.kanokna.shared.money.Money;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Map;
import java.util.stream.Collectors;

public final class PriceBookJpaMapper {

    private static final ObjectMapper mapper = new ObjectMapper();

    private PriceBookJpaMapper() {}

    public static PriceBook toDomain(PriceBookJpaEntity entity) {
        Map<String, Money> basePrices = readBasePrices(entity.getBasePricesJson(), Currency.getInstance(entity.getCurrency()));
        Map<OptionPremiumKey, Money> premiums = readOptionPremiums(entity.getOptionPremiumsJson(), Currency.getInstance(entity.getCurrency()));
        return new PriceBook(
            Id.of(entity.getId()),
            entity.getRegion(),
            Currency.getInstance(entity.getCurrency()),
            PriceBookStatus.valueOf(entity.getStatus()),
            entity.getVersion(),
            basePrices,
            premiums
        );
    }

    public static PriceBookJpaEntity toEntity(PriceBook priceBook) {
        String baseJson = writeBasePrices(priceBook);
        String optionJson = writeOptionPremiums(priceBook);
        return new PriceBookJpaEntity(
            priceBook.id().value(),
            priceBook.region(),
            priceBook.currency().getCurrencyCode(),
            priceBook.status().name(),
            priceBook.version(),
            baseJson,
            optionJson
        );
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Money> readBasePrices(String json, Currency currency) {
        try {
            Map<String, Double> parsed = mapper.readValue(json, Map.class);
            return parsed.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> Money.of(BigDecimal.valueOf(e.getValue()), currency)));
        } catch (Exception ex) {
            throw new PricingDomainException("Failed to read base prices json", ex);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<OptionPremiumKey, Money> readOptionPremiums(String json, Currency currency) {
        try {
            Map<String, Double> parsed = mapper.readValue(json, Map.class);
            return parsed.entrySet().stream()
                .collect(Collectors.toMap(
                    e -> {
                        String[] parts = e.getKey().split(":");
                        return new OptionPremiumKey(parts[0], parts[1]);
                    },
                    e -> Money.of(BigDecimal.valueOf(e.getValue()), currency)
                ));
        } catch (Exception ex) {
            throw new PricingDomainException("Failed to read option premiums json", ex);
        }
    }

    private static String writeBasePrices(PriceBook priceBook) {
        Map<String, BigDecimal> flat = priceBook.basePricesByItem().entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getAmount()));
        try {
            return mapper.writeValueAsString(flat);
        } catch (JsonProcessingException e) {
            throw new PricingDomainException("Failed to write base prices json", e);
        }
    }

    private static String writeOptionPremiums(PriceBook priceBook) {
        Map<String, BigDecimal> flat = priceBook.optionPremiums().entrySet().stream()
            .collect(Collectors.toMap(e -> e.getKey().asKey(), e -> e.getValue().getAmount()));
        try {
            return mapper.writeValueAsString(flat);
        } catch (JsonProcessingException e) {
            throw new PricingDomainException("Failed to write option premiums json", e);
        }
    }
}
