package de.bund.digitalservice.ris.norms.framework.adapter.output

import de.bund.digitalservice.ris.norms.application.port.output.GenerateNormFileOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.ParseJurisXmlOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.FileReference
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.entity.MetadatumType.KEYWORD
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.domain.entity.getHashFromContent
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
    @Suppress("UNCHECKED_CAST")
    val keywords = norm.metadata.toMutableSet()
        .filter { it.type == KEYWORD }
        .sortedBy { it.order }
        .map { it.value } as MutableList<String>

    val normData = NormData()
    normData.announcementDate = encodeLocalDate(norm.announcementDate)
    normData.citationDate = encodeLocalDate(norm.citationDate) ?: norm.citationYear
    normData.documentCategory = norm.documentCategory
    normData.documentNumber = norm.documentNumber
    normData.entryIntoForceDate = encodeLocalDate(norm.entryIntoForceDate)
    normData.expirationDate = encodeLocalDate(norm.expirationDate)
    normData.frameKeywords = keywords
    normData.officialAbbreviation = norm.officialAbbreviation
    normData.officialLongTitle = norm.officialLongTitle
    normData.officialShortTitle = norm.officialShortTitle
    normData.providerEntity = norm.providerEntity
    normData.providerDecidingBody = norm.providerDecidingBody
    normData.printAnnouncementGazette = norm.printAnnouncementGazette
    normData.printAnnouncementYear = norm.printAnnouncementYear
    normData.printAnnouncementNumber = norm.printAnnouncementNumber
    normData.risAbbreviation = norm.risAbbreviation
    return normData
}

fun mapDataToDomain(guid: UUID, data: NormData): Norm {
    val metadata = data.frameKeywords.mapIndexed { index, value -> Metadatum(value, KEYWORD, index) }

    return Norm(
        guid = guid,
        articles = mapArticlesToDomain(data.articles),
        metadata = metadata,
        officialLongTitle = data.officialLongTitle ?: "",
        risAbbreviation = data.risAbbreviation,
        risAbbreviationInternationalLaw = data.risAbbreviationInternationalLaw,
        documentNumber = data.documentNumber,
        documentCategory = data.documentCategory,
        providerEntity = data.providerEntity,
        providerDecidingBody = data.providerDecidingBody,
        providerIsResolutionMajority = data.providerIsResolutionMajority,
        participationType = data.participationType,
        participationInstitution = data.participationInstitution,
        leadJurisdiction = data.leadJurisdiction,
        leadUnit = data.leadUnit,
        subjectFna = data.subjectFna,
        subjectGesta = data.subjectGesta,
        officialShortTitle = data.officialShortTitle,
        officialAbbreviation = data.officialAbbreviation,
        unofficialLongTitle = data.unofficialLongTitle,
        unofficialShortTitle = data.unofficialShortTitle,
        unofficialAbbreviation = data.unofficialAbbreviation,
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
        unofficialReference = data.unofficialReference,
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
        validityRule = data.validityRule,
        referenceNumber = data.referenceNumber,
        celexNumber = data.celexNumber,
        definition = data.definition,
        ageOfMajorityIndication = data.ageOfMajorityIndication,
        text = data.text,
    )
}

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
