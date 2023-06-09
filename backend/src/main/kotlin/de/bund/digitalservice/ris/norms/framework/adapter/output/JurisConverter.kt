package de.bund.digitalservice.ris.norms.framework.adapter.output

import de.bund.digitalservice.ris.norms.application.port.output.GenerateNormFileOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.ParseJurisXmlOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.FileReference
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.domain.entity.getHashFromContent
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.AGE_OF_MAJORITY_INDICATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.ANNOUNCEMENT_GAZETTE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.ANNOUNCEMENT_MEDIUM
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DATE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DECIDING_BODY
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DEFINITION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DIVERGENT_DOCUMENT_NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.EDITION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.ENTITY
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.KEYWORD
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.LEAD_JURISDICTION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.LEAD_UNIT
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.NORM_CATEGORY
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PAGE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PARTICIPATION_INSTITUTION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PARTICIPATION_TYPE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RANGE_START
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.REFERENCE_NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RESOLUTION_MAJORITY
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RIS_ABBREVIATION_INTERNATIONAL_LAW
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.SUBJECT_FNA
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.SUBJECT_GESTA
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.TEMPLATE_NAME
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.TEXT
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.TYPE_NAME
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNDEFINED_DATE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_ABBREVIATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_LONG_TITLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_REFERENCE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_SHORT_TITLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.VALIDITY_RULE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.YEAR
import de.bund.digitalservice.ris.norms.domain.value.NormCategory
import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.decodeLocalDate
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeLocalDate
import de.bund.digitalservice.ris.norms.juris.converter.extractor.extractData
import de.bund.digitalservice.ris.norms.juris.converter.generator.generateZip
import de.bund.digitalservice.ris.norms.juris.converter.model.CategorizedReference
import de.bund.digitalservice.ris.norms.juris.converter.model.DigitalAnnouncement
import de.bund.digitalservice.ris.norms.juris.converter.model.DivergentEntryIntoForce
import de.bund.digitalservice.ris.norms.juris.converter.model.DivergentExpiration
import de.bund.digitalservice.ris.norms.juris.converter.model.DocumentType
import de.bund.digitalservice.ris.norms.juris.converter.model.NormProvider
import de.bund.digitalservice.ris.norms.juris.converter.model.PrintAnnouncement
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.nio.ByteBuffer
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.UUID
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName as Section
import de.bund.digitalservice.ris.norms.juris.converter.model.Article as ArticleData
import de.bund.digitalservice.ris.norms.juris.converter.model.Norm as NormData
import de.bund.digitalservice.ris.norms.juris.converter.model.Paragraph as ParagraphData

@Component
class JurisConverter() : ParseJurisXmlOutputPort, GenerateNormFileOutputPort {
    override fun parseJurisXml(query: ParseJurisXmlOutputPort.Query): Mono<Norm> {
        val data = extractData(ByteBuffer.wrap(query.zipFile))
        val norm = mapDataToDomain(query.newGuid, data)
        norm.files = listOf(FileReference(query.filename, getHashFromContent(query.zipFile)))
        return Mono.just(norm)
    }

    override fun generateNormFile(command: GenerateNormFileOutputPort.Command): Mono<ByteArray> {
        return Mono.just(generateZip(mapDomainToData(command.norm), ByteBuffer.wrap(command.previousFile)))
    }
}

fun mapDomainToData(norm: Norm): NormData {
    val keywords = extractStringValues(norm, MetadataSectionName.NORM, KEYWORD)
    val divergentNumber = extractFirstStringValue(norm, MetadataSectionName.NORM, DIVERGENT_DOCUMENT_NUMBER)
    val citationDates = extractLocalDateValues(norm, MetadataSectionName.CITATION_DATE, DATE)
    val citationYears = extractStringValues(norm, MetadataSectionName.CITATION_DATE, YEAR)

    val normProviders: List<NormProvider> = norm.metadataSections.filter { section -> section.name == MetadataSectionName.NORM_PROVIDER }.map {
        val entity = it.metadata.find { metadatum -> metadatum.type == ENTITY }?.let { found -> found.value as String }
        val decidingBody = it.metadata.find { metadatum -> metadatum.type == DECIDING_BODY }?.let { found -> found.value as String }
        val isResolutionMajority = it.metadata.find { metadatum -> metadatum.type == RESOLUTION_MAJORITY }?.let { found -> found.value as Boolean }
        NormProvider(entity, decidingBody, isResolutionMajority)
    }

    return NormData(
        announcementDate = encodeLocalDate(norm.announcementDate),
        citationDateList = citationDates.filterNotNull() + citationYears,
        documentCategory = norm.documentCategory,
        divergentDocumentNumber = divergentNumber,
        frameKeywordList = keywords,
        officialAbbreviation = norm.officialAbbreviation,
        officialLongTitle = norm.officialLongTitle,
        officialShortTitle = norm.officialShortTitle,
        normProviderList = normProviders,
        printAnnouncementList = extractPrintAnnouncementList(norm),
        digitalAnnouncementList = extractDigitalAnnouncementList(norm),
        risAbbreviation = norm.risAbbreviation,
        categorizedReferences = extractCategorizedReferences(norm),
    )
}

fun mapDataToDomain(guid: UUID, data: NormData): Norm {
    val participationType = createMetadataForType(data.participationList.mapNotNull { it.type }, PARTICIPATION_TYPE)
    val participationInstitution = createMetadataForType(data.participationList.mapNotNull { it.institution }, PARTICIPATION_INSTITUTION)

    val leadJurisdiction = createMetadataForType(data.leadList.mapNotNull { it.jurisdiction }, LEAD_JURISDICTION)
    val leadUnit = createMetadataForType(data.leadList.mapNotNull { it.unit }, LEAD_UNIT)

    val subjectFna = createMetadataForType(data.subjectAreaList.mapNotNull { it.fna }, SUBJECT_FNA)
    val subjectGesta = createMetadataForType(data.subjectAreaList.mapNotNull { it.gesta }, SUBJECT_GESTA)

    val citationDateSections = data.citationDateList.mapIndexed { index, value ->
        if (value.length == 4 && value.toIntOrNull() != null) {
            MetadataSection(MetadataSectionName.CITATION_DATE, listOf(Metadatum(value, YEAR, 1)), index)
        } else if (value.length > 4 && parseDateString(value) != null) {
            MetadataSection(MetadataSectionName.CITATION_DATE, listOf(Metadatum(parseDateString(value), DATE, 1)), index)
        } else {
            null
        }
    }

    val ageIndicationSections = data.ageIndicationStartList.mapIndexed { index, value ->
        MetadataSection(MetadataSectionName.AGE_INDICATION, listOf(Metadatum(value, RANGE_START, 1)), index)
    }

    val categorizedReferenceSections = data.categorizedReferences.mapIndexed { index, value ->
        MetadataSection(MetadataSectionName.CATEGORIZED_REFERENCE, listOf(Metadatum(value.text, TEXT)), index)
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

fun createSectionForEntryIntoForceAndExpiration(data: NormData): List<MetadataSection> {
    val sectionList = mutableListOf<MetadataSection>()

    if (data.entryIntoForceDate !== null) {
        val metadata = Metadatum(decodeLocalDate(data.entryIntoForceDate), DATE)
        sectionList.add(MetadataSection(MetadataSectionName.ENTRY_INTO_FORCE, listOf(metadata)))
    } else if (data.entryIntoForceDateState !== null) {
        val metadata = Metadatum(parseDateStateString(data.entryIntoForceDateState), UNDEFINED_DATE)
        sectionList.add(MetadataSection(MetadataSectionName.ENTRY_INTO_FORCE, listOf(metadata)))
    }

    if (data.principleEntryIntoForceDate !== null) {
        val metadata = Metadatum(decodeLocalDate(data.principleEntryIntoForceDate), DATE)
        sectionList.add(MetadataSection(MetadataSectionName.PRINCIPLE_ENTRY_INTO_FORCE, listOf(metadata)))
    } else if (data.principleEntryIntoForceDateState !== null) {
        val metadata = Metadatum(parseDateStateString(data.principleEntryIntoForceDateState), UNDEFINED_DATE)
        sectionList.add(MetadataSection(MetadataSectionName.PRINCIPLE_ENTRY_INTO_FORCE, listOf(metadata)))
    }

    if (data.expirationDate !== null) {
        val metadata = Metadatum(decodeLocalDate(data.expirationDate), DATE)
        sectionList.add(MetadataSection(MetadataSectionName.EXPIRATION, listOf(metadata)))
    } else if (data.expirationDateState !== null) {
        val metadata = Metadatum(parseDateStateString(data.expirationDateState), UNDEFINED_DATE)
        sectionList.add(MetadataSection(MetadataSectionName.EXPIRATION, listOf(metadata)))
    }

    if (data.principleExpirationDate !== null) {
        val metadata = Metadatum(decodeLocalDate(data.principleExpirationDate), DATE)
        sectionList.add(MetadataSection(MetadataSectionName.PRINCIPLE_EXPIRATION, listOf(metadata)))
    } else if (data.principleEntryIntoForceDateState !== null) {
        val metadata = Metadatum(parseDateStateString(data.principleEntryIntoForceDateState), UNDEFINED_DATE)
        sectionList.add(MetadataSection(MetadataSectionName.PRINCIPLE_EXPIRATION, listOf(metadata)))
    }

    return sectionList
}

private fun createSectionForDocumentType(documentType: DocumentType?): MetadataSection? {
    return if (documentType !== null) {
        val documentNormCategories = createMetadataForType(documentType.categories.mapNotNull { parseNormCategory(it) }, NORM_CATEGORY)
        val documentTemplateNames = createMetadataForType(documentType.templateNames.map { it }, TEMPLATE_NAME)
        val metadata = (documentNormCategories + documentTemplateNames).toMutableList()
        if (documentType.name != null) {
            metadata += createMetadataForType(listOf(documentType.name), TYPE_NAME)
        }
        MetadataSection(Section.DOCUMENT_TYPE, metadata)
    } else {
        null
    }
}

private fun createSectionForNorm(data: NormData): MetadataSection {
    val divergentDocumentNumber = data.divergentDocumentNumber?.let { listOf(Metadatum(data.divergentDocumentNumber, DIVERGENT_DOCUMENT_NUMBER, 1)) } ?: listOf()
    val frameKeywords = createMetadataForType(data.frameKeywordList, KEYWORD)
    val risAbbreviationInternationalLaw = createMetadataForType(data.risAbbreviationInternationalLawList, RIS_ABBREVIATION_INTERNATIONAL_LAW)
    val unofficialLongTitle = createMetadataForType(data.unofficialLongTitleList, UNOFFICIAL_LONG_TITLE)
    val unofficialShortTitle = createMetadataForType(data.unofficialShortTitleList, UNOFFICIAL_SHORT_TITLE)
    val unofficialAbbreviation = createMetadataForType(data.unofficialAbbreviationList, UNOFFICIAL_ABBREVIATION)
    val unofficialReference = createMetadataForType(data.unofficialReferenceList, UNOFFICIAL_REFERENCE)
    val referenceNumber = createMetadataForType(data.referenceNumberList, REFERENCE_NUMBER)
    val definition = createMetadataForType(data.definitionList, DEFINITION)
    val ageOfMajorityIndication = createMetadataForType(data.ageOfMajorityIndicationList, AGE_OF_MAJORITY_INDICATION)
    val validityRule = createMetadataForType(data.validityRuleList, VALIDITY_RULE)

    return MetadataSection(Section.NORM, frameKeywords + divergentDocumentNumber + risAbbreviationInternationalLaw + unofficialAbbreviation + unofficialShortTitle + unofficialLongTitle + unofficialReference + referenceNumber + definition + ageOfMajorityIndication + validityRule)
}

private fun createSectionsForDivergentEntryIntoForce(data: List<DivergentEntryIntoForce>): List<MetadataSection> {
    val definedDate = createMetadataForType(data.filter { it.state == null }.mapNotNull { parseDateString(it.date) }, DATE)
    val definedCategory = createMetadataForType(data.filter { it.state == null }.mapNotNull { parseNormCategory(it.normCategory) }, NORM_CATEGORY)
    val undefinedDate = createMetadataForType(data.mapNotNull { parseDateStateString(it.state) }, UNDEFINED_DATE)
    val undefinedCategory = createMetadataForType(data.filter { it.state != null }.mapNotNull { parseNormCategory(it.normCategory) }, NORM_CATEGORY)

    val sections = createSectionsFromMetadata(MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_DEFINED, definedDate + definedCategory) +
        createSectionsFromMetadata(MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE_UNDEFINED, undefinedDate + undefinedCategory)

    return sections.mapIndexed { index, section -> MetadataSection(MetadataSectionName.DIVERGENT_ENTRY_INTO_FORCE, listOf(), index, listOf(section)) }
}

private fun createSectionsForDivergentExpiration(data: List<DivergentExpiration>): List<MetadataSection> {
    val definedDate = createMetadataForType(data.filter { it.state == null }.mapNotNull { parseDateString(it.date) }, DATE)
    val definedCategory = createMetadataForType(data.filter { it.state == null }.mapNotNull { parseNormCategory(it.normCategory) }, NORM_CATEGORY)
    val undefinedDate = createMetadataForType(data.mapNotNull { parseDateStateString(it.state) }, UNDEFINED_DATE)
    val undefinedCategory = createMetadataForType(data.filter { it.state != null }.mapNotNull { parseNormCategory(it.normCategory) }, NORM_CATEGORY)

    val sections = createSectionsFromMetadata(MetadataSectionName.DIVERGENT_EXPIRATION_DEFINED, definedDate + definedCategory) +
        createSectionsFromMetadata(MetadataSectionName.DIVERGENT_EXPIRATION_UNDEFINED, undefinedDate + undefinedCategory)

    return sections.mapIndexed { index, section -> MetadataSection(MetadataSectionName.DIVERGENT_EXPIRATION, listOf(), index, listOf(section)) }
}

private fun createSectionsForOfficialReference(digitalAnnouncement: List<DigitalAnnouncement>, printAnnouncement: List<PrintAnnouncement>): List<MetadataSection> {
    val printAnnouncementGazette = createMetadataForType(printAnnouncement.mapNotNull { it.gazette }, ANNOUNCEMENT_GAZETTE)
    val printAnnouncementPage = createMetadataForType(printAnnouncement.mapNotNull { it.page }, PAGE)
    val printAnnouncementYear = createMetadataForType(printAnnouncement.mapNotNull { it.year }, YEAR)
    val digitalAnnouncementYear = createMetadataForType(digitalAnnouncement.mapNotNull { it.year }, YEAR)
    val digitalAnnouncementNumber = createMetadataForType(digitalAnnouncement.mapNotNull { it.number }, EDITION)
    val digitalAnnouncementMedium = createMetadataForType(digitalAnnouncement.mapNotNull { it.medium }, ANNOUNCEMENT_MEDIUM)

    val referenceSections = createSectionsFromMetadata(Section.PRINT_ANNOUNCEMENT, printAnnouncementGazette + printAnnouncementYear + printAnnouncementPage) +
        createSectionsFromMetadata(Section.DIGITAL_ANNOUNCEMENT, digitalAnnouncementNumber + digitalAnnouncementMedium + digitalAnnouncementYear)

    return referenceSections.mapIndexed { index, section -> MetadataSection(MetadataSectionName.OFFICIAL_REFERENCE, listOf(), index, listOf(section)) }
}

fun addProviderSections(normProviders: List<NormProvider>): List<MetadataSection> {
    return normProviders.mapIndexed { index, normProvider ->
        val metadata = mutableListOf<Metadatum<*>>()
        if (normProvider.entity !== null) {
            metadata.add(Metadatum(normProvider.entity, ENTITY, 1))
        }
        if (normProvider.decidingBody !== null) {
            metadata.add(Metadatum(normProvider.decidingBody, DECIDING_BODY, 1))
        }
        if (normProvider.isResolutionMajority !== null) {
            metadata.add(Metadatum(normProvider.isResolutionMajority, RESOLUTION_MAJORITY, 1))
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

private fun extractStringValues(norm: Norm, sectionName: MetadataSectionName, metadatumType: MetadatumType): List<String> {
    return norm.metadataSections
        .filter { it.name == sectionName }
        .flatMap { it.metadata }
        .filter { it.type == metadatumType }
        .sortedBy { it.order }
        .map { it.value.toString() }
}

private fun extractLocalDateValues(norm: Norm, sectionName: MetadataSectionName, metadatumType: MetadatumType): List<String?> {
    return norm.metadataSections
        .filter { it.name == sectionName }
        .flatMap { it.metadata }
        .filter { it.type == metadatumType }
        .sortedBy { it.order }
        .map { encodeLocalDate(it.value as LocalDate) }
}

private fun extractFirstStringValue(norm: Norm, sectionName: MetadataSectionName, metadatumType: MetadatumType): String {
    return norm.metadataSections
        .filter { it.name == sectionName }
        .flatMap { it.metadata }
        .filter { it.type == metadatumType }
        .minByOrNull { it.order }?.value.toString()
}

private fun extractCategorizedReferences(norm: Norm): List<CategorizedReference> = norm
    .metadataSections
    .filter { it.name == MetadataSectionName.CATEGORIZED_REFERENCE }
    .map { section ->
        CategorizedReference(
            section.metadata.find { it.type == TEXT }?.value.toString(),
        )
    }

private fun extractPrintAnnouncementList(norm: Norm): List<PrintAnnouncement> = norm
    .metadataSections
    .filter { it.name == MetadataSectionName.PRINT_ANNOUNCEMENT }
    .map { section ->
        PrintAnnouncement(
            section.metadata.find { it.type == YEAR }?.value.toString(),
            section.metadata.find { it.type == PAGE }?.value.toString(),
            section.metadata.find { it.type == ANNOUNCEMENT_GAZETTE }?.value.toString(),
        )
    }

private fun extractDigitalAnnouncementList(norm: Norm): List<DigitalAnnouncement> = norm
    .metadataSections
    .filter { it.name == MetadataSectionName.DIGITAL_ANNOUNCEMENT }
    .map { section ->
        DigitalAnnouncement(
            section.metadata.find { it.type == YEAR }?.value.toString(),
            section.metadata.find { it.type == NUMBER }?.value.toString(),
            section.metadata.find { it.type == ANNOUNCEMENT_MEDIUM }?.value.toString(),
        )
    }
