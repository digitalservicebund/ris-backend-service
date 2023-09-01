package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.schema

import de.bund.digitalservice.ris.norms.domain.entity.Recitals
import java.util.UUID

class RecitalsRequestSchema {
  var marker: String? = null
  var heading: String? = null
  lateinit var text: String

  fun toUseCaseData() =
      Recitals(
          guid = UUID.randomUUID(),
          marker = marker,
          heading = heading,
          text = text,
      )
}
