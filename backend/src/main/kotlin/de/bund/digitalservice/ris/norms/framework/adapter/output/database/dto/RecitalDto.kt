package de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto

import java.util.UUID
import org.springframework.data.relational.core.mapping.Table

@Table(name = "recitals")
data class RecitalsDto(
    val guid: UUID,
    var marker: String? = null,
    var heading: String? = null,
    val text: String,
)
