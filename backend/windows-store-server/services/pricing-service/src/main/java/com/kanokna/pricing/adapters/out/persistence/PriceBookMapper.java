package com.kanokna.pricing.adapters.out.persistence;

import org.springframework.stereotype.Component;
import com.kanokna.pricing.domain.model.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Mapper between PriceBook aggregate and JPA entities.
 */
@Component
public class PriceBookMapper {

    public PriceBook toDomain(PriceBookJpaEntity entity) {
        Money minimumCharge = entity.getMinimumCharge() != null
            ? Money.of(entity.getMinimumCharge(), entity.getCurrency())
            : null;
        BasePriceEntry basePriceEntry = BasePriceEntry.of(
            entity.getProductTemplateId(),
            entity.getPricePerM2(),
            entity.getMinimumAreaM2(),
            minimumCharge
        );

        List<OptionPremium> premiums = new ArrayList<>();
        for (OptionPremiumJpaEntity premiumEntity : entity.getOptionPremiums()) {
            premiums.add(toDomain(premiumEntity, entity.getCurrency()));
        }

        return PriceBook.restore(
            PriceBookId.of(entity.getId()),
            entity.getProductTemplateId(),
            entity.getCurrency(),
            basePriceEntry,
            entity.getCreatedBy(),
            entity.getStatus(),
            entity.getVersion(),
            entity.getCreatedAt(),
            entity.getUpdatedAt(),
            premiums
        );
    }

    public PriceBookJpaEntity toEntity(PriceBook priceBook) {
        PriceBookJpaEntity entity = new PriceBookJpaEntity();
        entity.setId(priceBook.getId().getValue());
        entity.setProductTemplateId(priceBook.getProductTemplateId());
        entity.setCurrency(priceBook.getCurrency());
        entity.setPricePerM2(priceBook.getBasePriceEntry().getPricePerM2());
        entity.setMinimumAreaM2(priceBook.getBasePriceEntry().getMinimumAreaM2());
        entity.setMinimumCharge(priceBook.getBasePriceEntry().getMinimumCharge() != null
            ? priceBook.getBasePriceEntry().getMinimumCharge().getAmount()
            : null);
        entity.setStatus(priceBook.getStatus());
        entity.setVersion(priceBook.getVersion());
        entity.setCreatedAt(priceBook.getCreatedAt());
        entity.setCreatedBy(priceBook.getCreatedBy());
        entity.setUpdatedAt(priceBook.getUpdatedAt() != null ? priceBook.getUpdatedAt() : Instant.now());

        List<OptionPremiumJpaEntity> premiumEntities = new ArrayList<>();
        for (OptionPremium premium : priceBook.getOptionPremiums()) {
            OptionPremiumJpaEntity premiumEntity = toEntity(premium, entity, priceBook.getCurrency());
            premiumEntities.add(premiumEntity);
        }
        entity.setOptionPremiums(premiumEntities);

        return entity;
    }

    private OptionPremium toDomain(OptionPremiumJpaEntity entity, String currency) {
        if (entity.getPremiumType() == PremiumType.ABSOLUTE) {
            return OptionPremium.absolute(
                entity.getOptionId(),
                entity.getOptionName(),
                Money.of(entity.getAmount(), currency)
            );
        }
        return OptionPremium.percentage(
            entity.getOptionId(),
            entity.getOptionName(),
            entity.getAmount()
        );
    }

    private OptionPremiumJpaEntity toEntity(OptionPremium premium, PriceBookJpaEntity priceBook, String currency) {
        OptionPremiumJpaEntity entity = new OptionPremiumJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setPriceBook(priceBook);
        entity.setOptionId(premium.getOptionId());
        entity.setOptionName(premium.getOptionName());
        entity.setPremiumType(premium.getPremiumType());
        entity.setAmount(premium.getAmount());
        entity.setCreatedAt(Instant.now());
        return entity;
    }
}
