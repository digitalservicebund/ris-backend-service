package de.bund.digitalservice.ris.norms.domain.entity

import de.bund.digitalservice.ris.norms.domain.value.DocumentSectionType
import java.util.UUID

sealed interface Documentation {
  val guid: UUID
  val order: Int
  val marker: String
  val heading: String?
}

data class DocumentSection(
    override val guid: UUID,
    override val order: Int,
    override val marker: String,
    override val heading: String,
    val type: DocumentSectionType,
    val documentation: Collection<Documentation> = emptyList(),
) : Documentation

data class Article(
    override val guid: UUID,
    override val order: Int,
    override val marker: String,
    override val heading: String? = null,
    val paragraphs: Collection<Paragraph> = emptyList(),
) : Documentation
