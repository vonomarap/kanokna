package com.kanokna.product_service.domain.model.shared;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embeddable component that represents the physical construction specifications
 * for building products such as windows and doors.
 * 
 * This class encapsulates common physical attributes like dimensions, materials,
 * and structural components that define how a product is constructed. By making
 * this an embeddable component, these specifications can be reused across
 * different product types while maintaining a consistent structure.
 * 
 * The specifications include both dimensional measurements and references to
 * related components such as profiles, glass, and lamination options.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ConstructionSpecs {

    /**
     * The width of the construction in centimeters.
     * Constrained between 50cm and 500cm to ensure manufacturability
     */
    @NotNull(message = "{constructionSpecs.width.required}")
    @Positive(message = "{constructionSpecs.width.positive}")
    @Max(value = 500, message = "{constructionSpecs.width.max}")
    @Min(value = 50, message = "{constructionSpecs.width.min}")
    @Column(name = "width", nullable = false)
    private Integer width;

    /**
     * The height of the construction in centimeters.
     * Constrained between 50cm and 500cm to ensure manufacturability
     */
    @NotNull(message = "{window.height.required}")
    @Positive(message = "{window.height.positive}")
    @Max(value = 500, message = "{window.height.max}")
    @Min(value = 50, message = "{window.height.min}")
    @Column(name = "height", nullable = false)
    private Integer height;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "glass_id", nullable = false)
    private Glass glass;

    @Embedded
    private Lamination lamination;

    public Lamination getLamination() {
        return lamination;
    }

    /**
     * Indicates whether the window includes casing (also known as architrave or window trim).
     * Casing is the decorative moulding that typically covers the gap
     * between the window frame and the surrounding wall surface.
     */
    @Column(name = "has_casing")
    private Boolean hasCasing = false;

    /**
     * Indicates whether an insect screen is included with the window or door.
     * Insect screens allow for ventilation while preventing insects
     * from entering when the window is open.
     */
    @Column(name = "has_insect_screen")
    private Boolean hasInsectScreen = false;
}
