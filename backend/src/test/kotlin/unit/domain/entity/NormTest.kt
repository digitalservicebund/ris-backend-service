package de.bund.digitalservice.ris.norms.domain.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
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
        assertThat(norm.unofficialLongTitle).isNull()
        assertThat(norm.risAbbreviation).isNull()
        assertThat(norm.documentStatusDescription).isNull()
        assertThat(norm.applicationScopeStartDate).isNull()
        assertThat(norm.categorizedReference).isNull()
        assertThat(norm.digitalEvidenceExternalDataNote).isNull()
        assertThat(norm.ageIndicationStart).isNull()
        assertThat(norm.text).isNull()
    }

    @Test
    fun `can create a norm with a list of metadata`() {
        val guid = UUID.randomUUID()
        val keyword = Metadatum("foo", MetadatumType.KEYWORD)
        val another_keyword = Metadatum("bar", MetadatumType.KEYWORD)
        val norm =
            Norm(
                guid = guid,
                metadata = listOf(keyword, another_keyword),
                officialLongTitle = "long title",
            )

        assertThat(norm.metadata).hasSize(2)
        assertThat(norm.metadata).contains(keyword, another_keyword)
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
                unofficialLongTitle = "unofficial long title",
                risAbbreviation = "ABC",
            )

        assertThat(norm.guid).isEqualTo(guid)
        assertThat(norm.officialLongTitle).isEqualTo("long title")
        assertThat(norm.articles).isEqualTo(listOf(article))
        assertThat(norm.officialShortTitle).isEqualTo("short title")
        assertThat(norm.officialAbbreviation).isEqualTo("ABC")
        assertThat(norm.unofficialLongTitle).isEqualTo("unofficial long title")
        assertThat(norm.risAbbreviation).isEqualTo("ABC")
    }

    @Test
    fun `can create a norm with optional date and boolean fields`() {
        val publicationDate = LocalDate.of(2022, 11, 17)
        val announcementDate = LocalDate.of(2022, 11, 18)
        val citationDate = LocalDate.of(2022, 11, 19)
        val paragraph = Paragraph(UUID.randomUUID(), "marker", "text")
        val article = Article(UUID.randomUUID(), "title", "marker", listOf(paragraph))
        val guid = UUID.randomUUID()
        val norm =
            Norm(
                guid = guid,
                articles = listOf(article),
                officialLongTitle = "long title",
                publicationDate = publicationDate,
                announcementDate = announcementDate,
                citationDate = citationDate,
                providerIsResolutionMajority = true,
                unofficialLongTitle = "unofficial long title",
                risAbbreviation = "ABC",
                documentStatusDescription = "document status description",
                applicationScopeStartDate = LocalDate.of(2022, 11, 18),
                categorizedReference = "categorized reference",
                digitalEvidenceExternalDataNote = "digital evidence external data note",
                ageIndicationStart = "age indication start",
                text = "text",
            )

        assertThat(norm.guid).isEqualTo(guid)
        assertThat(norm.officialLongTitle).isEqualTo("long title")
        assertThat(norm.articles).isEqualTo(listOf(article))
        assertThat(norm.publicationDate).isEqualTo(publicationDate)
        assertThat(norm.announcementDate).isEqualTo(announcementDate)
        assertThat(norm.citationDate).isEqualTo(citationDate)
        assertThat(norm.providerIsResolutionMajority).isTrue()
        assertThat(norm.unofficialLongTitle).isEqualTo("unofficial long title")
        assertThat(norm.risAbbreviation).isEqualTo("ABC")
        assertThat(norm.documentStatusDescription).isEqualTo("document status description")
        assertThat(norm.applicationScopeStartDate).isEqualTo(LocalDate.of(2022, 11, 18))
        assertThat(norm.categorizedReference).isEqualTo("categorized reference")
        assertThat(norm.digitalEvidenceExternalDataNote)
            .isEqualTo("digital evidence external data note")
        assertThat(norm.ageIndicationStart).isEqualTo("age indication start")
        assertThat(norm.text).isEqualTo("text")
    }
}
