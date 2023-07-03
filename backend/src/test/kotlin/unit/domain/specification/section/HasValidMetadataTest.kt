package de.bund.digitalservice.ris.norms.domain.specification.section

import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.NormCategory
import de.bund.digitalservice.ris.norms.domain.value.OtherType
import de.bund.digitalservice.ris.norms.domain.value.ProofIndication
import de.bund.digitalservice.ris.norms.domain.value.ProofType
import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalTime

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
        every { instance.name } returns MetadataSectionName.AGE_INDICATION
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("range start", MetadatumType.RANGE_START),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `can only generate age identification with both blocks of range value and unit`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.AGE_INDICATION
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("range start", MetadatumType.RANGE_START),
            Metadatum("range end", MetadatumType.RANGE_END),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `can generate citation date with a date value`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.CITATION_DATE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(LocalDate.now(), MetadatumType.DATE),
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
            Metadatum(LocalDate.now(), MetadatumType.DATE),
            Metadatum("citation year", MetadatumType.YEAR),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it throws an error on citation date with a not allowed value`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.CITATION_DATE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("something other", MetadatumType.LEAD_JURISDICTION),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it throws an error on citation date with date and a not allowed metadatum`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.CITATION_DATE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(LocalDate.now(), MetadatumType.DATE),
            Metadatum("something other", MetadatumType.LEAD_UNIT),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate print announcement with right metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.PRINT_ANNOUNCEMENT
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("announcement gazette", MetadatumType.ANNOUNCEMENT_GAZETTE),
            Metadatum("year", MetadatumType.YEAR),
            Metadatum("number", MetadatumType.NUMBER),
            Metadatum("page number", MetadatumType.PAGE),
            Metadatum("additional info #1", MetadatumType.ADDITIONAL_INFO),
            Metadatum("additional info #2", MetadatumType.ADDITIONAL_INFO),
            Metadatum("explanation #1", MetadatumType.EXPLANATION),
            Metadatum("explanation #2", MetadatumType.EXPLANATION),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on print announcement with right and wrong metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.PRINT_ANNOUNCEMENT
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("announcement gazette", MetadatumType.ANNOUNCEMENT_GAZETTE),
            Metadatum("announcement medium", MetadatumType.ANNOUNCEMENT_MEDIUM),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate digital announcement with right metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIGITAL_ANNOUNCEMENT
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("announcement medium", MetadatumType.ANNOUNCEMENT_MEDIUM),
            Metadatum(LocalDate.now(), MetadatumType.DATE),
            Metadatum("number", MetadatumType.EDITION),
            Metadatum("year", MetadatumType.YEAR),
            Metadatum("area of publication", MetadatumType.AREA_OF_PUBLICATION),
            Metadatum("number of the publication in the respective area", MetadatumType.NUMBER_OF_THE_PUBLICATION_IN_THE_RESPECTIVE_AREA),
            Metadatum("additional info #1", MetadatumType.ADDITIONAL_INFO),
            Metadatum("additional info #2", MetadatumType.ADDITIONAL_INFO),
            Metadatum("explanation #1", MetadatumType.EXPLANATION),
            Metadatum("explanation #2", MetadatumType.EXPLANATION),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on digital announcement with right and wrong metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIGITAL_ANNOUNCEMENT
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("announcement gazette", MetadatumType.ANNOUNCEMENT_GAZETTE),
            Metadatum("announcement medium", MetadatumType.ANNOUNCEMENT_MEDIUM),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate eu government gazette with right metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.EU_ANNOUNCEMENT
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("eu government gazette", MetadatumType.EU_GOVERNMENT_GAZETTE),
            Metadatum("year", MetadatumType.YEAR),
            Metadatum("series", MetadatumType.SERIES),
            Metadatum("number", MetadatumType.NUMBER),
            Metadatum("page number", MetadatumType.PAGE),
            Metadatum("additional info #1", MetadatumType.ADDITIONAL_INFO),
            Metadatum("additional info #2", MetadatumType.ADDITIONAL_INFO),
            Metadatum("explanation #1", MetadatumType.EXPLANATION),
            Metadatum("explanation #2", MetadatumType.EXPLANATION),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on eu government gazette with right and wrong metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.EU_ANNOUNCEMENT
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("eu government gazette", MetadatumType.EU_GOVERNMENT_GAZETTE),
            Metadatum("announcement medium", MetadatumType.ANNOUNCEMENT_MEDIUM),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate other official references with right metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.OTHER_OFFICIAL_ANNOUNCEMENT
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("other official reference", MetadatumType.OTHER_OFFICIAL_REFERENCE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on other official references with right and wrong metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.OTHER_OFFICIAL_ANNOUNCEMENT
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("entity", MetadatumType.ENTITY),
            Metadatum("announcement medium", MetadatumType.ANNOUNCEMENT_MEDIUM),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate norm provider with right metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.NORM_PROVIDER
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("norm entity", MetadatumType.ENTITY),
            Metadatum("deciding body", MetadatumType.DECIDING_BODY),
            Metadatum(true, MetadatumType.RESOLUTION_MAJORITY),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on norm provider with right and wrong metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.NORM_PROVIDER
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("norm entity", MetadatumType.ENTITY),
            Metadatum("announcement medium", MetadatumType.ANNOUNCEMENT_MEDIUM),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate document type with right metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DOCUMENT_TYPE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("type name", MetadatumType.TYPE_NAME),
            Metadatum(NormCategory.BASE_NORM, MetadatumType.NORM_CATEGORY),
            Metadatum(NormCategory.TRANSITIONAL_NORM, MetadatumType.NORM_CATEGORY),
            Metadatum("template name", MetadatumType.TEMPLATE_NAME),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on document type with right and wrong metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DOCUMENT_TYPE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("type name", MetadatumType.TYPE_NAME),
            Metadatum("announcement medium", MetadatumType.ANNOUNCEMENT_MEDIUM),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate divergent entry into force without metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE
        every { instance.metadata } returns emptyList()

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `can generate divergent entry into force defined with right metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(LocalDate.now(), MetadatumType.DATE),
            Metadatum(NormCategory.BASE_NORM, MetadatumType.NORM_CATEGORY),
            Metadatum(NormCategory.TRANSITIONAL_NORM, MetadatumType.NORM_CATEGORY),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on divergent entry into force defined with right and wrong metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(LocalDate.now(), MetadatumType.DATE),
            Metadatum("announcement medium", MetadatumType.ANNOUNCEMENT_MEDIUM),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate divergent entry into force undefined with right metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(UndefinedDate.UNDEFINED_FUTURE, MetadatumType.UNDEFINED_DATE),
            Metadatum(NormCategory.BASE_NORM, MetadatumType.NORM_CATEGORY),
            Metadatum(NormCategory.TRANSITIONAL_NORM, MetadatumType.NORM_CATEGORY),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on divergent entry into force undefined with right and wrong metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(UndefinedDate.UNDEFINED_FUTURE, MetadatumType.UNDEFINED_DATE),
            Metadatum("announcement medium", MetadatumType.ANNOUNCEMENT_MEDIUM),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate divergent expiration without metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIVERGENT_EXPIRATION
        every { instance.metadata } returns emptyList()

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `can generate divergent expiration defined with right metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(LocalDate.now(), MetadatumType.DATE),
            Metadatum(NormCategory.BASE_NORM, MetadatumType.NORM_CATEGORY),
            Metadatum(NormCategory.TRANSITIONAL_NORM, MetadatumType.NORM_CATEGORY),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on divergent expiration defined with right and wrong metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(LocalDate.now(), MetadatumType.DATE),
            Metadatum("announcement medium", MetadatumType.ANNOUNCEMENT_MEDIUM),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate divergent expiration undefined with right metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(UndefinedDate.UNDEFINED_UNKNOWN, MetadatumType.UNDEFINED_DATE),
            Metadatum(NormCategory.BASE_NORM, MetadatumType.NORM_CATEGORY),
            Metadatum(NormCategory.TRANSITIONAL_NORM, MetadatumType.NORM_CATEGORY),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on divergent expiration undefined with right and wrong metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(UndefinedDate.UNDEFINED_UNKNOWN, MetadatumType.UNDEFINED_DATE),
            Metadatum("announcement medium", MetadatumType.ANNOUNCEMENT_MEDIUM),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate categorized reference with right metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.CATEGORIZED_REFERENCE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("test reference", MetadatumType.TEXT),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on categorized reference with right and wrong metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.CATEGORIZED_REFERENCE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("test reference", MetadatumType.TEXT),
            Metadatum("test medium", MetadatumType.ANNOUNCEMENT_MEDIUM),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate entry into force with a date value`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.ENTRY_INTO_FORCE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(LocalDate.now(), MetadatumType.DATE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `can generate entry into force with an Undefined date value`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.ENTRY_INTO_FORCE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(UndefinedDate.UNDEFINED_UNKNOWN, MetadatumType.UNDEFINED_DATE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on entry into foce with both a date and undefined date value`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.ENTRY_INTO_FORCE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(LocalDate.now(), MetadatumType.DATE),
            Metadatum(UndefinedDate.UNDEFINED_UNKNOWN, MetadatumType.UNDEFINED_DATE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate principle entry into force with a date value`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.PRINCIPLE_ENTRY_INTO_FORCE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(LocalDate.now(), MetadatumType.DATE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `can generate principle entry into force with an Undefined date value`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.PRINCIPLE_ENTRY_INTO_FORCE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(UndefinedDate.UNDEFINED_UNKNOWN, MetadatumType.UNDEFINED_DATE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on principle entry into foce with both a date and undefined date value`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.PRINCIPLE_ENTRY_INTO_FORCE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(LocalDate.now(), MetadatumType.DATE),
            Metadatum(UndefinedDate.UNDEFINED_UNKNOWN, MetadatumType.UNDEFINED_DATE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate an expiration date with a date value`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.EXPIRATION
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(LocalDate.now(), MetadatumType.DATE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `can generate an expiration date with an Undefined date value`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.EXPIRATION
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(UndefinedDate.UNDEFINED_UNKNOWN, MetadatumType.UNDEFINED_DATE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on an expiration date with both a date and undefined date value`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.EXPIRATION
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(LocalDate.now(), MetadatumType.DATE),
            Metadatum(UndefinedDate.UNDEFINED_UNKNOWN, MetadatumType.UNDEFINED_DATE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate a principle expiration date with a date value`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.PRINCIPLE_EXPIRATION
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(LocalDate.now(), MetadatumType.DATE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `can generate a principle expiration date with an Undefined date value`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.PRINCIPLE_EXPIRATION
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(UndefinedDate.UNDEFINED_UNKNOWN, MetadatumType.UNDEFINED_DATE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on a principle expiration date with both a date and undefined date value`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.PRINCIPLE_EXPIRATION
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(LocalDate.now(), MetadatumType.DATE),
            Metadatum(UndefinedDate.UNDEFINED_UNKNOWN, MetadatumType.UNDEFINED_DATE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate digital evidence with right metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIGITAL_EVIDENCE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("link", MetadatumType.LINK),
            Metadatum("related data", MetadatumType.RELATED_DATA),
            Metadatum("external data note", MetadatumType.EXTERNAL_DATA_NOTE),
            Metadatum("appendix", MetadatumType.APPENDIX),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on digital evidence with right and wrong metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DIGITAL_EVIDENCE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("link", MetadatumType.LINK),
            Metadatum("announcement medium", MetadatumType.ANNOUNCEMENT_MEDIUM),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate footnote with right metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.FOOTNOTES
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("footnote reference", MetadatumType.FOOTNOTE_REFERENCE),
            Metadatum("footnote change", MetadatumType.FOOTNOTE_CHANGE),
            Metadatum("footnote comment", MetadatumType.FOOTNOTE_COMMENT),
            Metadatum("footnote decision", MetadatumType.FOOTNOTE_DECISION),
            Metadatum("footnote state law", MetadatumType.FOOTNOTE_STATE_LAW),
            Metadatum("footnote eu law", MetadatumType.FOOTNOTE_EU_LAW),
            Metadatum("footnote other", MetadatumType.FOOTNOTE_OTHER),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on footnote with right and wrong metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.FOOTNOTES
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("footnote reference", MetadatumType.FOOTNOTE_REFERENCE),
            Metadatum("footnote change", MetadatumType.FOOTNOTE_CHANGE),
            Metadatum("footnote comment", MetadatumType.FOOTNOTE_COMMENT),
            Metadatum("gazette", MetadatumType.ANNOUNCEMENT_GAZETTE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate document status with right metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DOCUMENT_STATUS
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("work note", MetadatumType.WORK_NOTE),
            Metadatum("description", MetadatumType.DESCRIPTION),
            Metadatum(LocalDate.now(), MetadatumType.DATE),
            Metadatum("year", MetadatumType.YEAR),
            Metadatum("reference", MetadatumType.REFERENCE),
            Metadatum("entry into force date note", MetadatumType.ENTRY_INTO_FORCE_DATE_NOTE),
            Metadatum(ProofIndication.NOT_YET_CONSIDERED, MetadatumType.PROOF_INDICATION),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on  document status with right and wrong metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DOCUMENT_STATUS
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("work note", MetadatumType.WORK_NOTE),
            Metadatum("announcement medium", MetadatumType.ANNOUNCEMENT_MEDIUM),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate document text proof with right metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DOCUMENT_TEXT_PROOF
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("work note", MetadatumType.TEXT),
            Metadatum(ProofType.TEXT_PROOF_FROM, MetadatumType.PROOF_TYPE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on  document text proof with right and wrong metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DOCUMENT_TEXT_PROOF
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("work note", MetadatumType.TEXT),
            Metadatum("announcement medium", MetadatumType.ANNOUNCEMENT_MEDIUM),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate document other with right metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DOCUMENT_OTHER
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(OtherType.TEXT_IN_PROGRESS, MetadatumType.OTHER_TYPE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on  document other with right and wrong metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.DOCUMENT_OTHER
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("work note", MetadatumType.TEXT),
            Metadatum(OtherType.TEXT_IN_PROGRESS, MetadatumType.OTHER_TYPE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate status with right metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.STATUS
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("note", MetadatumType.NOTE),
            Metadatum("description", MetadatumType.DESCRIPTION),
            Metadatum(LocalDate.now(), MetadatumType.DATE),
            Metadatum("reference", MetadatumType.REFERENCE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on status with right and wrong metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.STATUS
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("note", MetadatumType.NOTE),
            Metadatum("announcement medium", MetadatumType.ANNOUNCEMENT_MEDIUM),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate reissue with right metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.REISSUE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("note", MetadatumType.NOTE),
            Metadatum("article", MetadatumType.ARTICLE),
            Metadatum(LocalDate.now(), MetadatumType.DATE),
            Metadatum("reference", MetadatumType.REFERENCE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on  reissue with right and wrong metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.REISSUE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("note", MetadatumType.NOTE),
            Metadatum("article", MetadatumType.ARTICLE),
            Metadatum(LocalDate.now(), MetadatumType.DATE),
            Metadatum("announcement medium", MetadatumType.ANNOUNCEMENT_MEDIUM),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate repeal with right metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.REPEAL
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("text", MetadatumType.TEXT),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on  repeal with right and wrong metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.REPEAL
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("text", MetadatumType.TEXT),
            Metadatum(OtherType.TEXT_IN_PROGRESS, MetadatumType.OTHER_TYPE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate other status with right metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.OTHER_STATUS
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("note", MetadatumType.NOTE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `it throws an error on other status with right and wrong metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.OTHER_STATUS
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("note", MetadatumType.NOTE),
            Metadatum("text", MetadatumType.TEXT),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it throws an error if publication date has both date and year`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.PUBLICATION_DATE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(LocalDate.now(), MetadatumType.DATE),
            Metadatum("2020", MetadatumType.YEAR),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `it throws an error on publication date with wrong metadata`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.PUBLICATION_DATE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("note", MetadatumType.NOTE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `can generate publication date with date`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.PUBLICATION_DATE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(LocalDate.now(), MetadatumType.DATE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `can generate publication date with year`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.PUBLICATION_DATE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("2020", MetadatumType.YEAR),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `announcement date can have a date and time`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.ANNOUNCEMENT_DATE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(LocalDate.now(), MetadatumType.DATE),
            Metadatum(LocalTime.now(), MetadatumType.TIME),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `announcement date can have just a date`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.ANNOUNCEMENT_DATE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(LocalDate.now(), MetadatumType.DATE),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `announcement date can have just a year`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.ANNOUNCEMENT_DATE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("2020", MetadatumType.YEAR),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isTrue()
    }

    @Test
    fun `announcement date can not have a year and a time`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.ANNOUNCEMENT_DATE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum("2020", MetadatumType.YEAR),
            Metadatum(LocalTime.now(), MetadatumType.TIME),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }

    @Test
    fun `announcement date can not have a just a time`() {
        val instance = mockk<MetadataSection>()
        every { instance.name } returns MetadataSectionName.ANNOUNCEMENT_DATE
        every { instance.sections } returns null
        every { instance.metadata } returns listOf(
            Metadatum(LocalTime.now(), MetadatumType.TIME),
        )

        assertThat(hasValidMetadata.isSatisfiedBy(instance)).isFalse()
    }
}
