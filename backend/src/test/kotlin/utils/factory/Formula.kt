package utils.factory

import de.bund.digitalservice.ris.norms.domain.entity.Formula
import java.util.UUID

class FormulaBuilder {

  var guid: UUID = UUID.randomUUID()
  var text: String = ""

  fun build() =
      Formula(
          guid = guid,
          text = text,
      )
}
