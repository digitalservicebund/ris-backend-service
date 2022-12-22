package de.bund.digitalservice.ris.norms.conventions

import com.tngtech.archunit.lang.conditions.ArchConditions.not
import com.tngtech.archunit.library.GeneralCodingRules
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CodingRulesTest {

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
