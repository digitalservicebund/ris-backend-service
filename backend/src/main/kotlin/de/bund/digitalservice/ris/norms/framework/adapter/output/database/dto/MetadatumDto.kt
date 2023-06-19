package de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto

import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table(name = "metadata")
data class MetadatumDto(
    val guid: UUID,
    val value: String,
    val type: MetadatumType,
    @Column("order_number") // Because `order` is a reserved keyword in SQL.
    val order: Int,
    @Column("section_guid")
    val sectionGuid: UUID,
)
