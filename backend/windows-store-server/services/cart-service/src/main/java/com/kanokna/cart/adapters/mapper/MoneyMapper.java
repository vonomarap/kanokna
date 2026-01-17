package com.kanokna.cart.adapters.mapper;

import com.kanokna.shared.money.Currency;
import com.kanokna.shared.money.Money;
import org.mapstruct.Mapper;

import java.math.BigDecimal;

/**
 * MapStruct mapper for Money value object conversions.
 */
@Mapper(componentModel = "spring")
public interface MoneyMapper {

    default Money toMoney(BigDecimal amount, String currency) {
        if (amount == null) {
            return null;
        }
        Currency cur = currency != null ? Currency.valueOf(currency) : Currency.RUB;
        return Money.of(amount, cur);
    }

    default BigDecimal toAmount(Money money) {
        return money != null ? money.getAmount() : null;
    }

    default String toCurrencyCode(Money money) {
        return money != null ? money.getCurrency().name() : null;
    }
}
