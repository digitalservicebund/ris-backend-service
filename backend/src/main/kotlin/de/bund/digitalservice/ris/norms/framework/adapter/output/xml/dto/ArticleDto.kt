package de.bund.digitalservice.ris.norms.framework.adapter.output.xml.dto

import kotlin.collections.List

data class ArticleDto(
    val guid: String,
    var title: IdentifiedElement?,
    val marker: String,
    val markerText: IdentifiedElement,
    var paragraphs: List<ParagraphDto> = listOf()
)
