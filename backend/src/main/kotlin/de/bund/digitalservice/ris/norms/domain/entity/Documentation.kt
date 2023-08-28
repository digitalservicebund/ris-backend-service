package de.bund.digitalservice.ris.norms.domain.entity

import de.bund.digitalservice.ris.norms.domain.value.DocumentSectionType
import java.util.UUID

sealed interface Documentation {
  val guid: UUID
  val order: Int
  val marker: String?
  val heading: String?
}

data class DocumentSection(
    override val guid: UUID,
    override val order: Int,
    val type: DocumentSectionType,
    override val marker: String? = null,
    override val heading: String? = null,
    val documentation: Collection<Documentation> = emptyList(),
) : Documentation

data class Article(
    override val guid: UUID,
    override val order: Int,
    val paragraphs: Collection<Paragraph> = emptyList(),
    override val marker: String? = null,
    override val heading: String? = null,
) : Documentation
