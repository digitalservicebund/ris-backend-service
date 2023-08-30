package de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto

import de.bund.digitalservice.ris.norms.domain.value.DocumentSectionType
import java.util.UUID
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "document_section")
data class DocumentSectionDto(
    val guid: UUID,
    @Column("order_number") // Because `order` is a reserved keyword in SQL.
    val order: Int,
    val type: DocumentSectionType,
    var marker: String,
    val heading: String,
    @Column("parent_section_guid") val parentSectionGuid: UUID? = null,
    @Column("norm_guid") val normGuid: UUID? = null
)
