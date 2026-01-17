package com.kanokna.catalog.domain.model;

import com.kanokna.catalog.domain.exception.CatalogDomainErrors;
import java.util.Objects;

/**
 * Value object representing dimension constraints for a product.
 * Business rule: All dimensions must be within [50, 400] cm.
 */
public record DimensionConstraints(
    int minWidthCm,
    int maxWidthCm,
    int minHeightCm,
    int maxHeightCm
) {

    public static final int ABSOLUTE_MIN_CM = 50;
    public static final int ABSOLUTE_MAX_CM = 400;

    public DimensionConstraints {
        validate(minWidthCm, maxWidthCm, minHeightCm, maxHeightCm);
    }

    private static void validate(int minW, int maxW, int minH, int maxH) {
        if (minW < ABSOLUTE_MIN_CM || minW > ABSOLUTE_MAX_CM) {
            throw CatalogDomainErrors.dimensionOutOfRange(
                "minWidthCm",
                minW,
                ABSOLUTE_MIN_CM,
                ABSOLUTE_MAX_CM
            );
        }
        if (maxW < ABSOLUTE_MIN_CM || maxW > ABSOLUTE_MAX_CM) {
            throw CatalogDomainErrors.dimensionOutOfRange(
                "maxWidthCm",
                maxW,
                ABSOLUTE_MIN_CM,
                ABSOLUTE_MAX_CM
            );
        }
        if (minH < ABSOLUTE_MIN_CM || minH > ABSOLUTE_MAX_CM) {
            throw CatalogDomainErrors.dimensionOutOfRange(
                "minHeightCm",
                minH,
                ABSOLUTE_MIN_CM,
                ABSOLUTE_MAX_CM
            );
        }
        if (maxH < ABSOLUTE_MIN_CM || maxH > ABSOLUTE_MAX_CM) {
            throw CatalogDomainErrors.dimensionOutOfRange(
                "maxHeightCm",
                maxH,
                ABSOLUTE_MIN_CM,
                ABSOLUTE_MAX_CM
            );
        }
        if (minW > maxW) {
            throw CatalogDomainErrors.minExceedsMax("width", minW, maxW);
        }
        if (minH > maxH) {
            throw CatalogDomainErrors.minExceedsMax("height", minH, maxH);
        }
    }

    public boolean allows(int widthCm, int heightCm) {
        return widthCm >= minWidthCm && widthCm <= maxWidthCm
            && heightCm >= minHeightCm && heightCm <= maxHeightCm;
    }

    public static DimensionConstraints standard() {
        return new DimensionConstraints(50, 400, 50, 400);
    }
}
