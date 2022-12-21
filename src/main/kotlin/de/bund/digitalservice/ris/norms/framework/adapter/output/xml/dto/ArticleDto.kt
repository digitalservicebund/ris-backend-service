package de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto

import java.util.UUID
import kotlin.collections.List

data class ArticleDto(
    val guid: UUID,
    var title: String? = null,
    val marker: String,
    var paragraphs: List<ParagraphDto> = listOf()
)
