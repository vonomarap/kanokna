package com.kanokna.order_service.architecture;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "com.kanokna.order_service")
public class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule domain_should_be_framework_free = noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat().resideInAnyPackage("org.springframework..", "jakarta..", "..adapters..");

    @ArchTest
    static final ArchRule application_should_not_depend_on_adapters = noClasses()
        .that().resideInAPackage("..application..")
        .should().dependOnClassesThat().resideInAnyPackage("..adapters..");

    @ArchTest
    static final ArchRule no_cycles = slices().matching("com.kanokna.order_service.(*)..")
        .should().beFreeOfCycles();
}
