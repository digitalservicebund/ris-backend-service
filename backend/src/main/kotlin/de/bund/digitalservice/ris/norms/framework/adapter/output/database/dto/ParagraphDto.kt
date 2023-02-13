package de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table(name = "paragraphs")
data class ParagraphDto(
    @Id
    val id: Int,
    val guid: UUID,
    var marker: String? = null,
    val text: String,
    @Column("article_id")
    val articleId: Int,
)
