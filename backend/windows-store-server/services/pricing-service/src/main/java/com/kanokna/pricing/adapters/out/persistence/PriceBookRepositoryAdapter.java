package com.kanokna.pricing.adapters.out.persistence;

import org.springframework.stereotype.Component;
import com.kanokna.pricing.application.port.out.PriceBookRepository;
import com.kanokna.pricing.domain.model.PriceBook;
import com.kanokna.pricing.domain.model.PriceBookStatus;

import java.util.Optional;

/**
 * JPA adapter for price books.
 */
@Component
public class PriceBookRepositoryAdapter implements PriceBookRepository {
    private final PriceBookJpaRepository repository;
    private final PriceBookMapper mapper;

    public PriceBookRepositoryAdapter(PriceBookJpaRepository repository, PriceBookMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<PriceBook> findActiveByProductTemplateId(String productTemplateId) {
        return repository.findFirstByProductTemplateIdAndStatus(productTemplateId, PriceBookStatus.ACTIVE)
            .map(mapper::toDomain);
    }

    @Override
    public PriceBook save(PriceBook priceBook) {
        return mapper.toDomain(repository.save(mapper.toEntity(priceBook)));
    }
}
