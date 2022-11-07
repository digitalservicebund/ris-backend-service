package de.bund.digitalservice.ris.norms.domain.entity

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class ArticleTest {
    @Test
    fun `can create a simple paragraph instance`() {
        val paragraph = Paragraph(UUID.randomUUID(), "marker", "text")
        val guid = UUID.randomUUID()
        val article = Article(guid, "title", "marker", listOf(paragraph))

        assertTrue(article.guid == guid)
        assertTrue(article.title == "title")
        assertTrue(article.marker == "marker")
        assertTrue(article.paragraphs == listOf(paragraph))
    }
}
