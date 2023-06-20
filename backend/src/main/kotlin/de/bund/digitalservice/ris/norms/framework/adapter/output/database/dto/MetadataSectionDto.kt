package de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto

import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.*

@Table(name = "metadata_sections")
data class MetadataSectionDto(
    val guid: UUID,
    val name: MetadataSectionName,
    @Column("order_number")
    val order: Int,
    @Column("norm_guid")
    val normGuid: UUID,
    @Column("section_guid")
    val sectionGuid: UUID? = null,
)
