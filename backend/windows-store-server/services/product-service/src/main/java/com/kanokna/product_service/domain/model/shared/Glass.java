package com.kanokna.product_service.domain.model.shared;

import com.kanokna.product_service.domain.model.enums.GlassType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents different types of glass used in window and door products.
 * 
 * Glass is a critical component that impacts various characteristics of a window
 * or door including thermal insulation, sound insulation, security, light transmission,
 * and energy efficiency. This entity stores the specifications and properties of
 * different glass options available for window and door products.
 * 
 * Glass specifications are referenced by ConstructionSpecs to define the complete
 * window or door assembly.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "glasses")
public class Glass {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "glass_id", updatable = false, unique = true, nullable = false)
    private Long id;

    @NotBlank(message = "{productCard.name.required}")
    @Size(min = 4, max = 50, message = "{productCard.name.size}")
    @Column(name = "name", length = 50, nullable = false)
    private String name;

    @Lob
    @Column(name = "description")
    private String description;

    @NotNull(message = "{window.glassType.required}")
    @Enumerated(EnumType.STRING)
    @Column(name = "glass_type", nullable = false)
    private GlassType glassType;

    /**
     * The number of glass panes in the assembly.
     * Modern insulating glass typically has 2 or 3 panes (double or triple glazing).
     * More panes generally provide better thermal and acoustic insulation.
     * Minimum value is 2 for modern energy-efficient windows.
     */
    @NotNull(message = "{window.panes.required}")
    @Min(value = 2, message = "{window.panes.min}")
    @Column(name = "number_of_panes", nullable = false)
    private Integer numberOfPanes;
}
