package de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto

import java.util.UUID
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "articles")
data class ArticleDto(
    val guid: UUID,
    @Column("order_number") // Because `order` is a reserved keyword in SQL.
    val order: Int,
    var marker: String?,
    val heading: String?,
    @Column("document_section_guid") val documentSectionGuid: UUID? = null,
    @Column("norm_guid") val normGuid: UUID? = null
)
