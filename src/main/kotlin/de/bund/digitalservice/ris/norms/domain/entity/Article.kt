package de.bund.digitalservice.ris.norms.domain.entity

import java.util.UUID

// TODO: Add article marker value class.
data class Article(
    val guid: UUID,
    val title: String,
    val marker: String,
    val paragraphs: List<Paragraph> = listOf()
)
