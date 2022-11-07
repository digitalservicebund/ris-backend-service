package de.bund.digitalservice.ris.norms.domain.entity

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class NormTest {
    @Test
    fun `can create a simple norm instance`() {
        val paragraph = Paragraph(UUID.randomUUID(), "marker", "text")
        val article = Article(UUID.randomUUID(), "title", "marker", listOf(paragraph))
        val guid = UUID.randomUUID()
        val norm = Norm(guid, "long title", listOf(article))

        assertTrue(norm.guid == guid)
        assertTrue(norm.longTitle == "long title")
        assertTrue(norm.articles == listOf(article))
    }
}
