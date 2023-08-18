package de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto

import java.util.UUID
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "sections")
data class SectionDto(
    val guid: UUID,
    val type: SectionElementType,
    var designation: String,
    val header: String? = null,
    @Column("order_number") // Because `order` is a reserved keyword in SQL.
    val order: Int,
    @Column("section_guid") val sectionGuid: UUID? = null,
    @Column("norm_guid") val normGuid: UUID
)

enum class SectionElementType {
  BOOK,
  PART,
  CHAPTER,
  SUBCHAPTER,
  SECTION,
  ARTICLE,
  SUBSECTION,
  TITLE,
  SUBTITLE,
  UNCATEGORIZED
}
