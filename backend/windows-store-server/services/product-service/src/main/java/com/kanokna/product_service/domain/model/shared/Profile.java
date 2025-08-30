package com.kanokna.product_service.domain.model.shared;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Represents a window profile system used in window and door construction.
 * 
 * The profile is the structural frame of a window or door, typically made from
 * materials like PVC, aluminum, wood, or composite materials. It defines the
 * dimensional, structural, and thermal properties of the window or door frame.
 * 
 * Profiles significantly impact the energy efficiency, durability, appearance, 
 * and cost of windows and doors. Different profiles offer various chamber counts,
 * thicknesses, and thermal insulation properties to meet different customer needs
 * and building requirements.
 * 
 * This entity stores the specifications and properties of different profile
 * systems available for window and door products.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "profiles")
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_id", updatable = false, unique = true, nullable = false)
    private Long id;

    @NotBlank(message = "{profile.name.required}")
    @Size(min = 4, max = 50, message = "{profile.name.size}")
    @Column(name = "name", length = 50, nullable = false)
    @ToString.Include
    private String name;

    /**
     * The thickness of the profile wall in millimeters.
     * This dimension affects the structural strength and durability of the profile.
     * Thicker profiles generally offer improved structural integrity and longevity.
     */
    @NotBlank(message = "{profile.thickness.required}")
    @Positive(message = "{profile.thickness.positive}")
    @Column(name = "thickness")
    private Integer thickness;

    @Lob
    @Column(name = "description")
    private String description;

    /**
     * The number of internal chambers within the profile.
     * Modern PVC and aluminum profiles use multiple internal chambers to improve
     * thermal insulation. More chambers generally provide better insulation properties.
     * High-end profiles may have 5 or more chambers.
     */
    @NotNull(message = "{profile.numberChamber.required}")
    @Positive(message = "{profile.numberChamber.positive}")
    @Column(name = "number_chamber")
    private Integer numberChamber;

    /**
     * Thermal insulation coefficient of the profile (U-value).
     * Measured in W/(m²·K), this value indicates the rate of heat transfer through
     * the profile. Lower values indicate better thermal insulation properties.
     * Modern energy-efficient profiles typically have U-values between 0.8 and 1.5 W/(m²·K).
     */
    @NotNull(message = "{profile.thermalCoefficient.required}")
    @Positive(message = "{profile.thermalCoefficient.positive}")
    @Digits(integer = 1, fraction = 2, message = "{profile.thermalCoefficient.format}")
    @Column(name = "thermal_coefficient")
    private Integer thermalCoefficient;
}
