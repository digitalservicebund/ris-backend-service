package de.bund.digitalservice.ris.norms.domain.entity

import de.bund.digitalservice.ris.norms.domain.value.Guid

// TODO: Add article marker value class.
data class Article(
    val guid: Guid,
    val title: String,
    val marker: String,
    val paragraphs: List<Paragraph> = listOf()
)
