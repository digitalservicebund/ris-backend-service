package utils.factory

import de.bund.digitalservice.ris.norms.domain.entity.Documentation
import java.util.UUID

abstract class DocumentationBuilder {
  var guid: UUID = UUID.randomUUID()
  var order: Int = 1
  var marker: String = ""
  var heading: String? = null

  abstract fun build(): Documentation
}
