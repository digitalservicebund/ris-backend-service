package de.bund.digitalservice.ris.norms.domain.entity

import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import utils.createSimpleSections
import java.time.LocalDate
import java.util.UUID

class NormTest {

    @Test
    fun `can create a norm with only mandatory fields`() {
        val paragraph = Paragraph(UUID.randomUUID(), "marker", "text")
        val article = Article(UUID.randomUUID(), "title", "marker", listOf(paragraph))
        val guid = UUID.randomUUID()
        val norm = Norm(guid = guid, articles = listOf(article), officialLongTitle = "long title")

        assertThat(norm.guid).isEqualTo(guid)
        assertThat(norm.officialLongTitle).isEqualTo("long title")
        assertThat(norm.articles).isEqualTo(listOf(article))
        assertThat(norm.officialShortTitle).isNull()
        assertThat(norm.officialAbbreviation).isNull()
        assertThat(norm.risAbbreviation).isNull()
        assertThat(norm.documentStatusDescription).isNull()
        assertThat(norm.applicationScopeStartDate).isNull()
        assertThat(norm.categorizedReference).isNull()
        assertThat(norm.digitalEvidenceExternalDataNote).isNull()
        assertThat(norm.text).isNull()
    }

    @Test
    fun `can create a norm with a list of metadata`() {
        val guid = UUID.randomUUID()
        val sections = createSimpleSections()
        val norm =
            Norm(
                guid = guid,
                metadataSections = createSimpleSections(),
                officialLongTitle = "long title",
            )

        assertThat(norm.metadataSections.flatMap { it.metadata }).hasSize(2)
        assertThat(norm.metadataSections.flatMap { it.metadata }).containsAll(sections.first().metadata)
    }

    @Test
    fun `can create a norm with some optional string fields`() {
        val paragraph = Paragraph(UUID.randomUUID(), "marker", "text")
        val article = Article(UUID.randomUUID(), "title", "marker", listOf(paragraph))
        val guid = UUID.randomUUID()
        val norm =
            Norm(
                guid = guid,
                articles = listOf(article),
                officialLongTitle = "long title",
                officialShortTitle = "short title",
                officialAbbreviation = "ABC",
                risAbbreviation = "ABC",
            )

        assertThat(norm.guid).isEqualTo(guid)
        assertThat(norm.officialLongTitle).isEqualTo("long title")
        assertThat(norm.articles).isEqualTo(listOf(article))
        assertThat(norm.officialShortTitle).isEqualTo("short title")
        assertThat(norm.officialAbbreviation).isEqualTo("ABC")
        assertThat(norm.risAbbreviation).isEqualTo("ABC")
    }

    @Test
    fun `can create a norm with optional date and boolean fields`() {
        val publicationDate = LocalDate.of(2022, 11, 17)
        val announcementDate = LocalDate.of(2022, 11, 18)
        val paragraph = Paragraph(UUID.randomUUID(), "marker", "text")
        val article = Article(UUID.randomUUID(), "title", "marker", listOf(paragraph))
        val guid = UUID.randomUUID()

        val citationDate = Metadatum(LocalDate.of(2022, 11, 19), MetadatumType.DATE)
        val citationDateSection = MetadataSection(MetadataSectionName.CITATION_DATE, listOf(citationDate))

        val norm =
            Norm(
                guid = guid,
                articles = listOf(article),
                officialLongTitle = "long title",
                publicationDate = publicationDate,
                announcementDate = announcementDate,
                providerIsResolutionMajority = true,
                risAbbreviation = "ABC",
                documentStatusDescription = "document status description",
                applicationScopeStartDate = LocalDate.of(2022, 11, 18),
                categorizedReference = "categorized reference",
                digitalEvidenceExternalDataNote = "digital evidence external data note",
                text = "text",
                metadataSections = listOf(citationDateSection),
            )

        assertThat(norm.guid).isEqualTo(guid)
        assertThat(norm.officialLongTitle).isEqualTo("long title")
        assertThat(norm.articles).isEqualTo(listOf(article))
        assertThat(norm.publicationDate).isEqualTo(publicationDate)
        assertThat(norm.announcementDate).isEqualTo(announcementDate)
        assertThat(norm.metadataSections.flatMap { it.metadata }).contains(citationDate)
        assertThat(norm.providerIsResolutionMajority).isTrue()
        assertThat(norm.risAbbreviation).isEqualTo("ABC")
        assertThat(norm.documentStatusDescription).isEqualTo("document status description")
        assertThat(norm.applicationScopeStartDate).isEqualTo(LocalDate.of(2022, 11, 18))
        assertThat(norm.categorizedReference).isEqualTo("categorized reference")
        assertThat(norm.digitalEvidenceExternalDataNote)
            .isEqualTo("digital evidence external data note")
        assertThat(norm.text).isEqualTo("text")
    }
}
