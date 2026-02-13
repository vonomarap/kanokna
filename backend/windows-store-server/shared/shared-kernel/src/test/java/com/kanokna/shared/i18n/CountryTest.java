package com.kanokna.shared.i18n;

import com.kanokna.shared.money.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Country")
class CountryTest {

    @Test
    @DisplayName("GB default currency is GBP")
    void gbDefaultCurrencyIsGbp() {
        assertThat(Country.GB.getDefaultCurrency()).isEqualTo(Currency.GBP);
    }

    @Test
    @DisplayName("Existing default currencies unchanged")
    void existingDefaultCurrenciesUnchanged() {
        assertThat(Country.RU.getDefaultCurrency()).isEqualTo(Currency.RUB);
        assertThat(Country.DE.getDefaultCurrency()).isEqualTo(Currency.EUR);
        assertThat(Country.FR.getDefaultCurrency()).isEqualTo(Currency.EUR);
        assertThat(Country.US.getDefaultCurrency()).isEqualTo(Currency.USD);
    }
}

