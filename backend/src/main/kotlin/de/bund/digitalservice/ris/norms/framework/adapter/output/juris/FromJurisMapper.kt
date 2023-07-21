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
import de.bund.digitalservice.ris.norms.juris.converter.model.DigitalAnnouncement
import de.bund.digitalservice.ris.norms.juris.converter.model.DivergentEntryIntoForce
import de.bund.digitalservice.ris.norms.juris.converter.model.DivergentExpiration
import de.bund.digitalservice.ris.norms.juris.converter.model.DocumentStatus
import de.bund.digitalservice.ris.norms.juris.converter.model.DocumentType
import de.bund.digitalservice.ris.norms.juris.converter.model.Footnote
import de.bund.digitalservice.ris.norms.juris.converter.model.NormProvider
import de.bund.digitalservice.ris.norms.juris.converter.model.PrintAnnouncement
import de.bund.digitalservice.ris.norms.juris.converter.model.Reissue
import de.bund.digitalservice.ris.norms.juris.converter.model.Status
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
        } else if (value.length > 4) {
            val date = parseDateString(value)
            date?.let { MetadataSection(Section.CITATION_DATE, listOf(Metadatum(date, MetadatumType.DATE, 1)), index) }
        } else {
            null
        }
    }

    val ageIndicationSections = data.ageIndicationStartList.mapIndexed { index, value ->
        MetadataSection(Section.AGE_INDICATION, listOf(Metadatum(value, MetadatumType.RANGE_START, 1)), index)
    }

    val categorizedReferenceSections = data.categorizedReferences.map { it.text }.filterNotNull().mapIndexed { index, value ->
        MetadataSection(Section.CATEGORIZED_REFERENCE, listOf(Metadatum(value, MetadatumType.TEXT)), index)
    }

    val sections = listOf(createSectionForNorm(data)) +
        createSectionsWithoutGrouping(Section.SUBJECT_AREA, subjectFna + subjectGesta) +
        createSectionsFromMetadata(Section.LEAD, leadJurisdiction + leadUnit) +
        createSectionForDocumentType(data.documentType) +
        createSectionsFromMetadata(Section.PARTICIPATION, participationType + participationInstitution) +
        createSectionsForOfficialReference(data.digitalAnnouncementList, data.printAnnouncementList) +
        createSectionsForDocumentStatus(data.documentStatus, data.documentTextProof) +
        createSectionsForDivergentEntryIntoForce(data.divergentEntryIntoForceList) +
        createSectionsForDivergentExpiration(data.divergentExpirationsList) +
        citationDateSections + ageIndicationSections + categorizedReferenceSections +
        addProviderSections(data.normProviderList) +
        createSectionForEntryIntoForceAndExpiration(data) +
        createSectionForStatusIndication(data.statusList, data.reissueList, data.repealList, data.otherStatusList) +
        createFootnoteSections(data.footnotes) +
        listOfNotNull(createAnnoucementDateSection(data.announcementDate))

    return Norm(
        guid = guid,
        articles = mapArticlesToDomain(data.articles),
        metadataSections = sections.filterNotNull(),
    )
}

fun createSectionsForDocumentStatus(
    documentStatus: List<DocumentStatus>,
    documentTextProof: String?,
): List<MetadataSection> {
    val documentStatusSections = mutableListOf<MetadataSection>()
    var raiseOrder = 1

    if (documentTextProof != null) {
        val metadata = listOf(Metadatum(documentTextProof, MetadatumType.TEXT))
        val proofSection = MetadataSection(MetadataSectionName.DOCUMENT_TEXT_PROOF, metadata)
        val parentSection = MetadataSection(MetadataSectionName.DOCUMENT_STATUS_SECTION, emptyList(), sections = listOf(proofSection))
        documentStatusSections.add(parentSection)
        raiseOrder++
    }

    documentStatus.forEachIndexed { index, documentStatusGroup ->
        val metadata = mutableListOf<Metadatum<*>>()

        if (documentStatusGroup.documentStatusWorkNote.isNotEmpty()) {
            metadata.add(Metadatum(documentStatusGroup.documentStatusWorkNote[0], MetadatumType.WORK_NOTE))
        }

        documentStatusGroup.documentStatusDescription?.let { metadata.add(Metadatum(it, MetadatumType.DESCRIPTION)) }
        documentStatusGroup.documentStatusDateYear?.let { metadata.add(Metadatum(it, MetadatumType.YEAR)) }
        documentStatusGroup.documentStatusReference?.let { metadata.add(Metadatum(it, MetadatumType.REFERENCE)) }

        if (metadata.isNotEmpty()) {
            val childSection = MetadataSection(MetadataSectionName.DOCUMENT_STATUS, metadata)
            val parentSection = MetadataSection(MetadataSectionName.DOCUMENT_STATUS_SECTION, emptyList(), index + raiseOrder, sections = listOf(childSection))
            documentStatusSections.add(parentSection)
        }
    }

    return documentStatusSections
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

private fun createMetadatumForNullableString(string: String?, type: MetadatumType): List<Metadatum<String>> = string?.let { listOf(Metadatum(string, type)) } ?: listOf()

private fun createSectionForNorm(data: NormData): MetadataSection {
    val divergentDocumentNumber = createMetadatumForNullableString(data.divergentDocumentNumber, MetadatumType.DIVERGENT_DOCUMENT_NUMBER)
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
    val officialLongTitle = createMetadatumForNullableString(data.officialLongTitle, MetadatumType.OFFICIAL_LONG_TITLE)
    val risAbbreviation = createMetadatumForNullableString(data.risAbbreviation, MetadatumType.RIS_ABBREVIATION)
    val documentCategory = createMetadatumForNullableString(data.documentCategory, MetadatumType.DOCUMENT_CATEGORY)
    val officialShortTitle = createMetadatumForNullableString(data.officialShortTitle, MetadatumType.OFFICIAL_SHORT_TITLE)
    val officialAbbreviation = createMetadatumForNullableString(data.officialAbbreviation, MetadatumType.OFFICIAL_ABBREVIATION)
    val celexNumber = createMetadatumForNullableString(data.celexNumber, MetadatumType.CELEX_NUMBER)
    val text = createMetadatumForNullableString(data.text, MetadatumType.TEXT)

    return MetadataSection(Section.NORM, frameKeywords + divergentDocumentNumber + risAbbreviationInternationalLaw + unofficialAbbreviation + unofficialShortTitle + unofficialLongTitle + unofficialReference + referenceNumber + definition + ageOfMajorityIndication + validityRule + officialLongTitle + risAbbreviation + documentCategory + officialShortTitle + officialAbbreviation + celexNumber + text)
}

private fun createSectionsForDivergentEntryIntoForce(data: List<DivergentEntryIntoForce>): List<MetadataSection> {
    val definedDate = createMetadataForType(data.filter { it.state == null }.map { it.date }.filterNotNull().map { parseDateString(it) }, MetadatumType.DATE)
    val definedCategory = createMetadataForType(data.filter { it.state == null }.mapNotNull { parseNormCategory(it.normCategory) }, MetadatumType.NORM_CATEGORY)
    val undefinedDate = createMetadataForType(data.mapNotNull { parseDateStateString(it.state) }, MetadatumType.UNDEFINED_DATE)
    val undefinedCategory = createMetadataForType(data.filter { it.state != null }.mapNotNull { parseNormCategory(it.normCategory) }, MetadatumType.NORM_CATEGORY)

    val sections = createSectionsFromMetadata(Section.DIVERGENT_ENTRY_INTO_FORCE_DEFINED, definedDate + definedCategory) +
        createSectionsFromMetadata(Section.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED, undefinedDate + undefinedCategory)

    return sections.mapIndexed { index, section -> MetadataSection(Section.DIVERGENT_ENTRY_INTO_FORCE, listOf(), index, listOf(section)) }
}

private fun createSectionsForDivergentExpiration(data: List<DivergentExpiration>): List<MetadataSection> {
    val definedDate = createMetadataForType(data.filter { it.state == null }.map { it.date }.filterNotNull().map { parseDateString(it) }, MetadatumType.DATE)
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
        val metadata = Metadatum(LocalDate.parse(data.entryIntoForceDate), MetadatumType.DATE)
        sectionList.add(MetadataSection(Section.ENTRY_INTO_FORCE, listOf(metadata)))
    } else if (data.entryIntoForceDateState !== null) {
        val dateState = parseDateStateString(data.entryIntoForceDateState)
        dateState?.let {
            val metadata = Metadatum(it, MetadatumType.UNDEFINED_DATE)
            sectionList.add(MetadataSection(Section.ENTRY_INTO_FORCE, listOf(metadata)))
        }
    }

    if (data.principleEntryIntoForceDate !== null) {
        val metadata = Metadatum(LocalDate.parse(data.principleEntryIntoForceDate), MetadatumType.DATE)
        sectionList.add(MetadataSection(Section.PRINCIPLE_ENTRY_INTO_FORCE, listOf(metadata)))
    } else if (data.principleEntryIntoForceDateState !== null) {
        val dateState = parseDateStateString(data.principleEntryIntoForceDateState)
        dateState?.let {
            val metadata = Metadatum(it, MetadatumType.UNDEFINED_DATE)
            sectionList.add(MetadataSection(Section.PRINCIPLE_ENTRY_INTO_FORCE, listOf(metadata)))
        }
    }

    if (data.expirationDate !== null) {
        val metadata = Metadatum(LocalDate.parse(data.expirationDate), MetadatumType.DATE)
        sectionList.add(MetadataSection(Section.EXPIRATION, listOf(metadata)))
    } else if (data.expirationDateState !== null) {
        val dateState = parseDateStateString(data.expirationDateState)
        dateState?.let {
            val metadata = Metadatum(it, MetadatumType.UNDEFINED_DATE)
            sectionList.add(MetadataSection(Section.EXPIRATION, listOf(metadata)))
        }
    }

    if (data.principleExpirationDate !== null) {
        val metadata = Metadatum(LocalDate.parse(data.principleExpirationDate), MetadatumType.DATE)
        sectionList.add(MetadataSection(Section.PRINCIPLE_EXPIRATION, listOf(metadata)))
    } else if (data.principleExpirationDateState !== null) {
        val dateState = parseDateStateString(data.principleExpirationDateState)
        dateState?.let {
            val metadata = Metadatum(it, MetadatumType.UNDEFINED_DATE)
            sectionList.add(MetadataSection(Section.PRINCIPLE_EXPIRATION, listOf(metadata)))
        }
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

private fun createFootnoteSections(footnotes: List<Footnote>): List<MetadataSection> {
    val footnoteSections = mutableListOf<MetadataSection>()

    footnotes.forEachIndexed { index, footnoteGroup ->

        fun addFootnotesToMetadata(footnoteList: List<Pair<Int, String>>, type: MetadatumType, metadata: MutableList<Metadatum<*>>, addToFootnoteOrder: Int) {
            footnoteList.forEach { singleFootnote ->
                metadata.add(Metadatum(singleFootnote.second, type, singleFootnote.first + addToFootnoteOrder))
            }
        }

        val metadata = mutableListOf<Metadatum<*>>()

        footnoteGroup.reference?.let { metadata.add(Metadatum(it, MetadatumType.FOOTNOTE_REFERENCE, 1)) }

        val addToFootnotesOrder = if (footnoteGroup.reference != null) 1 else 0
        addFootnotesToMetadata(footnoteGroup.footnoteChange, MetadatumType.FOOTNOTE_CHANGE, metadata, addToFootnotesOrder)
        addFootnotesToMetadata(footnoteGroup.footnoteComment, MetadatumType.FOOTNOTE_COMMENT, metadata, addToFootnotesOrder)
        addFootnotesToMetadata(footnoteGroup.footnoteDecision, MetadatumType.FOOTNOTE_DECISION, metadata, addToFootnotesOrder)
        addFootnotesToMetadata(footnoteGroup.footnoteStateLaw, MetadatumType.FOOTNOTE_STATE_LAW, metadata, addToFootnotesOrder)
        addFootnotesToMetadata(footnoteGroup.footnoteEuLaw, MetadatumType.FOOTNOTE_EU_LAW, metadata, addToFootnotesOrder)
        addFootnotesToMetadata(footnoteGroup.otherFootnote, MetadatumType.FOOTNOTE_OTHER, metadata, addToFootnotesOrder)

        if (metadata.isNotEmpty()) footnoteSections.add(MetadataSection(Section.FOOTNOTES, metadata, index + 1))
    }

    return footnoteSections
}

private fun createSectionForStatusIndication(statusList: List<Status>, reissueList: List<Reissue>, repealList: List<String>, otherStatusList: List<String>): List<MetadataSection> {
    val statusSections = mutableListOf<MetadataSection>()

    statusList.forEachIndexed { index, status ->
        val metadata = mutableListOf<Metadatum<*>>()
        status.statusNote?.let { metadata.add(Metadatum(it, MetadatumType.NOTE)) }
        status.statusDescription?.let { metadata.add(Metadatum(it, MetadatumType.DESCRIPTION)) }
        status.statusDate?.let { metadata.add(Metadatum(LocalDate.parse(it), MetadatumType.DATE)) }
        status.statusReference?.let { metadata.add(Metadatum(it, MetadatumType.REFERENCE)) }

        if (metadata.isNotEmpty()) {
            statusSections.add(MetadataSection(Section.STATUS, metadata, index + 1))
        }
    }

    reissueList.forEachIndexed { index, reissue ->
        val metadata = mutableListOf<Metadatum<*>>()
        reissue.reissueNote?.let { metadata.add(Metadatum(it, MetadatumType.NOTE)) }
        reissue.reissueArticle?.let { metadata.add(Metadatum(it, MetadatumType.ARTICLE)) }
        reissue.reissueDate?.let { metadata.add(Metadatum(LocalDate.parse(it), MetadatumType.DATE)) }
        reissue.reissueReference?.let { metadata.add(Metadatum(it, MetadatumType.REFERENCE)) }

        if (metadata.isNotEmpty()) {
            statusSections.add(MetadataSection(Section.REISSUE, metadata, index + 1))
        }
    }

    statusSections.addAll(
        repealList.mapIndexed { index, repeal ->
            MetadataSection(Section.REPEAL, listOf(Metadatum(repeal, MetadatumType.TEXT)), index + 1)
        },
    )

    statusSections.addAll(
        otherStatusList.mapIndexed { index, otherStatus ->
            MetadataSection(Section.OTHER_STATUS, listOf(Metadatum(otherStatus, MetadatumType.NOTE)), index + 1)
        },
    )

    return statusSections.mapIndexed { index, section ->
        MetadataSection(Section.STATUS_INDICATION, emptyList(), index + 1, listOf(section))
    }
}

fun addProviderSections(normProviders: List<NormProvider>): List<MetadataSection> {
    return normProviders.mapIndexed { index, normProvider ->
        val metadata = mutableListOf<Metadatum<*>>()

        normProvider.entity?.let { metadata.add(Metadatum(it, MetadatumType.ENTITY, 1)) }

        normProvider.decidingBody?.let { metadata.add(Metadatum(it, MetadatumType.DECIDING_BODY, 1)) }
        normProvider.isResolutionMajority?.let { metadata.add(Metadatum(it, MetadatumType.RESOLUTION_MAJORITY, 1)) }

        if (metadata.isNotEmpty()) MetadataSection(Section.NORM_PROVIDER, metadata, index + 1) else null
    }.filterNotNull()
}

private fun createAnnoucementDateSection(announcementDate: String?): MetadataSection? {
    return announcementDate?.let {
        val date = parseDateString(it)
        date?.let {
            val metadatum = Metadatum(it, MetadatumType.DATE)
            MetadataSection(Section.ANNOUNCEMENT_DATE, listOf(metadatum))
        }
    }
}

private fun createMetadataForType(data: List<*>, type: MetadatumType): List<Metadatum<*>> = data
    .filterNotNull().mapIndexed { index, value -> Metadatum(value, type, index + 1) }

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

fun parseDateString(value: String): LocalDate? = try { LocalDate.parse(value) } catch (e: DateTimeParseException) { null }

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
