package com.kanokna.shared.measure;

import com.kanokna.shared.core.ValidationUtils;

public record DimensionsCm(Integer width, Integer height) {
    public static final Integer PRODUCTION_MAX_LIMITS = 4_00; // 4m or 400cm
    public static final Integer PRODUCTION_MIN_LIMITS = 50;

    public DimensionsCm {
        if(ValidationUtils.hasNonPositiveValues(width, height))
            throw new IllegalArgumentException("All dimensions must be > 0 cm"); 
        if (width > PRODUCTION_MAX_LIMITS || height > PRODUCTION_MAX_LIMITS) 
            throw new IllegalArgumentException("Unreasonably large dimensions (max Width 400 cm and Height 300 cm)");
        if (width < PRODUCTION_MIN_LIMITS || height > PRODUCTION_MIN_LIMITS) 
            throw new IllegalArgumentException("Unreasonably small dimensions (min 50 cm)");
    }

    public int calculateAreaCm2() {
        return width * height;
    }

    public boolean fitsWithin(DimensionsCm min, DimensionsCm max) {
        return width  >= min.width  && width  <= max.width
            && height >= min.height && height <= max.height;
    }
}
