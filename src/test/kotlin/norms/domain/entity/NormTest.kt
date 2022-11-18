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
        val norm = Norm(guid, "long title", listOf(article))

        assertTrue(norm.guid == guid)
        assertTrue(norm.longTitle == "long title")
        assertTrue(norm.articles == listOf(article))
        assertTrue(norm.officialShortTitle == null)
        assertTrue(norm.officialAbbreviation == null)
        assertTrue(norm.frameKeywords == null)
    }

    @Test
    fun `can create a norm with some optional string fields`() {
        val paragraph = Paragraph(UUID.randomUUID(), "marker", "text")
        val article = Article(UUID.randomUUID(), "title", "marker", listOf(paragraph))
        val guid = UUID.randomUUID()
        val norm = Norm(
            guid,
            "long title",
            listOf(article),
            officialShortTitle = "short title",
            officialAbbreviation = "ABC",
            frameKeywords = "keywords"
        )

        assertTrue(norm.guid == guid)
        assertTrue(norm.longTitle == "long title")
        assertTrue(norm.articles == listOf(article))
        assertTrue(norm.officialShortTitle == "short title")
        assertTrue(norm.officialAbbreviation == "ABC")
        assertTrue(norm.frameKeywords == "keywords")
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
            "long title",
            listOf(article),
            publicationDate = publicationDate,
            announcementDate = announcementDate,
            citationDate = citationDate,
            authorIsResolutionMajority = true
        )

        assertTrue(norm.guid == guid)
        assertTrue(norm.longTitle == "long title")
        assertTrue(norm.articles == listOf(article))
        assertTrue(norm.publicationDate == publicationDate)
        assertTrue(norm.announcementDate == announcementDate)
        assertTrue(norm.citationDate == citationDate)
        assertTrue(norm.authorIsResolutionMajority == true)
    }
}
