package com.kanokna.search;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;

/**
 * ArchUnit tests enforcing hexagonal architecture boundaries.
 */
class ArchitectureTest {
    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("com.kanokna.search");
    }

    @Test
    @DisplayName("Domain layer should not depend on Spring Framework")
    void domainLayerShouldNotDependOnSpring() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Domain layer should not depend on JPA")
    void domainLayerShouldNotDependOnJPA() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("jakarta.persistence..")
                .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Domain layer should not depend on Jackson")
    void domainLayerShouldNotDependOnJackson() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("com.fasterxml.jackson..")
                .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Application layer should not depend on adapters")
    void applicationShouldNotDependOnAdapters() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..adapters..")
                .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("JPA entities should reside only in adapters.out.persistence")
    void jpaEntitiesOnlyInAdapters() {
        ArchRule rule = classes()
                .that().haveNameMatching(".*JpaEntity")
                .should().resideInAPackage("..adapters.out.persistence..")
                .allowEmptyShould(true);

        rule.check(importedClasses);
    }
}
