package de.bund.digitalservice.ris.norms.domain.entity

import de.bund.digitalservice.ris.norms.domain.value.Guid
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NormTest {
    @Test
    fun `can create a simple norm instance`() {
        val paragraph = Paragraph(Guid.generateNew(), "marker", "text")
        val article = Article(Guid.generateNew(), "title", "marker", listOf(paragraph))
        val guid = Guid.generateNew()
        val norm = Norm(guid, "long title", listOf(article))

        assertTrue(norm.guid == guid)
        assertTrue(norm.longTitle == "long title")
        assertTrue(norm.articles == listOf(article))
    }
}
