package de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto

import kotlin.collections.List

data class ArticleDto(
    val guid: String,
    var title: String? = null,
    val marker: String,
    val markerText: String,
    var paragraphs: List<ParagraphDto> = listOf()
)
