package de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto

import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table(name = "paragraphs")
data class ParagraphDto(
    val guid: UUID,
    var marker: String? = null,
    val text: String,
    @Column("article_guid")
    val articleGuid: UUID,
)
