package de.bund.digitalservice.ris.norms.framework.adapter.output.database

import de.bund.digitalservice.ris.norms.application.port.output.SearchNormsOutputPort.QueryFields
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.FileReference
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.NormCategory
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ArticleDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.FileReferenceDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.MetadataSectionDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.MetadatumDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.NormDto
import de.bund.digitalservice.ris.norms.framework.adapter.output.database.dto.ParagraphDto
import java.time.LocalDate
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
        dtoSections.filter { dtoSectionToFilter -> dtoSectionToFilter.sectionId == null }.map { dtoCurrentParentSection ->
            val dtoChildrenOfCurrentParentSection = dtoSections.filter { it2 -> it2.sectionId == dtoCurrentParentSection.id }
            if (dtoChildrenOfCurrentParentSection.isEmpty()) {
                // Parent section without children, meaning with metadata
                val dtoMetadatumOfCurrentParentSection = dtoMetadata.filter { dtoMetadatum -> dtoCurrentParentSection.id == dtoMetadatum.sectionId }
                val convertedSection = metadataSectionToEntity(dtoCurrentParentSection, dtoMetadatumOfCurrentParentSection.map { metadatumToEntity(it) })
                listDomainSections.add(convertedSection)
            } else {
                // Parent section with children (assumming without metadata)
                val listChildrenDomain = mutableListOf<MetadataSection>()
                dtoChildrenOfCurrentParentSection.map { dtoChildOfCurrentParentSection ->
                    val dtoMetadatumOfChild = dtoMetadata.filter { dtoMetadatum -> dtoChildOfCurrentParentSection.id == dtoMetadatum.sectionId }
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
            normDto.officialLongTitle,
            normDto.risAbbreviation,
            normDto.documentNumber,
            normDto.documentCategory,
            normDto.officialShortTitle,
            normDto.officialAbbreviation,
            normDto.entryIntoForceDate,
            normDto.entryIntoForceDateState,
            normDto.principleEntryIntoForceDate,
            normDto.principleEntryIntoForceDateState,
            normDto.divergentEntryIntoForceDate,
            normDto.divergentEntryIntoForceDateState,
            normDto.entryIntoForceNormCategory,
            normDto.expirationDate,
            normDto.expirationDateState,
            normDto.isExpirationDateTemp,
            normDto.principleExpirationDate,
            normDto.principleExpirationDateState,
            normDto.divergentExpirationDate,
            normDto.divergentExpirationDateState,
            normDto.expirationNormCategory,
            normDto.announcementDate,
            normDto.publicationDate,
            normDto.completeCitation,
            normDto.statusNote,
            normDto.statusDescription,
            normDto.statusDate,
            normDto.statusReference,
            normDto.repealNote,
            normDto.repealArticle,
            normDto.repealDate,
            normDto.repealReferences,
            normDto.reissueNote,
            normDto.reissueArticle,
            normDto.reissueDate,
            normDto.reissueReference,
            normDto.otherStatusNote,
            normDto.documentStatusWorkNote,
            normDto.documentStatusDescription,
            normDto.documentStatusDate,
            normDto.documentStatusReference,
            normDto.documentStatusEntryIntoForceDate,
            normDto.documentStatusProof,
            normDto.documentTextProof,
            normDto.otherDocumentNote,
            normDto.applicationScopeArea,
            normDto.applicationScopeStartDate,
            normDto.applicationScopeEndDate,
            normDto.categorizedReference,
            normDto.otherFootnote,
            normDto.footnoteChange,
            normDto.footnoteComment,
            normDto.footnoteDecision,
            normDto.footnoteStateLaw,
            normDto.footnoteEuLaw,
            normDto.digitalEvidenceLink,
            normDto.digitalEvidenceRelatedData,
            normDto.digitalEvidenceExternalDataNote,
            normDto.digitalEvidenceAppendix,
            normDto.celexNumber,
            normDto.text,
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
        return FileReference(fileReferenceDto.name, fileReferenceDto.hash, fileReferenceDto.createdAt)
    }

    fun metadatumToEntity(metadatumDto: MetadatumDto): Metadatum<*> {
        val value = when (metadatumDto.type) {
            MetadatumType.DATE -> LocalDate.parse(metadatumDto.value)
            MetadatumType.RESOLUTION_MAJORITY -> metadatumDto.value.toBoolean()
            MetadatumType.NORM_CATEGORY -> NormCategory.valueOf(metadatumDto.value)
            else -> metadatumDto.value
        }

        return Metadatum(value, metadatumDto.type, metadatumDto.order, metadatumDto.guid)
    }

    fun normToDto(norm: Norm, id: Int = 0): NormDto {
        return NormDto(
            id,
            norm.guid,
            norm.officialLongTitle,
            norm.risAbbreviation,
            norm.documentNumber,
            norm.documentCategory,
            norm.officialShortTitle,
            norm.officialAbbreviation,
            norm.entryIntoForceDate,
            norm.entryIntoForceDateState,
            norm.principleEntryIntoForceDate,
            norm.principleEntryIntoForceDateState,
            norm.divergentEntryIntoForceDate,
            norm.divergentEntryIntoForceDateState,
            norm.entryIntoForceNormCategory,
            norm.expirationDate,
            norm.expirationDateState,
            norm.isExpirationDateTemp,
            norm.principleExpirationDate,
            norm.principleExpirationDateState,
            norm.divergentExpirationDate,
            norm.divergentExpirationDateState,
            norm.expirationNormCategory,
            norm.announcementDate,
            norm.publicationDate,
            norm.completeCitation,
            norm.statusNote,
            norm.statusDescription,
            norm.statusDate,
            norm.statusReference,
            norm.repealNote,
            norm.repealArticle,
            norm.repealDate,
            norm.repealReferences,
            norm.reissueNote,
            norm.reissueArticle,
            norm.reissueDate,
            norm.reissueReference,
            norm.otherStatusNote,
            norm.documentStatusWorkNote,
            norm.documentStatusDescription,
            norm.documentStatusDate,
            norm.documentStatusReference,
            norm.documentStatusEntryIntoForceDate,
            norm.documentStatusProof,
            norm.documentTextProof,
            norm.otherDocumentNote,
            norm.applicationScopeArea,
            norm.applicationScopeStartDate,
            norm.applicationScopeEndDate,
            norm.categorizedReference,
            norm.otherFootnote,
            norm.footnoteChange,
            norm.footnoteComment,
            norm.footnoteDecision,
            norm.footnoteStateLaw,
            norm.footnoteEuLaw,
            norm.digitalEvidenceLink,
            norm.digitalEvidenceRelatedData,
            norm.digitalEvidenceExternalDataNote,
            norm.digitalEvidenceAppendix,
            norm.celexNumber,
            norm.text,
        )
    }

    fun articlesToDto(articles: List<Article>, normId: Int, id: Int = 0): List<ArticleDto> {
        return articles.map { ArticleDto(id, it.guid, it.title, it.marker, normId) }
    }

    fun paragraphsToDto(paragraphs: List<Paragraph>, articleId: Int, id: Int = 0): List<ParagraphDto> {
        return paragraphs.map { ParagraphDto(id, it.guid, it.marker, it.text, articleId) }
    }

    fun fileReferencesToDto(fileReferences: List<FileReference>, normId: Int, id: Int = 0): List<FileReferenceDto> {
        return fileReferences.map { fileReferenceToDto(it, normId) }
    }

    fun fileReferenceToDto(fileReference: FileReference, normId: Int, id: Int = 0): FileReferenceDto {
        return FileReferenceDto(id, fileReference.name, fileReference.hash, normId, fileReference.createdAt)
    }

    fun metadataListToDto(metadata: List<Metadatum<*>>, sectionId: Int, sectionGuid: UUID, id: Int = 0): List<MetadatumDto> {
        return metadata.map { MetadatumDto(id = id, value = it.value.toString(), type = it.type, order = it.order, sectionId = sectionId, guid = it.guid, sectionGuid = sectionGuid) }
    }

    fun metadataSectionToDto(metadataSection: MetadataSection, normId: Int, sectionId: Int? = null, id: Int = 0): MetadataSectionDto {
        return MetadataSectionDto(id = id, guid = metadataSection.guid, name = metadataSection.name, normId = normId, order = metadataSection.order, sectionId = sectionId)
    }

    fun metadataSectionsToDto(sections: List<MetadataSection>, normId: Int, sectionId: Int? = null, id: Int = 0): List<MetadataSectionDto> {
        return sections.map {
            metadataSectionToDto(
                it,
                normId,
                sectionId,
            )
        }
    }

    // TODO Add UNOFFICIAL_LONG_TITLE & UNOFFICIAL_SHORT_TITLE once all metadata are migrated
    fun queryFieldToDbColumn(field: QueryFields): String {
        return when (field) {
            QueryFields.PRINT_ANNOUNCEMENT_PAGE -> "print_announcement_page"
            QueryFields.ANNOUNCEMENT_DATE -> "announcement_date"
            QueryFields.PRINT_ANNOUNCEMENT_GAZETTE -> "print_announcement_gazette"
            QueryFields.CITATION_DATE -> "citation_date"
            QueryFields.CITATION_YEAR -> "citation_year"
            QueryFields.OFFICIAL_LONG_TITLE -> "official_long_title"
            QueryFields.OFFICIAL_SHORT_TITLE -> "official_short_title"
        }
    }
}
