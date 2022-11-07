package de.bund.digitalservice.ris.norms.domain.entity

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

class ParagraphTest {
    @Test
    fun `can create a simple paragraph instance`() {
        val guid = UUID.randomUUID()
        val paragraph = Paragraph(guid, "marker", "text")

        assertTrue(paragraph.guid == guid)
        assertTrue(paragraph.marker == "marker")
        assertTrue(paragraph.text == "text")
    }
}
