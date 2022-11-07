package de.bund.digitalservice.ris.norms.domain.entity

import java.util.UUID

// TODO: Add paragraph marker value class.
data class Paragraph(val guid: UUID, val marker: String, val text: String)
