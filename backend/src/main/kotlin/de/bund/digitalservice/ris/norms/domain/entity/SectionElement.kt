package de.bund.digitalservice.ris.norms.domain.entity

import java.util.*

sealed interface SectionElement {
  val guid: UUID
  val order: Int
  val header: String?
  val designation: String
  var childSections: Collection<SectionElement>?
}

data class Book(
    override val header: String? = null,
    override val guid: UUID,
    override val designation: String,
    override val order: Int,
    override var childSections: Collection<SectionElement>? = null
) : SectionElement

data class Part(
    override val header: String? = null,
    override val guid: UUID,
    override val designation: String,
    override val order: Int,
    override var childSections: Collection<SectionElement>? = null
) : SectionElement

data class Chapter(
    override val header: String? = null,
    override val guid: UUID,
    override val designation: String,
    override val order: Int,
    override var childSections: Collection<SectionElement>? = null
) : SectionElement

data class Subchapter(
    override val header: String? = null,
    override val guid: UUID,
    override val designation: String,
    override val order: Int,
    override var childSections: Collection<SectionElement>? = null
) : SectionElement

data class Section(
    override val header: String? = null,
    override val guid: UUID,
    override val designation: String,
    override val order: Int,
    override var childSections: Collection<SectionElement>? = null
) : SectionElement

data class Article(
    override val header: String? = null,
    override val guid: UUID,
    override val designation: String,
    override val order: Int,
    var paragraphs: Collection<ContentElement>,
    override var childSections: Collection<SectionElement>? = null
) : SectionElement

data class Subsection(
    override val header: String? = null,
    override val guid: UUID,
    override val designation: String,
    override val order: Int,
    override var childSections: Collection<SectionElement>? = null
) : SectionElement

data class Title(
    override val header: String? = null,
    override val guid: UUID,
    override val designation: String,
    override val order: Int,
    override var childSections: Collection<SectionElement>? = null
) : SectionElement

data class Subtitle(
    override val header: String? = null,
    override val guid: UUID,
    override val designation: String,
    override val order: Int,
    override var childSections: Collection<SectionElement>? = null
) : SectionElement

data class Uncategorized(
    override val header: String? = null,
    override val guid: UUID,
    override val designation: String,
    override val order: Int,
    override var childSections: Collection<SectionElement>? = null
) : SectionElement
