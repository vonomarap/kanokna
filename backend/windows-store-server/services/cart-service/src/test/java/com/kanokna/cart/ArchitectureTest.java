package com.kanokna.cart;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ArchUnit tests enforcing hexagonal architecture boundaries.
 * Ensures domain layer has no framework dependencies.
 */
class ArchitectureTest {
    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("com.kanokna.cart");
    }

    @Test
    @DisplayName("TC-FUNC-CART-ADD-001: Domain layer should not depend on Spring Framework")
    void domainLayerShouldNotDependOnSpring() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("TC-FUNC-CART-UPDATE-001: Domain layer should not depend on JPA")
    void domainLayerShouldNotDependOnJPA() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("jakarta.persistence..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("TC-FUNC-CART-REMOVE-001: Domain layer should not depend on Jackson")
    void domainLayerShouldNotDependOnJackson() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("com.fasterxml.jackson..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("TC-FUNC-CART-GET-001: Application layer should not depend on adapters")
    void applicationShouldNotDependOnAdapters() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..adapters..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("TC-FUNC-CART-REFRESH-001: JPA entities should reside only in adapters.out.persistence package")
    void jpaEntitiesOnlyInAdapters() {
        ArchRule rule = classes()
            .that().haveNameMatching(".*JpaEntity")
            .should().resideInAPackage("..adapters.out.persistence..");

        rule.check(importedClasses);
    }
}
