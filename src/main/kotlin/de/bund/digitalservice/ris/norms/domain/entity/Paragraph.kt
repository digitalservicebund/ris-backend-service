package de.bund.digitalservice.ris.norms.domain.entity

import de.bund.digitalservice.ris.norms.domain.value.Guid

// TODO: Add paragraph marker value class.
data class Paragraph(val guid: Guid, val marker: String, val text: String)
