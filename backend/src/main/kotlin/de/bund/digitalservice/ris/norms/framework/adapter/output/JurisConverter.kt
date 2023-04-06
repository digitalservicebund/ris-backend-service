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
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DEFINITION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.DIVERGENT_DOCUMENT_NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.KEYWORD
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.LEAD_JURISDICTION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.LEAD_UNIT
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PARTICIPATION_INSTITUTION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.PARTICIPATION_TYPE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.REFERENCE_NUMBER
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.RIS_ABBREVIATION_INTERNATIONAL_LAW
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.SUBJECT_FNA
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.SUBJECT_GESTA
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_ABBREVIATION
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_LONG_TITLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_REFERENCE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.UNOFFICIAL_SHORT_TITLE
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType.VALIDITY_RULE
import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeLocalDate
import de.bund.digitalservice.ris.norms.juris.converter.extractor.extractData
import de.bund.digitalservice.ris.norms.juris.converter.generator.generateZip
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
    val keywords = norm.metadataSections
        .flatMap { it.metadata }
        .filter { it.type == KEYWORD }
        .sortedBy { it.order }
        .map { it.value.toString() }
    val divergentNumber = norm.metadataSections
        .flatMap { it.metadata }
        .filter { it.type == DIVERGENT_DOCUMENT_NUMBER }
        .minByOrNull { it.order }?.value.toString()

    return NormData(
        announcementDate = encodeLocalDate(norm.announcementDate),
        citationDate = encodeLocalDate(norm.citationDate) ?: norm.citationYear,
        documentCategory = norm.documentCategory,
        divergentDocumentNumber = divergentNumber,
        entryIntoForceDate = encodeLocalDate(norm.entryIntoForceDate),
        expirationDate = encodeLocalDate(norm.expirationDate),
        frameKeywords = keywords,
        officialAbbreviation = norm.officialAbbreviation,
        officialLongTitle = norm.officialLongTitle,
        officialShortTitle = norm.officialShortTitle,
        providerEntity = norm.providerEntity,
        providerDecidingBody = norm.providerDecidingBody,
        printAnnouncementGazette = norm.printAnnouncementGazette,
        printAnnouncementYear = norm.printAnnouncementYear,
        printAnnouncementNumber = norm.printAnnouncementNumber,
        risAbbreviation = norm.risAbbreviation,
    )
}

fun mapDataToDomain(guid: UUID, data: NormData): Norm {
    val divergentDocumentNumber = data.divergentDocumentNumber?.let { listOf(Metadatum(data.divergentDocumentNumber, DIVERGENT_DOCUMENT_NUMBER, 1)) } ?: listOf()
    val frameKeywords = createMetadataForType(data.frameKeywords, KEYWORD)
    val risAbbreviationInternationalLaw = createMetadataForType(data.risAbbreviationInternationalLaw, RIS_ABBREVIATION_INTERNATIONAL_LAW)
    val unofficialLongTitle = createMetadataForType(data.unofficialLongTitle, UNOFFICIAL_LONG_TITLE)
    val unofficialShortTitle = createMetadataForType(data.unofficialShortTitle, UNOFFICIAL_SHORT_TITLE)
    val unofficialAbbreviation = createMetadataForType(data.unofficialAbbreviation, UNOFFICIAL_ABBREVIATION)
    val unofficialReference = createMetadataForType(data.unofficialReference, UNOFFICIAL_REFERENCE)
    val referenceNumber = createMetadataForType(data.referenceNumber, REFERENCE_NUMBER)
    val definition = createMetadataForType(data.definition, DEFINITION)
    val ageOfMajorityIndication = createMetadataForType(data.ageOfMajorityIndication, AGE_OF_MAJORITY_INDICATION)
    val validityRule = createMetadataForType(data.validityRule, VALIDITY_RULE)
    val participationType = createMetadataForType(data.participationType, PARTICIPATION_TYPE)
    val participationInstitution = createMetadataForType(data.participationInstitution, PARTICIPATION_INSTITUTION)
    val leadJurisdiction = createMetadataForType(data.leadJurisdiction, LEAD_JURISDICTION)
    val leadUnit = createMetadataForType(data.leadUnit, LEAD_UNIT)
    val subjectFna = createMetadataForType(data.subjectFna, SUBJECT_FNA)
    val subjectGesta = createMetadataForType(data.subjectGesta, SUBJECT_GESTA)

    val sections = listOf(
        MetadataSection(Section.NORM, frameKeywords + divergentDocumentNumber + risAbbreviationInternationalLaw + unofficialAbbreviation + unofficialShortTitle + unofficialLongTitle + unofficialReference + referenceNumber + definition + ageOfMajorityIndication + validityRule),
    ) + createSectionsFromMetadata(Section.SUBJECT_AREA, subjectFna + subjectGesta) +
        createSectionsFromMetadata(Section.LEAD, leadJurisdiction + leadUnit) +
        createSectionsFromMetadata(Section.PARTICIPATION, participationInstitution + participationType)

    return Norm(
        guid = guid,
        articles = mapArticlesToDomain(data.articles),
        metadataSections = sections,
        officialLongTitle = data.officialLongTitle ?: "",
        risAbbreviation = data.risAbbreviation,
        documentCategory = data.documentCategory,
        providerEntity = data.providerEntity,
        providerDecidingBody = data.providerDecidingBody,
        providerIsResolutionMajority = data.providerIsResolutionMajority,
        officialShortTitle = data.officialShortTitle,
        officialAbbreviation = data.officialAbbreviation,
        entryIntoForceDate = parseDateString(data.entryIntoForceDate),
        entryIntoForceDateState = parseDateStateString(data.entryIntoForceDateState ?: ""),
        principleEntryIntoForceDate = parseDateString(data.principleEntryIntoForceDate),
        principleEntryIntoForceDateState =
        parseDateStateString(data.principleEntryIntoForceDateState),
        divergentEntryIntoForceDate = parseDateString(data.divergentEntryIntoForceDate),
        divergentEntryIntoForceDateState =
        parseDateStateString(data.divergentEntryIntoForceDateState),
        entryIntoForceNormCategory = data.entryIntoForceNormCategory,
        expirationDate = parseDateString(data.expirationDate),
        expirationDateState = parseDateStateString(data.expirationDateState),
        principleExpirationDate = parseDateString(data.principleExpirationDate),
        principleExpirationDateState = parseDateStateString(data.principleExpirationDateState),
        divergentExpirationDate = parseDateString(data.divergentExpirationDate),
        divergentExpirationDateState = parseDateStateString(data.divergentExpirationDateState),
        expirationNormCategory = data.expirationNormCategory,
        announcementDate = parseDateString(data.announcementDate),
        citationDate = parseDateString(data.citationDate),
        citationYear = if (data.citationDate?.length == 4 && data.citationDate?.toIntOrNull() != null) data.citationDate else null,
        printAnnouncementGazette = data.printAnnouncementGazette,
        printAnnouncementYear = data.printAnnouncementYear,
        printAnnouncementPage = data.printAnnouncementPage,
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
        categorizedReference = data.categorizedReference,
        otherFootnote = data.otherFootnote,
        celexNumber = data.celexNumber,
        text = data.text,
    )
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
