package de.bund.digitalservice.ris.norms.domain.specification.section

import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.OtherType
import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate

class HasValidChildrenTest {

    @Test
    fun `it is satisfied that the norm section does not have any children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.NORM
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is satisfied that the norm provider section does not have any children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.NORM_PROVIDER
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is satisfied that the subject area section does not have any children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.SUBJECT_AREA
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is satisfied that the lead section does not have any children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.LEAD
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is satisfied that the participation section does not have any children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.PARTICIPATION
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is satisfied that the citation date section does not have any children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.CITATION_DATE
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is satisfied that the age indication section does not have any children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.AGE_INDICATION
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is satisfied that the print announcement section does not have any children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.PRINT_ANNOUNCEMENT
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is satisfied that the digital announcement section does not have any children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIGITAL_ANNOUNCEMENT
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is satisfied that the eu announcement section does not have any children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.EU_ANNOUNCEMENT
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is satisfied that the other official announcement section does not have any children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.OTHER_OFFICIAL_ANNOUNCEMENT
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is satisfied that the official reference section only contains only one of the allowed children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.OFFICIAL_REFERENCE
        every { instance.sections } returns listOf(
            MetadataSection(MetadataSectionName.PRINT_ANNOUNCEMENT, listOf(Metadatum("announcement gazette", MetadatumType.ANNOUNCEMENT_GAZETTE))),
            MetadataSection(MetadataSectionName.DIGITAL_ANNOUNCEMENT, listOf(Metadatum("announcement medium", MetadatumType.ANNOUNCEMENT_MEDIUM))),
            MetadataSection(MetadataSectionName.EU_ANNOUNCEMENT, listOf(Metadatum("eu government gazette", MetadatumType.EU_GOVERNMENT_GAZETTE))),
            MetadataSection(MetadataSectionName.OTHER_OFFICIAL_ANNOUNCEMENT, listOf(Metadatum("other official reference", MetadatumType.OTHER_OFFICIAL_REFERENCE))),
        )

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is satisfied that the official reference section only contains only the allowed children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.OFFICIAL_REFERENCE
        every { instance.sections } returns listOf(
            MetadataSection(MetadataSectionName.PRINT_ANNOUNCEMENT, listOf(Metadatum("announcement gazette", MetadatumType.ANNOUNCEMENT_GAZETTE))),
        )

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is not satisfied that the official reference section only contains allowed children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.OFFICIAL_REFERENCE
        every { instance.sections } returns listOf(
            MetadataSection(MetadataSectionName.PRINT_ANNOUNCEMENT, listOf(Metadatum("announcement gazette", MetadatumType.ANNOUNCEMENT_GAZETTE))),
            MetadataSection(MetadataSectionName.NORM, listOf(Metadatum("keyword", MetadatumType.KEYWORD))),
        )

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is satisfied that the divergent entry into force defined section does not have any children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is satisfied that the divergent entry into force undefined section does not have any children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is satisfied that the divergent expiration defined section does not have any children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is satisfied that the divergent expiration undefined section does not have any children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is satisfied that the divergent entry into force section contains once every allowed children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE
        every { instance.sections } returns listOf(
            MetadataSection(MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED, listOf(Metadatum(LocalDate.now(), MetadatumType.DATE))),
            MetadataSection(MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED, listOf(Metadatum(UndefinedDate.UNDEFINED_UNKNOWN, MetadatumType.UNDEFINED_DATE))),
        )

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is satisfied that the divergent entry into force section contains only one of the allowed children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE
        every { instance.sections } returns listOf(
            MetadataSection(MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED, listOf(Metadatum(UndefinedDate.UNDEFINED_FUTURE, MetadatumType.UNDEFINED_DATE))),
        )

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is not satisfied that the divergent entry into force section only contains allowed children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE
        every { instance.sections } returns listOf(
            MetadataSection(MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED, listOf(Metadatum(LocalDate.now(), MetadatumType.DATE))),
            MetadataSection(MetadataSectionName.NORM, listOf(Metadatum("keyword", MetadatumType.KEYWORD))),
        )

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is satisfied that the divergent expiration section contains once every allowed children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIVERGENT_EXPIRATION
        every { instance.sections } returns listOf(
            MetadataSection(MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED, listOf(Metadatum(LocalDate.now(), MetadatumType.DATE))),
            MetadataSection(MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED, listOf(Metadatum(UndefinedDate.UNDEFINED_UNKNOWN, MetadatumType.UNDEFINED_DATE))),
        )

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is satisfied that the divergent expiration section contains only one of the allowed children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIVERGENT_EXPIRATION
        every { instance.sections } returns listOf(
            MetadataSection(MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED, listOf(Metadatum(UndefinedDate.UNDEFINED_FUTURE, MetadatumType.UNDEFINED_DATE))),
        )

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is not satisfied that the divergent expiration section only contains allowed children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIVERGENT_EXPIRATION
        every { instance.sections } returns listOf(
            MetadataSection(MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED, listOf(Metadatum(LocalDate.now(), MetadatumType.DATE))),
            MetadataSection(MetadataSectionName.NORM, listOf(Metadatum("keyword", MetadatumType.KEYWORD))),
        )

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is satisfied if the categorized reference does not have any children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.CATEGORIZED_REFERENCE
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is satisfied if the entry into force section does not have any children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.ENTRY_INTO_FORCE
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is satisfied if the principle entry into force section does not have any children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.PRINCIPLE_ENTRY_INTO_FORCE
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is satisfied if the expiration section does not have any children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.EXPIRATION
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is satisfied if the principle expiration section does not have any children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.PRINCIPLE_EXPIRATION
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is satisfied if the digital evidence section does not have any children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIGITAL_EVIDENCE
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is satisfied that the footnote section does not have any children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.FOOTNOTES
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is satisfied that the document status parent section only contains only one of the allowed children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DOCUMENT_STATUS_SECTION
        every { instance.sections } returns listOf(
            MetadataSection(MetadataSectionName.DOCUMENT_STATUS, listOf(Metadatum("work note", MetadatumType.WORK_NOTE))),
            MetadataSection(MetadataSectionName.DOCUMENT_TEXT_PROOF, listOf(Metadatum("text", MetadatumType.TEXT))),
            MetadataSection(MetadataSectionName.DOCUMENT_OTHER, listOf(Metadatum(OtherType.TEXT_IN_PROGRESS, MetadatumType.OTHER_TYPE))),
        )

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is not satisfied that the document status section only contains allowed children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DOCUMENT_STATUS_SECTION
        every { instance.sections } returns listOf(
            MetadataSection(MetadataSectionName.DOCUMENT_STATUS, listOf(Metadatum("work note", MetadatumType.WORK_NOTE))),
            MetadataSection(MetadataSectionName.NORM, listOf(Metadatum("keyword", MetadatumType.KEYWORD))),
        )

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it is satisfied that the document status section does not have any children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DOCUMENT_STATUS
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is satisfied that the document text proof section does not have any children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DOCUMENT_TEXT_PROOF
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it is satisfied that the document other section does not have any children sections`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DOCUMENT_OTHER
        every { instance.sections } returns null

        Assertions.assertThat(hasValidChildren.isSatisfiedBy(instance)).isTrue()
    }
}
