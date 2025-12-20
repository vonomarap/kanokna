package com.kanokna.pricing_service.adapters.out.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "price_book")
@DynamicUpdate
public class PriceBookJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String region;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private long version;

    @Lob
    @Column(name = "base_prices_json", nullable = false)
    private String basePricesJson;

    @Lob
    @Column(name = "option_premiums_json", nullable = false)
    private String optionPremiumsJson;

    protected PriceBookJpaEntity() {
    }

    public PriceBookJpaEntity(String id, String region, String currency, String status, long version, String basePricesJson, String optionPremiumsJson) {
        this.id = id;
        this.region = region;
        this.currency = currency;
        this.status = status;
        this.version = version;
        this.basePricesJson = basePricesJson;
        this.optionPremiumsJson = optionPremiumsJson;
    }

    public String getId() {
        return id;
    }

    public String getRegion() {
        return region;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStatus() {
        return status;
    }

    public long getVersion() {
        return version;
    }

    public String getBasePricesJson() {
        return basePricesJson;
    }

    public String getOptionPremiumsJson() {
        return optionPremiumsJson;
    }
}
