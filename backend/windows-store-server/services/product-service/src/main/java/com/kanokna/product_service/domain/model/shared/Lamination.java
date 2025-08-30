package com.kanokna.product_service.domain.model.shared;

import com.kanokna.product_service.domain.model.enums.LaminationColor;
import com.kanokna.product_service.domain.model.enums.LaminationType;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the lamination specifications applied to window and door profiles.
 * 
 * Lamination is a decorative and protective layer applied to window and door
 * profiles, usually made of PVC or aluminum. It provides color, texture, and
 * additional weather resistance to the profile surface.
 * 
 * This is an embeddable component that can be included as part of the
 * ConstructionSpecs to define the appearance and surface treatment of
 * window and door products.
 */
@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Lamination {
    
    /**
     * Specifies which side of the window or door profile the lamination is applied to.
     * Possible values include:
     * - OUTER: Only the exterior side of the profile is laminated
     * - INNER: Only the interior side of the profile is laminated
     * - BOTH: Both exterior and interior sides are laminated (potentially with different colors)
     * - NONE: No lamination is applied (standard color of the profile material is used)
     */
    @NotNull(message = "{lamination.type.required}")
    @Enumerated(EnumType.STRING)
    @Column(name = "lamination_type", nullable = false)
    private LaminationType laminationType;

    @NotNull(message = "{lamination.color.required}")
    @Enumerated(EnumType.STRING)
    @Column(name = "lamination_color", nullable = false)
    private LaminationColor laminationColor;
}
