package de.bund.digitalservice.ris.norms.domain.entity

import java.util.UUID

data class Article(
    val guid: UUID,
    var title: String? = null,
    val marker: String,
    val paragraphs: List<Paragraph> = listOf(),
)
