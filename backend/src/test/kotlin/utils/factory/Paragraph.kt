package utils.factory

import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import java.util.UUID
import utils.randomString

fun paragraph(block: ParagraphBuilder.() -> Unit): Paragraph =
    ParagraphBuilder().apply(block).build()

class ParagraphBuilder {
  var guid: UUID = UUID.randomUUID()
  var marker: String? = randomString(2)
  var text: String = randomString(100)
  var order: Int = 1

  fun build(): Paragraph = Paragraph(guid = guid, marker = marker, text = text, order = order)
}
