package com.kanokna.pricing.domain.model;

/**
 * Status of a price book in its lifecycle.
 * Per DEC-ARCH-ENTITY-VERSIONING.
 */
public enum PriceBookStatus {
    /** Draft price book, editable */
    DRAFT,

    /** Published and currently active */
    ACTIVE,

    /** Archived (replaced by newer version) */
    ARCHIVED
}
