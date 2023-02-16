package de.bund.digitalservice.ris.norms.framework.adapter.output

import de.bund.digitalservice.ris.norms.application.port.output.ParseJurisXmlOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.value.UndefinedDate
import de.bund.digitalservice.ris.norms.juris.extractor.extractData
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.util.UUID
import de.bund.digitalservice.ris.norms.juris.extractor.model.Norm as NormData

class JurisXmlParser() : ParseJurisXmlOutputPort {
    override fun parseJurisXml(command: ParseJurisXmlOutputPort.Command): Mono<Norm> {
        val data = extractData(command.zipFile)
        val norm = mapDataToDomain(command.newGuid, data)
        return Mono.just(norm)
    }
}

fun mapDataToDomain(guid: UUID, data: NormData): Norm {
    return Norm(
        guid = guid,
        articles = emptyList(),
        officialLongTitle = data.officialLongTitle ?: "",
        risAbbreviation = data.risAbbreviation,
        risAbbreviationInternationalLaw = data.risAbbreviationInternationalLaw,
        documentNumber = data.documentNumber,
        documentCategory = data.documentCategory,
        frameKeywords = data.frameKeywords,
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

fun parseDateString(value: String?): LocalDate? = value?.let { LocalDate.parse(value) }

fun parseDateStateString(value: String?): UndefinedDate? =
    if (value.isNullOrEmpty())null else UndefinedDate.valueOf(value)
