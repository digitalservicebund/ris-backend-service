package de.bund.digitalservice.ris.norms.framework.adapter.output.database

import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.FileReference
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.NormCategory
import de.bund.digitalservice.ris.norms.domain.value.OtherType
import de.bund.digitalservice.ris.norms.domain.value.ProofIndication
import de.bund.digitalservice.ris.norms.domain.value.ProofType
import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ArticleDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.FileReferenceDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.MetadataSectionDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.MetadatumDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ParagraphDto
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID

interface NormsMapper {
    fun normToEntity(
        normDto: NormDto,
        articles: List<Article>,
        fileReferences: List<FileReference>,
        dtoSections: List<MetadataSectionDto>,
        dtoMetadata: List<MetadatumDto> = emptyList(),
    ): Norm {
        val listDomainSections = mutableListOf<MetadataSection>()

        // 1. Objective: move children from parent level to their respective parents because otherwise we can't instantite the parent MetadataSection
        dtoSections.filter { dtoSectionToFilter -> dtoSectionToFilter.sectionGuid == null }.map { dtoCurrentParentSection ->
            val dtoChildrenOfCurrentParentSection = dtoSections.filter { it2 -> it2.sectionGuid == dtoCurrentParentSection.guid }
            if (dtoChildrenOfCurrentParentSection.isEmpty()) {
                // Parent section without children, meaning with metadata
                val dtoMetadatumOfCurrentParentSection = dtoMetadata.filter { dtoMetadatum -> dtoCurrentParentSection.guid == dtoMetadatum.sectionGuid }
                val convertedSection = metadataSectionToEntity(dtoCurrentParentSection, dtoMetadatumOfCurrentParentSection.map { metadatumToEntity(it) })
                listDomainSections.add(convertedSection)
            } else {
                // Parent section with children (assumming without metadata)
                val listChildrenDomain = mutableListOf<MetadataSection>()
                dtoChildrenOfCurrentParentSection.map { dtoChildOfCurrentParentSection ->
                    val dtoMetadatumOfChild = dtoMetadata.filter { dtoMetadatum -> dtoChildOfCurrentParentSection.guid == dtoMetadatum.sectionGuid }
                    val convertedChildSection = metadataSectionToEntity(dtoChildOfCurrentParentSection, dtoMetadatumOfChild.map { metadatumToEntity(it) })
                    listChildrenDomain.add(convertedChildSection)
                }
                val domainParent = MetadataSection(name = dtoCurrentParentSection.name, order = dtoCurrentParentSection.order, guid = dtoCurrentParentSection.guid, metadata = emptyList(), sections = listChildrenDomain)
                listDomainSections.add(domainParent)
            }
        }

        return Norm(
            normDto.guid,
            articles,
            listDomainSections,
            normDto.announcementDate,
            fileReferences,
        )
    }

    fun paragraphToEntity(paragraphDto: ParagraphDto): Paragraph {
        return Paragraph(paragraphDto.guid, paragraphDto.marker, paragraphDto.text)
    }

    fun articleToEntity(articleDto: ArticleDto, paragraphs: List<Paragraph>): Article {
        return Article(articleDto.guid, articleDto.title, articleDto.marker, paragraphs)
    }

    fun metadataSectionToEntity(metadataSectionDto: MetadataSectionDto, metadata: List<Metadatum<*>>): MetadataSection {
        return MetadataSection(name = metadataSectionDto.name, order = metadataSectionDto.order, metadata = metadata, guid = metadataSectionDto.guid)
    }

    fun fileReferenceToEntity(fileReferenceDto: FileReferenceDto): FileReference {
        return FileReference(fileReferenceDto.name, fileReferenceDto.hash, fileReferenceDto.createdAt, fileReferenceDto.guid)
    }

    fun metadatumToEntity(metadatumDto: MetadatumDto): Metadatum<*> {
        val value = when (metadatumDto.type) {
            MetadatumType.DATE -> LocalDate.parse(metadatumDto.value)
            MetadatumType.TIME -> LocalTime.parse(metadatumDto.value)
            MetadatumType.RESOLUTION_MAJORITY -> metadatumDto.value.toBoolean()
            MetadatumType.NORM_CATEGORY -> NormCategory.valueOf(metadatumDto.value)
            MetadatumType.UNDEFINED_DATE -> UndefinedDate.valueOf(metadatumDto.value)
            MetadatumType.PROOF_INDICATION -> ProofIndication.valueOf(metadatumDto.value)
            MetadatumType.PROOF_TYPE -> ProofType.valueOf(metadatumDto.value)
            MetadatumType.OTHER_TYPE -> OtherType.valueOf(metadatumDto.value)
            else -> metadatumDto.value
        }

        return Metadatum(value, metadatumDto.type, metadatumDto.order, metadatumDto.guid)
    }

    fun normToDto(norm: Norm): NormDto {
        return NormDto(
            norm.guid,
            norm.announcementDate,
        )
    }

    fun articlesToDto(articles: List<Article>, normGuid: UUID): List<ArticleDto> {
        return articles.map { ArticleDto(it.guid, it.title, it.marker, normGuid) }
    }

    fun paragraphsToDto(paragraphs: List<Paragraph>, articleGuid: UUID): List<ParagraphDto> {
        return paragraphs.map { ParagraphDto(it.guid, it.marker, it.text, articleGuid) }
    }

    fun fileReferencesToDto(fileReferences: List<FileReference>, normGuid: UUID): List<FileReferenceDto> {
        return fileReferences.map { fileReferenceToDto(fileReference = it, normGuid = normGuid) }
    }

    fun fileReferenceToDto(fileReference: FileReference, normGuid: UUID): FileReferenceDto {
        return FileReferenceDto(fileReference.guid, fileReference.name, fileReference.hash, normGuid, fileReference.createdAt)
    }

    fun metadataListToDto(metadata: List<Metadatum<*>>, sectionGuid: UUID): List<MetadatumDto> {
        return metadata.map { MetadatumDto(value = it.value.toString(), type = it.type, order = it.order, guid = it.guid, sectionGuid = sectionGuid) }
    }

    fun metadataSectionToDto(metadataSection: MetadataSection, normGuid: UUID, sectionGuid: UUID? = null): MetadataSectionDto {
        return MetadataSectionDto(guid = metadataSection.guid, name = metadataSection.name, order = metadataSection.order, sectionGuid = sectionGuid, normGuid = normGuid)
    }

    fun metadataSectionsToDto(sections: List<MetadataSection>, sectionGuid: UUID? = null, normGuid: UUID): List<MetadataSectionDto> {
        return sections.map {
            metadataSectionToDto(
                it,
                normGuid,
                sectionGuid,
            )
        }
    }
}
