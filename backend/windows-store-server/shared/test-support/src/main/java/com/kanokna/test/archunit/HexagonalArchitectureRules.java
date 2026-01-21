package com.kanokna.test.archunit;

import java.util.Objects;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Shared ArchUnit rules enforcing hexagonal architecture boundaries.
 */
public final class HexagonalArchitectureRules {
    private static final String DOMAIN_PACKAGE = "..domain..";
    private static final String APPLICATION_PACKAGE = "..application..";
    private static final String ADAPTERS_PACKAGE = "..adapters..";
    private static final String PORT_PACKAGE = "..port..";
    private static final String SPRING_PACKAGE = "org.springframework..";
    private static final String JPA_PACKAGE = "jakarta.persistence..";
    private static final String JACKSON_PACKAGE = "com.fasterxml.jackson..";
    private static final String GRPC_STUB_PACKAGE = "..v1..";
    private static final String JPA_ENTITY_NAME = ".*JpaEntity";
    private static final String ADAPTER_IMPLEMENTER_PATTERN = ".*(Adapter|Client)";

    private HexagonalArchitectureRules() {
    }

    public static JavaClasses importClasses(String basePackage) {
        Objects.requireNonNull(basePackage, "basePackage");
        return new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .withImportOption(location -> !location.contains("api-contracts"))
            .importPackages(basePackage);
    }

    public static ArchRule domainShouldNotDependOnSpring() {
        return noClasses()
            .that().resideInAPackage(DOMAIN_PACKAGE)
            .should().dependOnClassesThat().resideInAPackage(SPRING_PACKAGE);
    }

    public static ArchRule domainShouldNotDependOnJpa() {
        return noClasses()
            .that().resideInAPackage(DOMAIN_PACKAGE)
            .should().dependOnClassesThat().resideInAPackage(JPA_PACKAGE);
    }

    public static ArchRule domainShouldNotDependOnJackson() {
        return noClasses()
            .that().resideInAPackage(DOMAIN_PACKAGE)
            .should().dependOnClassesThat().resideInAPackage(JACKSON_PACKAGE);
    }

    public static ArchRule applicationShouldNotDependOnAdapters() {
        return noClasses()
            .that().resideInAPackage(APPLICATION_PACKAGE)
            .should().dependOnClassesThat().resideInAPackage(ADAPTERS_PACKAGE);
    }

    public static ArchRule adaptersShouldImplementPorts() {
        return classes()
            .that().resideInAPackage(ADAPTERS_PACKAGE)
            .and().haveNameMatching(ADAPTER_IMPLEMENTER_PATTERN)
            .should().implement(JavaClass.Predicates.resideInAPackage(PORT_PACKAGE));
    }

    public static ArchRule jpaEntitiesOnlyInAdapters() {
        return classes()
            .that().haveNameMatching(JPA_ENTITY_NAME)
            .should().resideInAPackage("..adapters.out.persistence..");
    }

    public static ArchRule grpcStubsOnlyUsedByAdapters() {
        return noClasses()
            .that().resideOutsideOfPackage(ADAPTERS_PACKAGE)
            .should().dependOnClassesThat().resideInAPackage(GRPC_STUB_PACKAGE);
    }

    public static ArchRule noPackageCycles(String basePackage) {
        Objects.requireNonNull(basePackage, "basePackage");
        return SlicesRuleDefinition.slices()
            .matching(basePackage + ".(*)..")
            .should().beFreeOfCycles();
    }
}
