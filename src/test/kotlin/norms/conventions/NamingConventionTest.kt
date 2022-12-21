package de.bund.digitalservice.ris.norms.conventions

import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import de.bund.digitalservice.ris.norms.conventions.condition.Conditions.haveAMethodWithNameLikeClassPrefix
import de.bund.digitalservice.ris.norms.conventions.condition.Conditions.implementInterfaceWithSamePrefix
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.stereotype.Service

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NamingConventionTest {

    @Test
    fun `input ports are named as use-case but no other class`() {
        ArchRuleDefinition.classes()
            .that(areFromTheInputPortPackage)
            .should()
            .haveSimpleNameEndingWith("UseCase")
            .orShould()
            .beMemberClasses()
            .check(allClasses)

        ArchRuleDefinition.classes()
            .that()
            .haveSimpleNameEndingWith("UseCase")
            .should(beInsideTheInputPortPackage)
            .check(allClasses)
    }

    @Test
    fun `input ports have a method matching their use-case name`() {
        ArchRuleDefinition.classes()
            .that(areAnInputPort)
            .should(haveAMethodWithNameLikeClassPrefix("UseCase"))
            .check(allClasses)
    }

    @Test
    fun `output ports are named as output port but no other class`() {
        ArchRuleDefinition.classes()
            .that(areFromTheOutputPortPackage)
            .should()
            .haveSimpleNameEndingWith("OutputPort")
            .check(allClasses)

        ArchRuleDefinition.classes()
            .that()
            .haveSimpleNameEndingWith("OutputPort")
            .should(beInsideTheOutputPortPackage)
            .check(allClasses)
    }

    @Test
    fun `output ports have a method matching their name`() {
        ArchRuleDefinition.classes()
            .that(areAnOutputPort)
            .should(haveAMethodWithNameLikeClassPrefix("OutputPort"))
            .check(allClasses)
    }

    @Test
    fun `application services are named as service`() {
        ArchRuleDefinition.classes()
            .that(areFromTheServicePackage)
            .should()
            .haveSimpleNameEndingWith("Service")
            .check(allClasses)
    }

    /**
     * The annotation as a service is used for discovery by the Spring framework as well as some minor
     * documentation purpose.
     */
    @Test
    fun `application services are annotated as service`() {
        ArchRuleDefinition.classes()
            .that(areAService)
            .should()
            .beAnnotatedWith(Service::class.java)
            .check(allClasses)
    }

    @Test
    fun `application services have the same name as their related use-case`() {
        ArchRuleDefinition.classes()
            .that(areAService)
            .should(implementInterfaceWithSamePrefix("Service", "UseCase"))
            .check(allClasses)
    }
}
