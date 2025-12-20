package com.kanokna.pricing_service.adapters.out.persistence.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "campaign")
public class CampaignJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String status;

    @Column(name = "percent_off", nullable = false, precision = 5, scale = 3)
    private BigDecimal percentOff;

    @Column(name = "starts_at")
    private Instant startsAt;

    @Column(name = "ends_at")
    private Instant endsAt;

    @Lob
    @Column(name = "conditions_json")
    private String conditionsJson;

    protected CampaignJpaEntity() {}

    public CampaignJpaEntity(String id, String name, String status, BigDecimal percentOff, Instant startsAt, Instant endsAt, String conditionsJson) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.percentOff = percentOff;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.conditionsJson = conditionsJson;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getPercentOff() {
        return percentOff;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public String getConditionsJson() {
        return conditionsJson;
    }
}
