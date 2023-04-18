package de.bund.digitalservice.ris.norms.domain.specification.metadatum

import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DATE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.KEYWORD
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RANGE_END
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RANGE_END_UNIT
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RANGE_START
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RANGE_START_UNIT
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RESOLUTION_MAJORITY
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

    @Test
    fun `it is not satisfied if the value for a date is a string`() {
        val instance = getMockedMetadatum("citation date", DATE)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is not satisfied if the value for a range start is not a string`() {
        val instance = getMockedMetadatum(123, RANGE_START)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is not satisfied if the value for a range start unit is a string`() {
        val instance = getMockedMetadatum("range start unit", RANGE_START_UNIT)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is not satisfied if the value for a range end is not a string`() {
        val instance = getMockedMetadatum(123, RANGE_END)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is not satisfied if the value for a range end unit is a string`() {
        val instance = getMockedMetadatum("range end unit", RANGE_END_UNIT)

        assertThat(hasValidValueType.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is not satisfied if the value for a resolution majority is a string`() {
        val instance = getMockedMetadatum("resolution majority", RESOLUTION_MAJORITY)

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
