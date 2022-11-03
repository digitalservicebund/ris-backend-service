package de.bund.digitalservice.ris.norms

import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption.Predefined
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

private const val BASE_PACKAGE_PATH = "de.bund.digitalservice.ris.norms"
private const val NORMS_PACKAGE = "$BASE_PACKAGE_PATH.."
private const val DOMAIN_PACKAGE = "$BASE_PACKAGE_PATH.domain.."
private const val APPLICATION_PACKAGE = "$BASE_PACKAGE_PATH.application.."

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArchitectureFitnessTest {
    private lateinit var classes: JavaClasses

    @BeforeAll
    fun beforeAll() {
        classes =
            ClassFileImporter()
                .withImportOption(Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(NORMS_PACKAGE)
    }

    @Test
    fun `all packages are free circular dependencies`() {
        SlicesRuleDefinition.slices()
            .matching("$BASE_PACKAGE_PATH.(**)")
            .should()
            .beFreeOfCycles()
            .check(classes)
    }

    @Test
    fun `the domain package should depend on nothing except very specific standard libraries`() {
        ArchRuleDefinition.classes()
            .that()
            .resideInAPackage(DOMAIN_PACKAGE)
            .should()
            .onlyDependOnClassesThat()
            .resideInAnyPackage(DOMAIN_PACKAGE, "kotlin..", "java..", "org.jetbrains.annotations..")
            .check(classes)
    }

    @Test
    fun `the application package should depend only on the domain and specific extras`() {
        ArchRuleDefinition.classes()
            .that()
            .resideInAPackage(APPLICATION_PACKAGE)
            .should()
            .onlyDependOnClassesThat()
            .resideInAnyPackage(
                DOMAIN_PACKAGE,
                APPLICATION_PACKAGE,
                "reactor.core..",
                "org.springframework.stereotype..",
                "kotlin..",
                "java..",
                "org.jetbrains.annotations.."
            )
            .check(classes)
    }
}
