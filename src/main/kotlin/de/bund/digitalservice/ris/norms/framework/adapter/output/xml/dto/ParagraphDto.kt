package de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto

import java.util.UUID

data class ParagraphDto(
    val guid: UUID,
    var marker: String? = null,
    val text: String
)
