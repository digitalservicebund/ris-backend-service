package de.bund.digitalservice.ris.norms.framework.adapter.output.database

import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Book
import de.bund.digitalservice.ris.norms.domain.entity.Chapter
import de.bund.digitalservice.ris.norms.domain.entity.Closing
import de.bund.digitalservice.ris.norms.domain.entity.ContentElement
import de.bund.digitalservice.ris.norms.domain.entity.FileReference
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.domain.entity.Part
import de.bund.digitalservice.ris.norms.domain.entity.Preamble
import de.bund.digitalservice.ris.norms.domain.entity.Section
import de.bund.digitalservice.ris.norms.domain.entity.SectionElement
import de.bund.digitalservice.ris.norms.domain.entity.Subchapter
import de.bund.digitalservice.ris.norms.domain.entity.Subsection
import de.bund.digitalservice.ris.norms.domain.entity.Subtitle
import de.bund.digitalservice.ris.norms.domain.entity.Title
import de.bund.digitalservice.ris.norms.domain.entity.Uncategorized
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.NormCategory
import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ContentDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ContentElementType
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.FileReferenceDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.MetadataSectionDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.MetadatumDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.SectionDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.SectionElementType
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

interface NormsMapper {
  fun normToEntity(
      normDto: NormDto,
      contentsNormLevel: List<ContentElement>,
      fileReferences: List<FileReference>,
      dtoMetadataSections: List<MetadataSectionDto>,
      dtoMetadata: List<MetadatumDto> = emptyList(),
      sectionsDto: List<SectionDto>,
      contentsNotNormLevel: List<ContentDto> = emptyList(),
  ): Norm {
    val listDomainSections = mutableListOf<MetadataSection>()

    // 1. Objective: move children from parent level to their respective parents because otherwise
    // we can't instantite the parent MetadataSection
    dtoMetadataSections
        .filter { dtoSectionToFilter -> dtoSectionToFilter.sectionGuid == null }
        .map { dtoCurrentParentSection ->
          val dtoChildrenOfCurrentParentSection =
              dtoMetadataSections.filter { it2 -> it2.sectionGuid == dtoCurrentParentSection.guid }
          if (dtoChildrenOfCurrentParentSection.isEmpty()) {
            // Parent section without children, meaning with metadata
            val dtoMetadatumOfCurrentParentSection =
                dtoMetadata.filter { dtoMetadatum ->
                  dtoCurrentParentSection.guid == dtoMetadatum.sectionGuid
                }
            val convertedSection =
                metadataSectionToEntity(
                    dtoCurrentParentSection,
                    dtoMetadatumOfCurrentParentSection.map { metadatumToEntity(it) })
            listDomainSections.add(convertedSection)
          } else {
            // Parent section with children (assumming without metadata)
            val listChildrenDomain = mutableListOf<MetadataSection>()
            dtoChildrenOfCurrentParentSection.map { dtoChildOfCurrentParentSection ->
              val dtoMetadatumOfChild =
                  dtoMetadata.filter { dtoMetadatum ->
                    dtoChildOfCurrentParentSection.guid == dtoMetadatum.sectionGuid
                  }
              val convertedChildSection =
                  metadataSectionToEntity(
                      dtoChildOfCurrentParentSection,
                      dtoMetadatumOfChild.map { metadatumToEntity(it) })
              listChildrenDomain.add(convertedChildSection)
            }
            val domainParent =
                MetadataSection(
                    name = dtoCurrentParentSection.name,
                    order = dtoCurrentParentSection.order,
                    guid = dtoCurrentParentSection.guid,
                    metadata = emptyList(),
                    sections = listChildrenDomain)
            listDomainSections.add(domainParent)
          }
        }

    // Move flat list SectionElement to nested structure and add ContentElement to SectionElements
    // 1. We first take those sections, with child content elements --> Articles, since these don't
    // have child sections, and we save them in a Pair list of the transformed SectionElement with
    // the corresponding foreign key of the parent
    val sections = mutableListOf<Pair<UUID?, MutableList<SectionElement>>>()
    val contentsBySectionGuid: Map<UUID?, List<ContentDto>> =
        contentsNotNormLevel.groupBy { it.sectionGuid }
    contentsBySectionGuid.forEach { entry ->
      val parentSectionDto = sectionsDto.find { sectionDto -> sectionDto.guid == entry.key }
      if (parentSectionDto != null) {
        val parentSection =
            sectionElementToEntity(
                parentSectionDto, null, entry.value.map { contentElementToEntity(it) })
        val sectionWithSectionGuidAlreadyPresent =
            sections.find { it.first == parentSectionDto.sectionGuid }
        if (sectionWithSectionGuidAlreadyPresent != null) {
          sectionWithSectionGuidAlreadyPresent.second.add(parentSection)
        } else {
          sections.add(Pair(parentSectionDto.sectionGuid, mutableListOf(parentSection)))
        }
      }
    }

    // CHECK 2. if there are sections with no content at the end of the branch, add code for that

    // 3. Now we put those initial article sections recursively into their corresponding parent
    // sections till there is now parent section left
    var newSections: List<Pair<UUID?, MutableList<SectionElement>>> = sections
    do {
      newSections = findParentSections(newSections, sectionsDto)
    } while (newSections.any { it.first != null })

    return Norm(
        normDto.guid,
        listDomainSections,
        fileReferences,
        newSections.flatMap { it.second },
        contentsNormLevel)
  }

  private fun findParentSections(
      sections: List<Pair<UUID?, MutableList<SectionElement>>>,
      sectionsDto: List<SectionDto>
  ): List<Pair<UUID?, MutableList<SectionElement>>> {
    val newSections = mutableListOf<Pair<UUID?, MutableList<SectionElement>>>()
    sections.forEach { section ->
      val parentSectionGuid = section.first
      if (parentSectionGuid == null) {
        newSections.add(section)
        return@forEach
      }
      val childSectionElements = section.second
      val parentSectionDto = sectionsDto.find { sectionDto -> sectionDto.guid == parentSectionGuid }
      if (parentSectionDto != null) {
        val parentSection =
            sectionElementToEntity(
                parentSectionDto, sectionElements = childSectionElements, contentElements = null)
        val sectionWithSectionGuidAlreadyPresent =
            newSections
                .filter { it.first != null }
                .find { it.first == parentSectionDto.sectionGuid }
        if (sectionWithSectionGuidAlreadyPresent != null) {
          sectionWithSectionGuidAlreadyPresent.second.add(parentSection)
        } else {
          newSections.add(Pair(parentSectionDto.sectionGuid, mutableListOf(parentSection)))
        }
      }
    }
    return newSections
  }

  fun metadataSectionToEntity(
      metadataSectionDto: MetadataSectionDto,
      metadata: List<Metadatum<*>>
  ): MetadataSection {
    return MetadataSection(
        name = metadataSectionDto.name,
        order = metadataSectionDto.order,
        metadata = metadata,
        guid = metadataSectionDto.guid)
  }

  fun fileReferenceToEntity(fileReferenceDto: FileReferenceDto): FileReference {
    return FileReference(
        fileReferenceDto.name,
        fileReferenceDto.hash,
        fileReferenceDto.createdAt,
        fileReferenceDto.guid)
  }

  fun contentElementToEntity(contentDto: ContentDto): ContentElement {
    return when (contentDto.type) {
      ContentElementType.PREAMBLE ->
          Preamble(contentDto.guid, contentDto.order, contentDto.marker, contentDto.text)
      ContentElementType.PARAGRAPH ->
          Paragraph(contentDto.guid, contentDto.order, contentDto.marker, contentDto.text)
      ContentElementType.CLOSING ->
          Closing(contentDto.guid, contentDto.order, contentDto.marker, contentDto.text)
    }
  }

  fun sectionElementToEntity(
      sectionDto: SectionDto,
      sectionElements: List<SectionElement>?,
      contentElements: List<ContentElement>?
  ): SectionElement {
    return when (sectionDto.type) {
      SectionElementType.BOOK ->
          Book(
              sectionDto.header,
              sectionDto.guid,
              sectionDto.designation,
              sectionDto.order,
              childSections = sectionElements)
      SectionElementType.PART ->
          Part(
              sectionDto.header,
              sectionDto.guid,
              sectionDto.designation,
              sectionDto.order,
              childSections = sectionElements)
      SectionElementType.CHAPTER ->
          Chapter(
              sectionDto.header,
              sectionDto.guid,
              sectionDto.designation,
              sectionDto.order,
              childSections = sectionElements)
      SectionElementType.SUBCHAPTER ->
          Subchapter(
              sectionDto.header,
              sectionDto.guid,
              sectionDto.designation,
              sectionDto.order,
              childSections = sectionElements)
      SectionElementType.SECTION ->
          Section(
              sectionDto.header,
              sectionDto.guid,
              sectionDto.designation,
              sectionDto.order,
              childSections = sectionElements)
      SectionElementType.ARTICLE ->
          Article(
              sectionDto.header,
              sectionDto.guid,
              sectionDto.designation,
              sectionDto.order,
              contentElements ?: listOf())
      SectionElementType.SUBSECTION ->
          Subsection(
              sectionDto.header,
              sectionDto.guid,
              sectionDto.designation,
              sectionDto.order,
              childSections = sectionElements)
      SectionElementType.TITLE ->
          Title(
              sectionDto.header,
              sectionDto.guid,
              sectionDto.designation,
              sectionDto.order,
              childSections = sectionElements)
      SectionElementType.SUBTITLE ->
          Subtitle(
              sectionDto.header,
              sectionDto.guid,
              sectionDto.designation,
              sectionDto.order,
              childSections = sectionElements)
      SectionElementType.UNCATEGORIZED ->
          Uncategorized(
              sectionDto.header,
              sectionDto.guid,
              sectionDto.designation,
              sectionDto.order,
              childSections = sectionElements)
    }
  }

  fun metadatumToEntity(metadatumDto: MetadatumDto): Metadatum<*> {
    val value =
        when (metadatumDto.type) {
          MetadatumType.DATE -> LocalDate.parse(metadatumDto.value)
          MetadatumType.TIME -> LocalTime.parse(metadatumDto.value)
          MetadatumType.RESOLUTION_MAJORITY -> metadatumDto.value.toBoolean()
          MetadatumType.NORM_CATEGORY -> NormCategory.valueOf(metadatumDto.value)
          MetadatumType.UNDEFINED_DATE -> UndefinedDate.valueOf(metadatumDto.value)
          else -> metadatumDto.value
        }

    return Metadatum(value, metadatumDto.type, metadatumDto.order, metadatumDto.guid)
  }

  fun normToDto(norm: Norm): NormDto {
    return NormDto(norm.guid, norm.eGesetzgebung)
  }

  fun sectionsToDto(
      sections: List<SectionElement>,
      normGuid: UUID,
      sectionGuid: UUID?
  ): List<SectionDto> {
    return sections.map {
      val type =
          when (it) {
            is Book -> SectionElementType.BOOK
            is Article -> SectionElementType.ARTICLE
            is Chapter -> SectionElementType.CHAPTER
            is Part -> SectionElementType.PART
            is Section -> SectionElementType.SECTION
            is Subchapter -> SectionElementType.SUBCHAPTER
            is Subsection -> SectionElementType.SUBSECTION
            is Subtitle -> SectionElementType.SUBTITLE
            is Title -> SectionElementType.TITLE
            is Uncategorized -> SectionElementType.UNCATEGORIZED
          }
      SectionDto(it.guid, type, it.designation, it.header, it.order, sectionGuid, normGuid)
    }
  }

  fun articlesToDto(articles: List<Article>, normGuid: UUID, sectionGuid: UUID?): List<SectionDto> {
    return articles.map {
      SectionDto(
          it.guid,
          SectionElementType.ARTICLE,
          it.designation,
          it.header,
          it.order,
          normGuid = normGuid,
          sectionGuid = sectionGuid)
    }
  }

  fun contentsToDto(
      contents: Collection<ContentElement>,
      normGuid: UUID?,
      sectionGuid: UUID?
  ): List<ContentDto> {
    return contents.map {
      val type =
          when (it) {
            is Preamble -> ContentElementType.PREAMBLE
            is Paragraph -> ContentElementType.PARAGRAPH
            is Closing -> ContentElementType.CLOSING
          }
      ContentDto(
          it.guid,
          type,
          it.marker,
          it.text,
          it.order,
          normGuid = normGuid,
          sectionGuid = sectionGuid)
    }
  }

  fun fileReferencesToDto(
      fileReferences: List<FileReference>,
      normGuid: UUID
  ): List<FileReferenceDto> {
    return fileReferences.map { fileReferenceToDto(fileReference = it, normGuid = normGuid) }
  }

  fun fileReferenceToDto(fileReference: FileReference, normGuid: UUID): FileReferenceDto {
    return FileReferenceDto(
        fileReference.guid,
        fileReference.name,
        fileReference.hash,
        normGuid,
        fileReference.createdAt)
  }

  fun metadataListToDto(metadata: List<Metadatum<*>>, sectionGuid: UUID): List<MetadatumDto> {
    return metadata.map {
      MetadatumDto(
          value = it.value.toString(),
          type = it.type,
          order = it.order,
          guid = it.guid,
          sectionGuid = sectionGuid)
    }
  }

  fun metadataSectionToDto(
      metadataSection: MetadataSection,
      normGuid: UUID,
      sectionGuid: UUID? = null
  ): MetadataSectionDto {
    return MetadataSectionDto(
        guid = metadataSection.guid,
        name = metadataSection.name,
        order = metadataSection.order,
        sectionGuid = sectionGuid,
        normGuid = normGuid)
  }

  fun metadataSectionsToDto(
      sections: List<MetadataSection>,
      sectionGuid: UUID? = null,
      normGuid: UUID
  ): List<MetadataSectionDto> {
    return sections.map {
      metadataSectionToDto(
          it,
          normGuid,
          sectionGuid,
      )
    }
  }
}
