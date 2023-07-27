package de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto

import java.util.UUID
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table

@Table(name = "articles")
data class ArticleDto(
    val guid: UUID,
    var title: String? = null,
    val marker: String,
    @Column("norm_guid") val normGuid: UUID,
)
