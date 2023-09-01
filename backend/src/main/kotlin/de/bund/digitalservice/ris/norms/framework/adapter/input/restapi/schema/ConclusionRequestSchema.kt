package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.schema

import de.bund.digitalservice.ris.norms.domain.entity.Conclusion
import java.util.UUID

class ConclusionRequestSchema {
  lateinit var text: String

  fun toUseCaseData() = Conclusion(guid = UUID.randomUUID(), text = text)
}
