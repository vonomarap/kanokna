package com.kanokna.pricing_service.adapters.out.persistence;

import com.kanokna.pricing_service.domain.model.PremiumType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "option_premiums", schema = "pricing")
public class OptionPremiumJpaEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "price_book_id", nullable = false)
    private PriceBookJpaEntity priceBook;

    @Column(name = "option_id", nullable = false, length = 100)
    private String optionId;

    @Column(name = "option_name", nullable = false, length = 200)
    private String optionName;

    @Enumerated(EnumType.STRING)
    @Column(name = "premium_type", nullable = false, length = 20)
    private PremiumType premiumType;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected OptionPremiumJpaEntity() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public PriceBookJpaEntity getPriceBook() {
        return priceBook;
    }

    public void setPriceBook(PriceBookJpaEntity priceBook) {
        this.priceBook = priceBook;
    }

    public String getOptionId() {
        return optionId;
    }

    public void setOptionId(String optionId) {
        this.optionId = optionId;
    }

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public PremiumType getPremiumType() {
        return premiumType;
    }

    public void setPremiumType(PremiumType premiumType) {
        this.premiumType = premiumType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
