package de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto

import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "metadata_sections")
data class MetadataSectionDto(
    @Id
    val id: Int,
    val name: MetadataSectionName,
    @Column("order_number")
    val order: Int,
    @Column("norm_id")
    val normId: Int,
    @Column("section_id")
    val sectionId: Int? = null,
)
