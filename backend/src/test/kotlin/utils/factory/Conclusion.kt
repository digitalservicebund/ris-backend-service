package utils.factory

import de.bund.digitalservice.ris.norms.domain.entity.Conclusion
import java.util.UUID

fun conclusion(block: ConclusionBuilder.() -> Unit) = ConclusionBuilder().apply(block).build()

class ConclusionBuilder {
  var guid: UUID = UUID.randomUUID()
  var text: String = ""

  fun build() = Conclusion(guid = guid, text = text)
}
