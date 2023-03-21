package de.bund.digitalservice.ris.norms.conventions

import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage
import com.tngtech.archunit.lang.conditions.ArchConditions.beInterfaces
import com.tngtech.archunit.lang.conditions.ArchConditions.bePublic
import com.tngtech.archunit.lang.conditions.ArchConditions.implement
import com.tngtech.archunit.lang.conditions.ArchConditions.notImplement
import com.tngtech.archunit.lang.conditions.ArchConditions.onlyDependOnClassesThat
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition
import de.bund.digitalservice.ris.norms.conventions.condition.Conditions.haveAParameterWithTypeName
import de.bund.digitalservice.ris.norms.conventions.condition.Conditions.haveASingleMethod
import de.bund.digitalservice.ris.norms.conventions.condition.Conditions.haveASingleParameter
import de.bund.digitalservice.ris.norms.conventions.condition.Conditions.haveNoParameter
import de.bund.digitalservice.ris.norms.conventions.condition.Conditions.implementASingleInterface
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArchitectureFitnessTest {

    @Test
    fun `all packages are free circular dependencies`() {
        SlicesRuleDefinition.slices()
            .matching("$BASE_PACKAGE_PATH.(**)")
            .should()
            .beFreeOfCycles()
            .check(sourceClasses)
    }

    @Test
    fun `the domain package should depend on nothing except very specific standard libraries`() {
        ArchRuleDefinition.classes()
            .that(areFromTheDomain())
            .should(onlyDependOnClassesThat(areFromTheDomain().or(areFromAnyStandardLibrary())))
            .check(sourceClasses)
    }

    @Test
    fun `all classes of the domain should be sorted into the following packages`() {
        ArchRuleDefinition.classes()
            .that(areFromTheDomain())
            .should(beInsideTheEntityPackage())
            .orShould(beInsideTheObjectValuePackage())
            .orShould(beInsideTheSpecificationPackage())
            .check(sourceClasses)
    }

    @Test
    fun `the application package should depend only on the domain and specific extras`() {
        ArchRuleDefinition.classes()
            .that(areFromTheApplication())
            .should(
                onlyDependOnClassesThat(
                    areFromTheDomain()
                        .or(areFromTheApplication())
                        .or(areFromAnyStandardLibrary())
                        .or(
                            resideInAnyPackage(
                                "reactor.core..",
                                "org.springframework.stereotype..",
                                "org.reactivestreams..",
                                "org.slf4j..",
                            ),
                        ),
                ),
            )
            .check(sourceClasses)
    }

    @Test
    fun `all classes of the application should be sorted into the following packages`() {
        ArchRuleDefinition.classes()
            .that(areFromTheApplication())
            .should(beInsideTheInputPortPackage())
            .orShould(beInsideTheOutputPortPackage())
            .orShould(beInsideTheServicePackage())
            .check(sourceClasses)
    }

    @Test
    fun `ports are interfaces`() {
        ArchRuleDefinition.classes().that(areAnyPort()).should(beInterfaces()).check(sourceClasses)
    }

    @Test
    fun `ports should not depend on each other`() {
        SlicesRuleDefinition.slices()
            .matching("$BASE_PACKAGE_PATH.application.port.(*)..")
            .should()
            .notDependOnEachOther()
            .check(sourceClasses)
    }

    @Test
    fun `ports have a single, public method only`() {
        ArchRuleDefinition.classes()
            .that(areAnyPort())
            .should(haveASingleMethod())
            .andShould(bePublic())
            .check(sourceClasses)
    }

    @Test
    fun `port methods have a single command or query parameter`() {
        val haveACommandParameter = haveAParameterWithTypeName("Command")
        val haveAQueryParameter = haveAParameterWithTypeName("Query")

        ArchRuleDefinition.methods()
            .that()
            .areDeclaredInClassesThat(areAnyPort())
            .should(haveASingleParameter().and(haveACommandParameter))
            .orShould(haveASingleParameter().and(haveAQueryParameter))
            .orShould(haveNoParameter())
            .check(sourceClasses)
    }

    @Test
    fun `application services implement a single input port`() {
        ArchRuleDefinition.classes()
            .that(areAService())
            .should(implementASingleInterface())
            .andShould(implement(anInputPort()))
            .check(sourceClasses)
    }

    @Test
    fun `application services do not implement output ports`() {
        ArchRuleDefinition.classes()
            .that(areAService())
            .should(notImplement(anyOutputPort()))
            .check(sourceClasses)
    }
}
