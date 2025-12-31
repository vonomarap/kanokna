package com.kanokna.catalog.domain.model;

/**
 * Lifecycle status of a ProductTemplate.
 * DRAFT: editable, not visible to customers
 * ACTIVE: published, visible to customers
 * ARCHIVED: historical version, not visible to customers
 */
public enum TemplateStatus {
    DRAFT,
    ACTIVE,
    ARCHIVED
}
