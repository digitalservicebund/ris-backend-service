package de.bund.digitalservice.ris.norms.domain.entity

import de.bund.digitalservice.ris.norms.domain.value.Guid
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ArticleTest {
    @Test
    fun `can create a simple paragraph instance`() {
        val paragraph = Paragraph(Guid.generateNew(), "marker", "text")
        val guid = Guid.generateNew()
        val article = Article(guid, "title", "marker", listOf(paragraph))

        assertTrue(article.guid == guid)
        assertTrue(article.title == "title")
        assertTrue(article.marker == "marker")
        assertTrue(article.paragraphs == listOf(paragraph))
    }
}
