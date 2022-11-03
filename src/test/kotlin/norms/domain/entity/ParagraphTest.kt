package de.bund.digitalservice.ris.norms.domain.entity

import de.bund.digitalservice.ris.norms.domain.value.Guid
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ParagraphTest {
    @Test
    fun `can create a simple paragraph instance`() {
        val guid = Guid.generateNew()
        val paragraph = Paragraph(guid, "marker", "text")

        assertTrue(paragraph.guid == guid)
        assertTrue(paragraph.marker == "marker")
        assertTrue(paragraph.text == "text")
    }
}
