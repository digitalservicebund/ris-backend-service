package de.bund.digitalservice.ris.norms

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage
import com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith
import com.tngtech.archunit.core.domain.JavaClasses
import com.tngtech.archunit.core.domain.JavaMethod
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption.Predefined
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.stereotype.Service

private const val BASE_PACKAGE_PATH = "de.bund.digitalservice.ris.norms"
private const val NORMS_PACKAGE = "$BASE_PACKAGE_PATH.."
private const val DOMAIN_PACKAGE = "$BASE_PACKAGE_PATH.domain.."
private const val APPLICATION_PACKAGE = "$BASE_PACKAGE_PATH.application.."
private const val INPUT_PORT_PACKAGE = "$BASE_PACKAGE_PATH.application.port.input.."
private const val OUTPUT_PORT_PACKAGE = "$BASE_PACKAGE_PATH.application.port.output.."
private const val APPLICATION_SERVICE_PACKAGE = "$BASE_PACKAGE_PATH.application.service.."

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

    @Test
    fun `input ports are named as a use-cases but nothing else is`() {
        ArchRuleDefinition.classes()
            .that()
            .resideInAPackage(INPUT_PORT_PACKAGE)
            .should()
            .haveSimpleNameEndingWith("UseCase")
            .orShould()
            .beMemberClasses()
            .check(classes)

        ArchRuleDefinition.classes()
            .that()
            .haveSimpleNameContaining("UseCase")
            .should()
            .resideInAPackage(INPUT_PORT_PACKAGE)
            .check(classes)
    }

    @Test
    fun `input ports do not depend on output ports`() {
        SlicesRuleDefinition.slices()
            .matching("$BASE_PACKAGE_PATH.application.port.(*)..")
            .should()
            .notDependOnEachOther()
            .check(classes)
    }

    @Test
    fun `input ports have a method matching their use-case name`() {
        ArchRuleDefinition.classes()
            .that()
            .haveSimpleNameContaining("UseCase")
            .should(haveAMethodMatchingTheirUseCaseName)
            .check(classes)
    }

    @Test
    fun `output ports are named as that but nothing else is`() {
        ArchRuleDefinition.classes()
            .that()
            .resideInAPackage(OUTPUT_PORT_PACKAGE)
            .should()
            .haveSimpleNameEndingWith("OutputPort")
            .check(classes)

        ArchRuleDefinition.classes()
            .that()
            .haveSimpleNameContaining("OutputPort")
            .should()
            .resideInAPackage(OUTPUT_PORT_PACKAGE)
            .check(classes)
    }

    @Test
    fun `output ports have a method matching their name`() {
        ArchRuleDefinition.classes()
            .that()
            .haveSimpleNameContaining("OutputPort")
            .should(haveAMethodMatchingTheirOutPortName)
            .check(classes)
    }

    @Test
    fun `ports are interfaces`() {
        ArchRuleDefinition.classes()
            .that()
            .haveSimpleNameEndingWith("UseCase")
            .or()
            .haveSimpleNameEndingWith("OutputPort")
            .should()
            .beInterfaces()
            .check(classes)
    }

    @Test
    fun `ports have a single, public method only`() {
        ArchRuleDefinition.classes()
            .that()
            .haveSimpleNameEndingWith("UseCase")
            .or()
            .haveSimpleNameEndingWith("OutputPort")
            .should(haveASingleMethod)
            .andShould()
            .bePublic()
            .check(classes)
    }

    // TODO: Extend for output ports too.
    @Test
    fun `use-case methods have a single command or query parameter`() {
        ArchRuleDefinition.methods()
            .that()
            .areDeclaredInClassesThat(simpleNameEndingWith("UseCase"))
            .should(haveASingleParameter.and(haveCommandParameter))
            .orShould(haveASingleParameter.and(haveQueryParameter))
            .orShould(haveNoParameter)
            .check(classes)
    }

    @Test
    fun `application services are named as service`() {
        ArchRuleDefinition.classes()
            .that()
            .resideInAPackage(APPLICATION_SERVICE_PACKAGE)
            .should()
            .haveSimpleNameEndingWith("Service")
            .check(classes)
    }

    @Test
    fun `application services are annotated as service`() {
        ArchRuleDefinition.classes()
            .that()
            .resideInAPackage(APPLICATION_SERVICE_PACKAGE)
            .should()
            .beAnnotatedWith(Service::class.java)
            .check(classes)
    }

    @Test
    fun `application services implement a single input port`() {
        ArchRuleDefinition.classes()
            .that()
            .resideInAPackage(APPLICATION_SERVICE_PACKAGE)
            .should(implementASingleInterface)
            .andShould()
            .implement(resideInAPackage(INPUT_PORT_PACKAGE))
            .check(classes)
    }

    @Test
    fun `application services do not implement output ports`() {
        ArchRuleDefinition.noClasses()
            .that()
            .resideInAPackage(APPLICATION_SERVICE_PACKAGE)
            .should()
            .implement(resideInAPackage(OUTPUT_PORT_PACKAGE))
            .check(classes)
    }

    @Test
    fun `application services have the same name as their related use-case`() {
        ArchRuleDefinition.classes()
            .that()
            .resideInAPackage(APPLICATION_SERVICE_PACKAGE)
            .should(haveServiceNameMatchingUseCaseName)
            .check(classes)
    }
}

val haveASingleMethod = HaveExactNumberOfMethods(1)

class HaveExactNumberOfMethods(private val count: Int) :
    ArchCondition<JavaClass>("have exactly $count methods") {
    override fun check(item: JavaClass, events: ConditionEvents) {
        val methodCount = item.methods.size

        if (methodCount != this.count) {
            val message = "${item.name} has $methodCount methods instead of $count"
            val event = SimpleConditionEvent.violated(item, message)
            events.add(event)
            println("violated: ${events.containViolation()}")
        }
    }
}

val haveASingleParameter = HaveExactNumberOfParameters(1)
val haveNoParameter = HaveExactNumberOfParameters(0)

class HaveExactNumberOfParameters(private val count: Int) :
    ArchCondition<JavaMethod>("have exactly $count parameters") {

    override fun check(item: JavaMethod, events: ConditionEvents) {
        val parameterCount = item.parameters.size

        if (parameterCount != this.count) {
            val message = "${item.name} has $parameterCount parameters instead of $count"
            val event = SimpleConditionEvent.violated(item, message)
            events.add(event)
        }
    }
}

val implementASingleInterface = ImplementExactNumberOfInterfaces(1)

class ImplementExactNumberOfInterfaces(private val count: Int) :
    ArchCondition<JavaClass>("implement exactly $count interfaces") {

    override fun check(item: JavaClass, events: ConditionEvents) {
        val interfaceCount = item.interfaces.size

        if (interfaceCount != this.count) {
            val message = "${item.name} implements $interfaceCount interfaces instead of $count"
            val event = SimpleConditionEvent.violated(item, message)
            events.add(event)
        }
    }
}

// TODO: Also verify the parameter name too (Kotlin specific).
val haveCommandParameter = HaveParameterWithTypeName("Command")
val haveQueryParameter = HaveParameterWithTypeName("Query")

class HaveParameterWithTypeName(private val typeName: String) :
    ArchCondition<JavaMethod>("have a parameter of type name '$typeName'") {
    override fun check(item: JavaMethod, events: ConditionEvents) {
        for (parameter in item.parameters) {
            val typeName = parameter.type.getName()

            // Note that inner classes have the type name with a separating dollar sign.
            if (typeName == this.typeName || typeName.endsWith("\$${this.typeName}")) {
                return
            }
        }

        val message = "${item.name} has no parameter with type name $typeName"
        val event = SimpleConditionEvent.violated(item, message)
        events.add(event)
    }
}

val haveAMethodMatchingTheirUseCaseName = HaveAMethodNamedLikeClassPrefix("UseCase")
val haveAMethodMatchingTheirOutPortName = HaveAMethodNamedLikeClassPrefix("OutputPort")

/** Example: `LoadNormUseCase` with postfix `UseCase` has method `loadNorm` */
class HaveAMethodNamedLikeClassPrefix(private val classPostfix: String) :
    ArchCondition<JavaClass>("have method named like class prefix where postifx is $classPostfix") {
    override fun check(item: JavaClass, events: ConditionEvents) {
        if (!item.simpleName.endsWith(this.classPostfix)) {
            val message = "${item.name} has no matching name postfix of '${this.classPostfix}'"
            val event = SimpleConditionEvent.violated(item, message)
            events.add(event)
        }

        val classPrefix = item.simpleName.replace(this.classPostfix, "")
        val expectedMethodName = classPrefix.replaceFirstChar { it.lowercase() }

        for (method in item.methods) {
            if (method.name == expectedMethodName) {
                return
            }
        }

        val message = "${item.name} has no method of name '$expectedMethodName'"
        val event = SimpleConditionEvent.violated(item, message)
        events.add(event)
    }
}

val haveServiceNameMatchingUseCaseName = ImplementsInterfaceWithSamePrefix("Service", "UseCase")

/**
 * Example: `LoadNormService` with postfix `Service` implements interface with name
 * `LoadNormUseCase` for the interface postfix `UseCase` (matching prefix is 'LoadNorm')
 *
 * There is no verification that the class or interface actually has the related postfix nor if the
 * class implements any interface. Only the first interface gets checked.
 */
class ImplementsInterfaceWithSamePrefix(
    private val classPostfix: String,
    private val interfacePostfix: String
) :
    ArchCondition<JavaClass>(
        "with postfix '$classPostfix' has the same prefix as the interface it implements with postfix '$interfacePostfix'"
    ) {
    override fun check(item: JavaClass, events: ConditionEvents) {
        val firstInterface = item.rawInterfaces.elementAt(0)
        val classPrefix = item.simpleName.replace(this.classPostfix, "")
        val interfacePrefix = firstInterface.simpleName.replace(this.interfacePostfix, "")

        if (classPrefix != interfacePrefix) {
            val message =
                "${item.name} implements '${firstInterface.simpleName}' that has not expected same prefix of '$classPrefix'"
            val event = SimpleConditionEvent.violated(item, message)
            events.add(event)
        }
    }
}
