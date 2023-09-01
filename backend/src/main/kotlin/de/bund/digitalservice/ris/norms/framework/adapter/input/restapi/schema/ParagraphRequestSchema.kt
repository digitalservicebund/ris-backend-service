package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.schema

import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import java.util.UUID

class ParagraphRequestSchema {
  var marker: String? = null
  lateinit var text: String

  fun toUseCaseData() =
      Paragraph(
          guid = UUID.randomUUID(),
          marker = marker,
          text = text,
      )
}
