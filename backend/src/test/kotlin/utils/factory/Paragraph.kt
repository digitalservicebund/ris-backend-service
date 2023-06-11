package utils.factory

import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import utils.randomString
import java.util.UUID

fun paragraph(block: ParagraphBuilder.() -> Unit): Paragraph = ParagraphBuilder().apply(block).build()

class ParagraphBuilder {
    var guid: UUID = UUID.randomUUID()
    var marker: String? = randomString(2)
    var text: String = randomString(100)

    fun build(): Paragraph = Paragraph(guid, marker, text)
}
