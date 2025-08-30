package com.kanokna.product_service.domain.model.window;

import com.kanokna.product_service.domain.model.AbstractProductCard;
import com.kanokna.product_service.domain.model.shared.ConstructionSpecs;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
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
@Table(name = "windows")
public class Window extends AbstractProductCard {

    @Embedded
    private ConstructionSpecs constructionSpecs;

    /**
     * The number of dividers separating panes of glass
     * within the window sash or unit. 0 indicates no dividers.
     */
    @Positive(message = "{window.numberOfDividers.positive}")
    @Max(value = 10, message = "{window.numberOfDividers.max}")
    @Column(name = "number_of_dividers", nullable = false)
    private Integer numberOfDividers = 0; 
    
    @NotNull(message = "{window.isOpenable.required}")
    @Column(name = "is_openable", nullable = false)
    private Boolean isOpenable = false;

    /**
     * Indicates whether the window sash can be tilted inwards (usually from the top)
     * for ventilation. This commonly refers to hopper-style or tilt-and-turn functionality.
     */
    @NotNull(message = "{window.isTiltable.required}")
    @Column(name = "is_tiltable", nullable = false)
    private boolean isTiltable = false;

    @NotNull(message = "{window.hasInteriorSill.required}")
    @Column(name = "has_interior_sill", nullable = false)
    private Boolean hasInteriorSill = false;

    /**
     * Represents the exterior sill (sometimes called external sill or subsill).
     * This is the bottom component of the window frame on the outside
     */
    @NotNull(message = "{window.hasExteriorSill.required}")
    @Column(name = "has_exterior_sill", nullable = false)
    private Boolean hasExteriorSill = false;

}
