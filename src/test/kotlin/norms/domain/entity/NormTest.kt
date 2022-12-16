package de.bund.digitalservice.ris.norms.domain.entity

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class NormTest {

    @Test
    fun `can create a norm with only mandatory fields`() {
        val paragraph = Paragraph(UUID.randomUUID(), "marker", "text")
        val article = Article(UUID.randomUUID(), "title", "marker", listOf(paragraph))
        val guid = UUID.randomUUID()
        val norm = Norm(guid, listOf(article), "long title")

        assertTrue(norm.guid == guid)
        assertTrue(norm.officialLongTitle == "long title")
        assertTrue(norm.articles == listOf(article))
        assertTrue(norm.officialShortTitle == null)
        assertTrue(norm.officialAbbreviation == null)
        assertTrue(norm.frameKeywords == null)
        assertTrue(norm.unofficialLongTitle == null)
        assertTrue(norm.risAbbreviation == null)
        assertTrue(norm.documentStatusDescription == null)
        assertTrue(norm.applicationScopeStartDate == null)
        assertTrue(norm.categorizedReference == null)
        assertTrue(norm.digitalEvidenceExternalDataNote == null)
        assertTrue(norm.ageIndicationStart == null)
        assertTrue(norm.text == null)
    }

    @Test
    fun `can create a norm with some optional string fields`() {
        val paragraph = Paragraph(UUID.randomUUID(), "marker", "text")
        val article = Article(UUID.randomUUID(), "title", "marker", listOf(paragraph))
        val guid = UUID.randomUUID()
        val norm = Norm(
            guid,
            listOf(article),
            "long title",

            officialShortTitle = "short title",
            officialAbbreviation = "ABC",
            frameKeywords = "keywords",
            unofficialLongTitle = "unofficial long title",
            risAbbreviation = "ABC"
        )

        assertTrue(norm.guid == guid)
        assertTrue(norm.officialLongTitle == "long title")
        assertTrue(norm.articles == listOf(article))
        assertTrue(norm.officialShortTitle == "short title")
        assertTrue(norm.officialAbbreviation == "ABC")
        assertTrue(norm.frameKeywords == "keywords")
        assertTrue(norm.unofficialLongTitle == "unofficial long title")
        assertTrue(norm.risAbbreviation == "ABC")
    }

    @Test
    fun `can create a norm with optional date and boolean fields`() {
        val publicationDate = LocalDate.of(2022, 11, 17)
        val announcementDate = LocalDate.of(2022, 11, 18)
        val citationDate = LocalDate.of(2022, 11, 19)
        val paragraph = Paragraph(UUID.randomUUID(), "marker", "text")
        val article = Article(UUID.randomUUID(), "title", "marker", listOf(paragraph))
        val guid = UUID.randomUUID()
        val norm = Norm(
            guid,
            listOf(article),
            "long title",

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
            text = "text"
        )

        assertTrue(norm.guid == guid)
        assertTrue(norm.officialLongTitle == "long title")
        assertTrue(norm.articles == listOf(article))
        assertTrue(norm.publicationDate == publicationDate)
        assertTrue(norm.announcementDate == announcementDate)
        assertTrue(norm.citationDate == citationDate)
        assertTrue(norm.providerIsResolutionMajority == true)
        assertTrue(norm.unofficialLongTitle == "unofficial long title")
        assertTrue(norm.risAbbreviation == "ABC")
        assertTrue(norm.documentStatusDescription == "document status description")
        assertTrue(norm.applicationScopeStartDate == LocalDate.of(2022, 11, 18))
        assertTrue(norm.categorizedReference == "categorized reference")
        assertTrue(norm.digitalEvidenceExternalDataNote == "digital evidence external data note")
        assertTrue(norm.ageIndicationStart == "age indication start")
        assertTrue(norm.text == "text")
    }
}
