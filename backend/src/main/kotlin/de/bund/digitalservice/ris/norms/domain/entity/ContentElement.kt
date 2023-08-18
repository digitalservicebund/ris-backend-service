package de.bund.digitalservice.ris.norms.domain.entity

import java.util.*

sealed interface ContentElement {
  val guid: UUID
  val order: Int
  val marker: String?
  val text: String
}

data class Preamble(
    override val guid: UUID,
    override val order: Int,
    override val marker: String? = null,
    override val text: String
) : ContentElement

data class Paragraph(
    override val guid: UUID,
    override val order: Int,
    override val marker: String?,
    override val text: String
) : ContentElement

data class Closing(
    override val guid: UUID,
    override val order: Int,
    override val marker: String? = null,
    override val text: String
) : ContentElement
