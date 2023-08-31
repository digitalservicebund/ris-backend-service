package utils.factory

import de.bund.digitalservice.ris.norms.domain.entity.Recitals
import java.util.UUID

class RecitalsBuilder {

  var guid: UUID = UUID.randomUUID()
  var marker: String? = null
  var heading: String? = null
  var text: String = ""

  fun build() =
      Recitals(
          guid = guid,
          marker = marker,
          heading = heading,
          text = text,
      )
}
