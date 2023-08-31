package de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto

import java.util.UUID
import org.springframework.data.relational.core.mapping.Table

@Table(name = "formulas")
data class FormulaDto(
    val guid: UUID,
    val text: String,
)
