package de.bund.digitalservice.ris.norms.domain.entity

import java.util.UUID

data class Recitals(
    val guid: UUID,
    val marker: String?,
    val heading: String?,
    val text: String,
)
