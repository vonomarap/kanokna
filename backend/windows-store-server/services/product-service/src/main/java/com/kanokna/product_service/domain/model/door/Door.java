package com.kanokna.product_service.domain.model.door;

import com.kanokna.product_service.domain.model.AbstractProductCard;
import com.kanokna.product_service.domain.model.enums.DoorType;
import com.kanokna.product_service.domain.model.enums.OpeningDirection;
import com.kanokna.product_service.domain.model.shared.ConstructionSpecs;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "doors")
public class Door extends AbstractProductCard {
    
    @Embedded
    private ConstructionSpecs constructionSpecs;
    
    @NotNull(message = "{door.doorType.required}")
    @Enumerated(EnumType.STRING)
    @Column(name = "door_type", nullable = false)
    private DoorType doorType;
    
    @NotNull(message = "{door.openingDirection.required}")
    @Enumerated(EnumType.STRING)
    @Column(name = "opening_direction", nullable = false)
    private OpeningDirection openingDirection;
    
    @Positive(message = "{door.numberOfHinges.positive}")
    @Max(value = 5, message = "{door.numberOfHinges.max}")
    @Min(value = 2, message = "{door.numberOfHinges.min}")
    @Column(name = "number_of_hinges", nullable = false)
    private Integer numberOfHinges;

    /**
     * Indicates whether the door has a threshold (the bottom part of the doorframe).
     */
    @NotNull(message = "{door.hasThreshold.required}")
    @Column(name = "has_threshold", nullable = false)
    private Boolean hasThreshold = true;
    
    
} 