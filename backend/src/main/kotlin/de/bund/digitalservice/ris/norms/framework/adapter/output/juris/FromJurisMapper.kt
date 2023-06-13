package de.bund.digitalservice.ris.norms.framework.adapter.output.juris

import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.NormCategory
import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.decodeLocalDate
import de.bund.digitalservice.ris.norms.juris.converter.model.DigitalAnnouncement
import de.bund.digitalservice.ris.norms.juris.converter.model.DivergentEntryIntoForce
import de.bund.digitalservice.ris.norms.juris.converter.model.DivergentExpiration
import de.bund.digitalservice.ris.norms.juris.converter.model.DocumentType
import de.bund.digitalservice.ris.norms.juris.converter.model.NormProvider
import de.bund.digitalservice.ris.norms.juris.converter.model.PrintAnnouncement
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.*
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName as Section
import de.bund.digitalservice.ris.norms.juris.converter.model.Article as ArticleData
import de.bund.digitalservice.ris.norms.juris.converter.model.Norm as NormData
import de.bund.digitalservice.ris.norms.juris.converter.model.Paragraph as ParagraphData

fun mapDataToDomain(guid: UUID, data: NormData): Norm {
    val participationType = createMetadataForType(data.participationList.mapNotNull { it.type }, MetadatumType.PARTICIPATION_TYPE)
    val participationInstitution = createMetadataForType(data.participationList.mapNotNull { it.institution }, MetadatumType.PARTICIPATION_INSTITUTION)

    val leadJurisdiction = createMetadataForType(data.leadList.mapNotNull { it.jurisdiction }, MetadatumType.LEAD_JURISDICTION)
    val leadUnit = createMetadataForType(data.leadList.mapNotNull { it.unit }, MetadatumType.LEAD_UNIT)

    val subjectFna = createMetadataForType(data.subjectAreaList.mapNotNull { it.fna }, MetadatumType.SUBJECT_FNA)
    val subjectGesta = createMetadataForType(data.subjectAreaList.mapNotNull { it.gesta }, MetadatumType.SUBJECT_GESTA)

    val citationDateSections = data.citationDateList.mapIndexed { index, value ->
        if (value.length == 4 && value.toIntOrNull() != null) {
            MetadataSection(Section.CITATION_DATE, listOf(Metadatum(value, MetadatumType.YEAR, 1)), index)
        } else if (value.length > 4 && parseDateString(value) != null) {
            MetadataSection(Section.CITATION_DATE, listOf(Metadatum(parseDateString(value), MetadatumType.DATE, 1)), index)
        } else {
            null
        }
    }

    val ageIndicationSections = data.ageIndicationStartList.mapIndexed { index, value ->
        MetadataSection(Section.AGE_INDICATION, listOf(Metadatum(value, MetadatumType.RANGE_START, 1)), index)
    }

    val categorizedReferenceSections = data.categorizedReferences.mapIndexed { index, value ->
        MetadataSection(Section.CATEGORIZED_REFERENCE, listOf(Metadatum(value.text, MetadatumType.TEXT)), index)
    }

    val sections = listOf(createSectionForNorm(data)) +
        createSectionsWithoutGrouping(Section.SUBJECT_AREA, subjectFna + subjectGesta) +
        createSectionsFromMetadata(Section.LEAD, leadJurisdiction + leadUnit) +
        createSectionForDocumentType(data.documentType) +
        createSectionsFromMetadata(Section.PARTICIPATION, participationInstitution + participationType) +
        createSectionsForOfficialReference(data.digitalAnnouncementList, data.printAnnouncementList) +
        createSectionsForDivergentEntryIntoForce(data.divergentEntryIntoForceList) +
        createSectionsForDivergentExpiration(data.divergentExpirationsList) +
        citationDateSections + ageIndicationSections + categorizedReferenceSections +
        addProviderSections(data.normProviderList) +
        createSectionForEntryIntoForceAndExpiration(data)

    return Norm(
        guid = guid,
        articles = mapArticlesToDomain(data.articles),
        metadataSections = sections.filterNotNull(),
        officialLongTitle = data.officialLongTitle ?: "",
        risAbbreviation = data.risAbbreviation,
        documentCategory = data.documentCategory,
        officialShortTitle = data.officialShortTitle,
        officialAbbreviation = data.officialAbbreviation,
        announcementDate = parseDateString(data.announcementDate),
        statusNote = data.statusNote,
        statusDescription = data.statusDescription,
        statusDate = parseDateString(data.statusDate),
        statusReference = data.statusReference,
        repealNote = data.repealNote,
        repealArticle = data.repealArticle,
        repealDate = parseDateString(data.repealDate),
        repealReferences = data.repealReferences,
        reissueNote = data.reissueNote,
        reissueArticle = data.reissueArticle,
        reissueDate = parseDateString(data.reissueDate),
        reissueReference = data.reissueReference,
        otherStatusNote = data.otherStatusNote,
        documentStatusWorkNote = data.documentStatusWorkNote,
        documentStatusDescription = data.documentStatusDescription,
        documentStatusDate = parseDateString(data.documentStatusDate),
        applicationScopeArea = data.applicationScopeArea,
        applicationScopeStartDate = parseDateString(data.applicationScopeStartDate),
        applicationScopeEndDate = parseDateString(data.applicationScopeEndDate),
        otherFootnote = data.otherFootnote,
        celexNumber = data.celexNumber,
        text = data.text,
    )
}

private fun createSectionForDocumentType(documentType: DocumentType?): MetadataSection? {
    return if (documentType !== null) {
        val documentNormCategories = createMetadataForType(documentType.categories.mapNotNull { parseNormCategory(it) }, MetadatumType.NORM_CATEGORY)
        val documentTemplateNames = createMetadataForType(documentType.templateNames.map { it }, MetadatumType.TEMPLATE_NAME)
        val metadata = (documentNormCategories + documentTemplateNames).toMutableList()
        if (documentType.name != null) {
            metadata += createMetadataForType(listOf(documentType.name), MetadatumType.TYPE_NAME)
        }
        MetadataSection(Section.DOCUMENT_TYPE, metadata)
    } else {
        null
    }
}

private fun createSectionForNorm(data: NormData): MetadataSection {
    val divergentDocumentNumber = data.divergentDocumentNumber?.let { listOf(Metadatum(data.divergentDocumentNumber, MetadatumType.DIVERGENT_DOCUMENT_NUMBER, 1)) } ?: listOf()
    val frameKeywords = createMetadataForType(data.frameKeywordList, MetadatumType.KEYWORD)
    val risAbbreviationInternationalLaw = createMetadataForType(data.risAbbreviationInternationalLawList, MetadatumType.RIS_ABBREVIATION_INTERNATIONAL_LAW)
    val unofficialLongTitle = createMetadataForType(data.unofficialLongTitleList, MetadatumType.UNOFFICIAL_LONG_TITLE)
    val unofficialShortTitle = createMetadataForType(data.unofficialShortTitleList, MetadatumType.UNOFFICIAL_SHORT_TITLE)
    val unofficialAbbreviation = createMetadataForType(data.unofficialAbbreviationList, MetadatumType.UNOFFICIAL_ABBREVIATION)
    val unofficialReference = createMetadataForType(data.unofficialReferenceList, MetadatumType.UNOFFICIAL_REFERENCE)
    val referenceNumber = createMetadataForType(data.referenceNumberList, MetadatumType.REFERENCE_NUMBER)
    val definition = createMetadataForType(data.definitionList, MetadatumType.DEFINITION)
    val ageOfMajorityIndication = createMetadataForType(data.ageOfMajorityIndicationList, MetadatumType.AGE_OF_MAJORITY_INDICATION)
    val validityRule = createMetadataForType(data.validityRuleList, MetadatumType.VALIDITY_RULE)

    return MetadataSection(Section.NORM, frameKeywords + divergentDocumentNumber + risAbbreviationInternationalLaw + unofficialAbbreviation + unofficialShortTitle + unofficialLongTitle + unofficialReference + referenceNumber + definition + ageOfMajorityIndication + validityRule)
}

private fun createSectionsForDivergentEntryIntoForce(data: List<DivergentEntryIntoForce>): List<MetadataSection> {
    val definedDate = createMetadataForType(data.filter { it.state == null }.mapNotNull { parseDateString(it.date) }, MetadatumType.DATE)
    val definedCategory = createMetadataForType(data.filter { it.state == null }.mapNotNull { parseNormCategory(it.normCategory) }, MetadatumType.NORM_CATEGORY)
    val undefinedDate = createMetadataForType(data.mapNotNull { parseDateStateString(it.state) }, MetadatumType.UNDEFINED_DATE)
    val undefinedCategory = createMetadataForType(data.filter { it.state != null }.mapNotNull { parseNormCategory(it.normCategory) }, MetadatumType.NORM_CATEGORY)

    val sections = createSectionsFromMetadata(Section.DIVERGENT_ENTRY_INTO_FORCE_DEFINED, definedDate + definedCategory) +
        createSectionsFromMetadata(Section.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED, undefinedDate + undefinedCategory)

    return sections.mapIndexed { index, section -> MetadataSection(Section.DIVERGENT_ENTRY_INTO_FORCE, listOf(), index, listOf(section)) }
}

private fun createSectionsForDivergentExpiration(data: List<DivergentExpiration>): List<MetadataSection> {
    val definedDate = createMetadataForType(data.filter { it.state == null }.mapNotNull { parseDateString(it.date) }, MetadatumType.DATE)
    val definedCategory = createMetadataForType(data.filter { it.state == null }.mapNotNull { parseNormCategory(it.normCategory) }, MetadatumType.NORM_CATEGORY)
    val undefinedDate = createMetadataForType(data.mapNotNull { parseDateStateString(it.state) }, MetadatumType.UNDEFINED_DATE)
    val undefinedCategory = createMetadataForType(data.filter { it.state != null }.mapNotNull { parseNormCategory(it.normCategory) }, MetadatumType.NORM_CATEGORY)

    val sections = createSectionsFromMetadata(Section.DIVERGENT_EXPIRATION_DEFINED, definedDate + definedCategory) +
        createSectionsFromMetadata(Section.DIVERGENT_EXPIRATION_UNDEFINED, undefinedDate + undefinedCategory)

    return sections.mapIndexed { index, section -> MetadataSection(Section.DIVERGENT_EXPIRATION, listOf(), index, listOf(section)) }
}

fun createSectionForEntryIntoForceAndExpiration(data: NormData): List<MetadataSection> {
    val sectionList = mutableListOf<MetadataSection>()

    if (data.entryIntoForceDate !== null) {
        val metadata = Metadatum(decodeLocalDate(data.entryIntoForceDate), MetadatumType.DATE)
        sectionList.add(MetadataSection(Section.ENTRY_INTO_FORCE, listOf(metadata)))
    } else if (data.entryIntoForceDateState !== null) {
        val metadata = Metadatum(parseDateStateString(data.entryIntoForceDateState), MetadatumType.UNDEFINED_DATE)
        sectionList.add(MetadataSection(Section.ENTRY_INTO_FORCE, listOf(metadata)))
    }

    if (data.principleEntryIntoForceDate !== null) {
        val metadata = Metadatum(decodeLocalDate(data.principleEntryIntoForceDate), MetadatumType.DATE)
        sectionList.add(MetadataSection(Section.PRINCIPLE_ENTRY_INTO_FORCE, listOf(metadata)))
    } else if (data.principleEntryIntoForceDateState !== null) {
        val metadata = Metadatum(parseDateStateString(data.principleEntryIntoForceDateState), MetadatumType.UNDEFINED_DATE)
        sectionList.add(MetadataSection(Section.PRINCIPLE_ENTRY_INTO_FORCE, listOf(metadata)))
    }

    if (data.expirationDate !== null) {
        val metadata = Metadatum(decodeLocalDate(data.expirationDate), MetadatumType.DATE)
        sectionList.add(MetadataSection(Section.EXPIRATION, listOf(metadata)))
    } else if (data.expirationDateState !== null) {
        val metadata = Metadatum(parseDateStateString(data.expirationDateState), MetadatumType.UNDEFINED_DATE)
        sectionList.add(MetadataSection(Section.EXPIRATION, listOf(metadata)))
    }

    if (data.principleExpirationDate !== null) {
        val metadata = Metadatum(decodeLocalDate(data.principleExpirationDate), MetadatumType.DATE)
        sectionList.add(MetadataSection(Section.PRINCIPLE_EXPIRATION, listOf(metadata)))
    } else if (data.principleExpirationDateState !== null) {
        val metadata = Metadatum(parseDateStateString(data.principleExpirationDateState), MetadatumType.UNDEFINED_DATE)
        sectionList.add(MetadataSection(Section.PRINCIPLE_EXPIRATION, listOf(metadata)))
    }

    return sectionList
}

private fun createSectionsForOfficialReference(digitalAnnouncement: List<DigitalAnnouncement>, printAnnouncement: List<PrintAnnouncement>): List<MetadataSection> {
    val printAnnouncementGazette = createMetadataForType(printAnnouncement.mapNotNull { it.gazette }, MetadatumType.ANNOUNCEMENT_GAZETTE)
    val printAnnouncementPage = createMetadataForType(printAnnouncement.mapNotNull { it.page }, MetadatumType.PAGE)
    val printAnnouncementYear = createMetadataForType(printAnnouncement.mapNotNull { it.year }, MetadatumType.YEAR)
    val digitalAnnouncementYear = createMetadataForType(digitalAnnouncement.mapNotNull { it.year }, MetadatumType.YEAR)
    val digitalAnnouncementNumber = createMetadataForType(digitalAnnouncement.mapNotNull { it.number }, MetadatumType.EDITION)
    val digitalAnnouncementMedium = createMetadataForType(digitalAnnouncement.mapNotNull { it.medium }, MetadatumType.ANNOUNCEMENT_MEDIUM)

    val referenceSections = createSectionsFromMetadata(Section.PRINT_ANNOUNCEMENT, printAnnouncementGazette + printAnnouncementYear + printAnnouncementPage) +
        createSectionsFromMetadata(Section.DIGITAL_ANNOUNCEMENT, digitalAnnouncementNumber + digitalAnnouncementMedium + digitalAnnouncementYear)

    return referenceSections.mapIndexed { index, section -> MetadataSection(Section.OFFICIAL_REFERENCE, listOf(), index, listOf(section)) }
}

fun addProviderSections(normProviders: List<NormProvider>): List<MetadataSection> {
    return normProviders.mapIndexed { index, normProvider ->
        val metadata = mutableListOf<Metadatum<*>>()
        if (normProvider.entity !== null) {
            metadata.add(Metadatum(normProvider.entity, MetadatumType.ENTITY, 1))
        }
        if (normProvider.decidingBody !== null) {
            metadata.add(Metadatum(normProvider.decidingBody, MetadatumType.DECIDING_BODY, 1))
        }
        if (normProvider.isResolutionMajority !== null) {
            metadata.add(Metadatum(normProvider.isResolutionMajority, MetadatumType.RESOLUTION_MAJORITY, 1))
        }
        if (metadata.size > 0) MetadataSection(Section.NORM_PROVIDER, metadata, index + 1) else null
    }.filterNotNull()
}

private fun createMetadataForType(data: List<*>, type: MetadatumType): List<Metadatum<*>> = data
    .mapIndexed { index, value -> Metadatum(value, type, index + 1) }

fun mapArticlesToDomain(articles: List<ArticleData>): List<Article> {
    return articles.map { article ->
        Article(
            guid = UUID.randomUUID(),
            title = article.title,
            marker = article.marker,
            paragraphs = mapParagraphsToDomain(article.paragraphs),
        )
    }
}

fun mapParagraphsToDomain(paragraphs: List<ParagraphData>): List<Paragraph> {
    return paragraphs.map { paragraph ->
        Paragraph(
            guid = UUID.randomUUID(),
            marker = paragraph.marker,
            text = paragraph.text,
        )
    }
}

fun parseDateString(value: String?): LocalDate? = value?.let { try { LocalDate.parse(value) } catch (e: DateTimeParseException) { null } }

fun parseNormCategory(value: String?): NormCategory? = when (value) {
    "SN" -> NormCategory.BASE_NORM
    "ÄN" -> NormCategory.AMENDMENT_NORM
    "ÜN" -> NormCategory.TRANSITIONAL_NORM
    else -> null
}

fun parseDateStateString(value: String?): UndefinedDate? =
    if (value.isNullOrEmpty()) null else UndefinedDate.valueOf(value)

fun createSectionsFromMetadata(sectionName: MetadataSectionName, metadata: List<Metadatum<*>>) = metadata
    .groupBy { it.order }
    .mapValues {
        MetadataSection(
            sectionName,
            it.value.map { metadatum -> Metadatum(metadatum.value, metadatum.type, 1) },
            it.key,
        )
    }.values

private fun createSectionsWithoutGrouping(sectionName: MetadataSectionName, metadata: List<Metadatum<*>>) = metadata
    .mapIndexed { index, metadatum ->
        MetadataSection(sectionName, listOf(Metadatum(metadatum.value, metadatum.type, 1)), index)
    }
