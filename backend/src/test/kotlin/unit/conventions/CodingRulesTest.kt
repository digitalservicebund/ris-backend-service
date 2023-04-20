package de.bund.digitalservice.ris.norms.conventions

import com.tngtech.archunit.base.DescribedPredicate.not
import com.tngtech.archunit.core.domain.JavaClass.Predicates.equivalentTo
import com.tngtech.archunit.lang.conditions.ArchConditions.dependOnClassesThat
import com.tngtech.archunit.lang.conditions.ArchConditions.not
import com.tngtech.archunit.lang.conditions.ArchPredicates.are
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.library.GeneralCodingRules
import org.hamcrest.MatcherAssert
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CodingRulesTest {
    @Test
    fun `tests do not use the JUnit or Hamcrest assertions`() {
        classes()
            .that(are(not(equivalentTo(this::class.java))))
            .should(not(dependOnClassesThat(are(equivalentTo(Assertions::class.java)))))
            .andShould(not(dependOnClassesThat(are(equivalentTo(MatcherAssert::class.java)))))
            .check(testClasses)
    }

    @Test
    fun `the standard output or error streams should not be used`() {
        GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS.check(allClasses)
    }

    @Test
    fun `no generic exceptions are thrown`() {
        GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS.check(sourceClasses)
    }

    @Test
    fun `all dependencies are injected via constructor`() {
        GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION.check(sourceClasses)
    }
}
