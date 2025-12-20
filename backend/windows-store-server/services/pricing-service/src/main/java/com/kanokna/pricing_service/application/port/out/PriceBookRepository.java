package com.kanokna.pricing_service.application.port.out;

import com.kanokna.pricing_service.domain.model.PriceBook;
import com.kanokna.shared.core.Id;

import java.util.Currency;
import java.util.Optional;

public interface PriceBookRepository {

    Optional<PriceBook> findActiveById(Id priceBookId);

    Optional<PriceBook> findActiveByRegionAndCurrency(String region, Currency currency);
}
