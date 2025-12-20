package com.kanokna.pricing_service.adapters.out.persistence;

import com.kanokna.pricing_service.adapters.out.persistence.jpa.PriceBookJpaEntity;
import com.kanokna.pricing_service.adapters.out.persistence.jpa.PriceBookSpringRepository;
import com.kanokna.pricing_service.application.port.out.PriceBookRepository;
import com.kanokna.pricing_service.domain.model.PriceBook;
import com.kanokna.pricing_service.domain.model.PriceBookStatus;
import com.kanokna.shared.core.Id;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Currency;
import java.util.Optional;

@Component
public class PriceBookJpaAdapter implements PriceBookRepository {

    private final PriceBookSpringRepository repository;

    public PriceBookJpaAdapter(PriceBookSpringRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PriceBook> findActiveById(Id priceBookId) {
        return repository.findById(priceBookId.value())
            .filter(entity -> PriceBookStatus.valueOf(entity.getStatus()) == PriceBookStatus.ACTIVE)
            .map(PriceBookJpaMapper::toDomain);
    }

    @Override
    public Optional<PriceBook> findActiveByRegionAndCurrency(String region, Currency currency) {
        return repository.findFirstByRegionIgnoreCaseAndCurrencyAndStatus(region, currency.getCurrencyCode(), PriceBookStatus.ACTIVE.name())
            .map(PriceBookJpaMapper::toDomain);
    }

    @Transactional
    public PriceBook save(PriceBook priceBook) {
        PriceBookJpaEntity entity = PriceBookJpaMapper.toEntity(priceBook);
        repository.save(entity);
        return priceBook;
    }
}
