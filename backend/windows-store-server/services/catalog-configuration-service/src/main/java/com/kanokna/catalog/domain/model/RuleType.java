package com.kanokna.catalog.domain.model;

/**
 * Types of configuration rules.
 */
public enum RuleType {
    /**
     * Enforces compatibility between options (e.g., material + glazing).
     */
    COMPATIBILITY,

    /**
     * Enforces dependency (e.g., option A requires option B).
     */
    DEPENDENCY,

    /**
     * Enforces mutual exclusion (e.g., option A excludes option B).
     */
    EXCLUSION,

    /**
     * General constraint (e.g., large sizes require reinforcement).
     */
    CONSTRAINT
}
