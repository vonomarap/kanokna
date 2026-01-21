package com.kanokna.catalog;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.kanokna.test.archunit.HexagonalArchitectureRules;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * ArchUnit tests enforcing hexagonal architecture boundaries.
 * Ensures domain layer has no framework dependencies.
 */
class ArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        int javaFeature = Runtime.version().feature();
        Assumptions.assumeTrue(
            javaFeature < 25,
            "ArchUnit does not support Java " + javaFeature
        );

        importedClasses = HexagonalArchitectureRules.importClasses("com.kanokna.catalog");
    }

    @Test
    @DisplayName("Domain layer should not depend on Spring Framework")
    void domainLayerShouldNotDependOnSpring() {
        HexagonalArchitectureRules.domainShouldNotDependOnSpring().check(importedClasses);
    }

    @Test
    @DisplayName("Domain layer should not depend on JPA")
    void domainLayerShouldNotDependOnJPA() {
        HexagonalArchitectureRules.domainShouldNotDependOnJpa().check(importedClasses);
    }

    @Test
    @DisplayName("Domain layer should not depend on Jackson")
    void domainLayerShouldNotDependOnJackson() {
        HexagonalArchitectureRules.domainShouldNotDependOnJackson().check(importedClasses);
    }

    @Test
    @DisplayName("Domain model classes should be in domain.model package")
    void domainModelInCorrectPackage() {
        ArchRule rule = classes()
            .that().haveNameMatching(".*Template|.*Configuration|.*Option.*|.*Rule.*|.*Bom.*|.*Version")
            .and().areNotInterfaces()
            .and().resideOutsideOfPackage("..dto..")
            .and().resideOutsideOfPackage("..adapters..")
            .should().resideInAPackage("..domain.model..");

        rule.check(importedClasses);
    }

    @Test
    @DisplayName("Application services should not depend on adapters")
    void applicationShouldNotDependOnAdapters() {
        HexagonalArchitectureRules.applicationShouldNotDependOnAdapters().check(importedClasses);
    }

    @Test
    @DisplayName("JPA entities should reside only in adapters.out.persistence package")
    void jpaEntitiesOnlyInAdapters() {
        HexagonalArchitectureRules.jpaEntitiesOnlyInAdapters().check(importedClasses);
    }

    @Test
    @DisplayName("Adapters should implement port interfaces")
    void adaptersImplementPorts() {
        HexagonalArchitectureRules.adaptersShouldImplementPorts().check(importedClasses);
    }

    @Test
    @DisplayName("No cyclic dependencies between packages")
    void noPackageCycles() {
        HexagonalArchitectureRules.noPackageCycles("com.kanokna.catalog").check(importedClasses);
    }

    @Test
    @DisplayName("gRPC stubs should only be referenced by adapters")
    void grpcStubsOnlyUsedByAdapters() {
        HexagonalArchitectureRules.grpcStubsOnlyUsedByAdapters().check(importedClasses);
    }
}
