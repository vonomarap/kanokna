package com.kanokna.pricing;

import com.kanokna.test.archunit.HexagonalArchitectureRules;
import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


/**
 * ArchUnit tests enforcing hexagonal architecture boundaries.
 * Ensures domain layer has no framework dependencies.
 */
class ArchitectureTest {
    private static final int ARCHUNIT_UNSUPPORTED_JAVA_FEATURE = 25;
    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        int javaFeature = Runtime.version().feature();
        Assumptions.assumeTrue(
            javaFeature < ARCHUNIT_UNSUPPORTED_JAVA_FEATURE,
            "ArchUnit does not support Java " + javaFeature
        );
        importedClasses = HexagonalArchitectureRules.importClasses("com.kanokna.pricing");
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
    @DisplayName("Application layer should not depend on adapters")
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
        HexagonalArchitectureRules.noPackageCycles("com.kanokna.pricing").check(importedClasses);
    }

    @Test
    @DisplayName("gRPC stubs should only be referenced by adapters")
    void grpcStubsOnlyUsedByAdapters() {
        HexagonalArchitectureRules.grpcStubsOnlyUsedByAdapters().check(importedClasses);
    }
}

