package de.bund.digitalservice.ris.norms.domain.specification.metadatum

import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.entity.MetadatumType
import de.bund.digitalservice.ris.norms.domain.entity.MetadatumType.KEYWORD
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HasValidValueTypeTest {
    @Test
    fun `it is satisfied if the value for a keyword is a string`() {
        val instance = getMockedMetadatum("foo", KEYWORD)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is not satisfied if the value for a keyword is not a string`() {
        val instance = getMockedMetadatum(103, KEYWORD)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }
}

/**
 * Creates a mocked instance of a [Metadatum]. This is necessary because the [Metadatum] class is
 * using this specification here itself within the initialization block. Thereby it is never
 * possible to create an instance that is not satisfying this specification. To still being able to
 * independently and fully test this specification, we use a mocked version of the instance type.
 */
private fun <T> getMockedMetadatum(value: T, type: MetadatumType): Metadatum<T> {
    val instance = mockk<Metadatum<T>>()
    every { instance.value } returns value
    every { instance.type } returns type
    return instance
}
