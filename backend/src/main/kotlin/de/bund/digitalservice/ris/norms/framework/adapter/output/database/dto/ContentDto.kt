package de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto

import java.util.UUID
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "contents")
data class ContentDto(
    val guid: UUID,
    val type: ContentElementType,
    var marker: String? = null,
    val text: String,
    @Column("order_number") // Because `order` is a reserved keyword in SQL.
    val order: Int,
    @Column("section_guid") val sectionGuid: UUID? = null,
    @Column("norm_guid") val normGuid: UUID? = null
)

enum class ContentElementType {
  PREAMBLE,
  PARAGRAPH,
  CLOSING,
}
