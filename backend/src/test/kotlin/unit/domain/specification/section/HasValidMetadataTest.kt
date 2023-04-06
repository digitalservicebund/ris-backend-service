package de.bund.digitalservice.ris.norms.domain.specification.section

import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
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

        Assertions.assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is not satisfied if any of the metadatum types do not belong to the section`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.NORM
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("Keyword", MetadatumType.KEYWORD),
            Metadatum("divergentDocumentNumber", MetadatumType.UNOFFICIAL_REFERENCE),
        )

        Assertions.assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }
}
