package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.schema

import de.bund.digitalservice.ris.norms.domain.entity.Formula
import java.util.UUID

class FormulaRequestSchema {
  lateinit var text: String

  fun toUseCaseData() = Formula(guid = UUID.randomUUID(), text = text)
}
