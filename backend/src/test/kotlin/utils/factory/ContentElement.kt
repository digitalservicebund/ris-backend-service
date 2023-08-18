import de.bund.digitalservice.ris.norms.domain.entity.Closing
import de.bund.digitalservice.ris.norms.domain.entity.ContentElement
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.domain.entity.Preamble
import java.util.*

class ContentElementBuilder<T : ContentElement>(private val clazz: Class<T>) {
  var guid: UUID = UUID.randomUUID()
  var order: Int = 1
  var marker: String? = null
  var text: String = ""

  fun build(): ContentElement {
    return when (clazz) {
      Preamble::class.java -> Preamble(guid, order, marker, text)
      Paragraph::class.java -> Paragraph(guid, order, marker, text)
      Closing::class.java -> Closing(guid, order, marker, text)
      else -> throw IllegalArgumentException("Unsupported class: ${clazz.simpleName}")
    }
  }
}
