package de.bund.digitalservice.ris.norms.domain.specification.section

import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HasValidMetadataTest {
    @Test
    fun `it is satisfied if the metadatum types belong to the section`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.NORM
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("Keyword", MetadatumType.KEYWORD),
            Metadatum("divergentDocumentNumber", MetadatumType.DIVERGENT_DOCUMENT_NUMBER),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is not satisfied if any of the metadatum types do not belong to the section`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.LEAD
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("Keyword", MetadatumType.KEYWORD),
            Metadatum("divergentDocumentNumber", MetadatumType.UNOFFICIAL_REFERENCE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can only generate age identification with blocks of range value and unit`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.AGE_IDENTIFICATION
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("range start", MetadatumType.RANGE_START),
            Metadatum("range start unit", MetadatumType.RANGE_START_UNIT),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `can only generate age identification with both blocks of range value and unit`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.AGE_IDENTIFICATION
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("range start", MetadatumType.RANGE_START),
            Metadatum("range start unit", MetadatumType.RANGE_START_UNIT),
            Metadatum("range end", MetadatumType.RANGE_END),
            Metadatum("range end unit", MetadatumType.RANGE_END_UNIT),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on age identification with only one of range value or range unit`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.AGE_IDENTIFICATION
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("range start", MetadatumType.RANGE_START),
            Metadatum("range start unit", MetadatumType.RANGE_START_UNIT),
            Metadatum("range end unit", MetadatumType.RANGE_END_UNIT),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it throws an error on age identification with one of range value or range unit and one not complete range`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.AGE_IDENTIFICATION
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("range start", MetadatumType.RANGE_START),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate citation date with a date value`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.CITATION_DATE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("citation date", MetadatumType.DATE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `can generate citation date with a year value`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.CITATION_DATE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("citation year", MetadatumType.YEAR),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on citation date with date and year value`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.CITATION_DATE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("citation date", MetadatumType.DATE),
            Metadatum("citation year", MetadatumType.YEAR),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }
}
