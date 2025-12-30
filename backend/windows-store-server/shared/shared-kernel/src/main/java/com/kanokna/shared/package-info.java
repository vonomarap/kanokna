/* <MODULE_CONTRACT id="MC-shared-kernel-core"
     ROLE="SharedLibrary"
     SERVICE="shared-kernel"
     LAYER="domain"
     BOUNDED_CONTEXT="shared-kernel"
     SPECIFICATION="NFR-MAINT-MODULARITY">
  <PURPOSE>
    Cross-service domain primitives providing type-safe, immutable value objects
    for the windows and doors e-commerce platform. Framework-free pure Java.
  </PURPOSE>

  <RESPONSIBILITIES>
    <Item>Money value object with multi-currency arithmetic (RUB, EUR, USD)</Item>
    <Item>DimensionsCm for product dimensions (50-400cm range enforced)</Item>
    <Item>Address, Email, PhoneNumber contact value objects</Item>
    <Item>LocalizedString for internationalized content</Item>
    <Item>DomainEvent interface with standard metadata</Item>
    <Item>Result sealed type for explicit error handling</Item>
    <Item>Common enums: Language, Currency, Country</Item>
  </RESPONSIBILITIES>

  <INVARIANTS>
    <Item>All value objects are immutable (final fields, no setters)</Item>
    <Item>No framework dependencies (Spring, JPA, Jackson annotations)</Item>
    <Item>Validation performed in constructors/factory methods</Item>
    <Item>equals/hashCode based on value, not identity</Item>
    <Item>All types are serialization-agnostic (adapters handle serialization)</Item>
  </INVARIANTS>

  <CONSTRAINTS>
    <Item>MUST NOT depend on Spring Framework</Item>
    <Item>MUST NOT depend on JPA/Hibernate</Item>
    <Item>MUST NOT depend on Jackson/JSON libraries</Item>
    <Item>MUST NOT contain DTOs or API models</Item>
    <Item>MUST NOT contain business logic beyond value validation</Item>
  </CONSTRAINTS>

  <PACKAGES>
    <Package name="com.kanokna.shared.core">Base value objects (Id, Email, Address, Result, PhoneNumber)</Package>
    <Package name="com.kanokna.shared.money">Money and Currency types</Package>
    <Package name="com.kanokna.shared.measure">Dimensions and measurements</Package>
    <Package name="com.kanokna.shared.event">Domain event interface and metadata</Package>
    <Package name="com.kanokna.shared.i18n">Internationalization types (Language, Country, LocalizedString)</Package>
  </PACKAGES>

  <TESTS>
    <Case id="TC-SK-001">Money arithmetic preserves currency and precision</Case>
    <Case id="TC-SK-002">DimensionsCm rejects values outside 50-400cm</Case>
    <Case id="TC-SK-003">Email validates format correctly</Case>
    <Case id="TC-SK-004">Result.Success and Result.Failure work as expected</Case>
    <Case id="TC-SK-005">LocalizedString retrieves correct language fallback</Case>
    <Case id="TC-SK-006">All value objects are immutable</Case>
    <Case id="TC-SK-007">equals/hashCode are value-based</Case>
  </TESTS>

  <LINKS>
    <Link ref="DevelopmentPlan.xml#DP-SVC-shared-kernel"/>
    <Link ref="RequirementsAnalysis.xml#NFR-MAINT-MODULARITY"/>
    <Link ref="RequirementsAnalysis.xml#NFR-I18N-MULTI-CURRENCY"/>
    <Link ref="Technology.xml#TECH-java"/>
  </LINKS>
</MODULE_CONTRACT> */

/**
 * Shared-kernel module providing cross-service domain primitives.
 * <p>
 * This module contains framework-free, immutable value objects used across
 * all bounded contexts in the Windows &amp; Doors E-Commerce platform.
 *
 * <h2>Core Packages:</h2>
 * <ul>
 *   <li>{@link com.kanokna.shared.core} - Base value objects (Id, Email, Address, Result)</li>
 *   <li>{@link com.kanokna.shared.money} - Money and Currency types</li>
 *   <li>{@link com.kanokna.shared.measure} - Dimensions and measurements</li>
 *   <li>{@link com.kanokna.shared.event} - Domain event interface and metadata</li>
 *   <li>{@link com.kanokna.shared.i18n} - Internationalization types</li>
 * </ul>
 *
 * <h2>Design Principles:</h2>
 * <ul>
 *   <li>Framework-free: No Spring, JPA, or Jackson dependencies</li>
 *   <li>Immutable: All value objects have final fields</li>
 *   <li>Validated: Invariants enforced at construction time</li>
 *   <li>Value-based: equals/hashCode based on content, not identity</li>
 * </ul>
 *
 * @see <a href="DevelopmentPlan.xml#DP-SVC-shared-kernel">DP-SVC-shared-kernel</a>
 */
package com.kanokna.shared;
