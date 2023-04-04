package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import de.bund.digitalservice.ris.norms.application.port.output.EditNormOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.domain.value.MetadatumType
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class EditNormFrameService(private val editNormOutputPort: EditNormOutputPort) :
    EditNormFrameUseCase {

    override fun editNormFrame(command: EditNormFrameUseCase.Command): Mono<Boolean> {
        val norm = getUpdatedNorm(command.guid, command.properties)
        val editNormCommand = EditNormOutputPort.Command(norm)
        return editNormOutputPort.editNorm(editNormCommand)
    }
}

private fun getUpdatedNorm(guid: UUID, properties: EditNormFrameUseCase.NormFrameProperties) = Norm(
    guid = guid,
    articles = listOf(),
    metadataSections = mapMetadataToMetadataSections(properties.metadata),
    officialLongTitle = properties.officialLongTitle,
    risAbbreviation = properties.risAbbreviation,
    documentNumber = properties.documentNumber,
    documentCategory = properties.documentCategory,
    documentTypeName = properties.documentTypeName,
    documentNormCategory = properties.documentNormCategory,
    documentTemplateName = properties.documentTemplateName,
    providerEntity = properties.providerEntity,
    providerDecidingBody = properties.providerDecidingBody,
    providerIsResolutionMajority = properties.providerIsResolutionMajority,
    participationType = properties.participationType,
    participationInstitution = properties.participationInstitution,
    leadJurisdiction = properties.leadJurisdiction,
    leadUnit = properties.leadUnit,
    subjectFna = properties.subjectFna,
    subjectPreviousFna = properties.subjectPreviousFna,
    subjectGesta = properties.subjectGesta,
    subjectBgb3 = properties.subjectBgb3,
    officialShortTitle = properties.officialShortTitle,
    officialAbbreviation = properties.officialAbbreviation,
    entryIntoForceDate = properties.entryIntoForceDate,
    entryIntoForceDateState = properties.entryIntoForceDateState,
    principleEntryIntoForceDate = properties.principleEntryIntoForceDate,
    principleEntryIntoForceDateState = properties.principleEntryIntoForceDateState,
    divergentEntryIntoForceDate = properties.divergentEntryIntoForceDate,
    divergentEntryIntoForceDateState = properties.divergentEntryIntoForceDateState,
    entryIntoForceNormCategory = properties.entryIntoForceNormCategory,
    expirationDate = properties.expirationDate,
    expirationDateState = properties.expirationDateState,
    isExpirationDateTemp = properties.isExpirationDateTemp,
    principleExpirationDate = properties.principleExpirationDate,
    principleExpirationDateState = properties.principleExpirationDateState,
    divergentExpirationDate = properties.divergentExpirationDate,
    divergentExpirationDateState = properties.divergentExpirationDateState,
    expirationNormCategory = properties.expirationNormCategory,
    announcementDate = properties.announcementDate,
    publicationDate = properties.publicationDate,
    citationDate = properties.citationDate,
    citationYear = properties.citationYear,
    printAnnouncementGazette = properties.printAnnouncementGazette,
    printAnnouncementYear = properties.printAnnouncementYear,
    printAnnouncementNumber = properties.printAnnouncementNumber,
    printAnnouncementPage = properties.printAnnouncementPage,
    printAnnouncementInfo = properties.printAnnouncementInfo,
    printAnnouncementExplanations = properties.printAnnouncementExplanations,
    digitalAnnouncementMedium = properties.digitalAnnouncementMedium,
    digitalAnnouncementDate = properties.digitalAnnouncementDate,
    digitalAnnouncementEdition = properties.digitalAnnouncementEdition,
    digitalAnnouncementYear = properties.digitalAnnouncementYear,
    digitalAnnouncementPage = properties.digitalAnnouncementPage,
    digitalAnnouncementArea = properties.digitalAnnouncementArea,
    digitalAnnouncementAreaNumber = properties.digitalAnnouncementAreaNumber,
    digitalAnnouncementInfo = properties.digitalAnnouncementInfo,
    digitalAnnouncementExplanations = properties.digitalAnnouncementExplanations,
    euAnnouncementGazette = properties.euAnnouncementGazette,
    euAnnouncementYear = properties.euAnnouncementYear,
    euAnnouncementSeries = properties.euAnnouncementSeries,
    euAnnouncementNumber = properties.euAnnouncementNumber,
    euAnnouncementPage = properties.euAnnouncementPage,
    euAnnouncementInfo = properties.euAnnouncementInfo,
    euAnnouncementExplanations = properties.euAnnouncementExplanations,
    otherOfficialAnnouncement = properties.otherOfficialAnnouncement,
    completeCitation = properties.completeCitation,
    statusNote = properties.statusNote,
    statusDescription = properties.statusDescription,
    statusDate = properties.statusDate,
    statusReference = properties.statusReference,
    repealNote = properties.repealNote,
    repealArticle = properties.repealArticle,
    repealDate = properties.repealDate,
    repealReferences = properties.repealReferences,
    reissueNote = properties.reissueNote,
    reissueArticle = properties.reissueArticle,
    reissueDate = properties.reissueDate,
    reissueReference = properties.reissueReference,
    otherStatusNote = properties.otherStatusNote,
    documentStatusWorkNote = properties.documentStatusWorkNote,
    documentStatusDescription = properties.documentStatusDescription,
    documentStatusDate = properties.documentStatusDate,
    documentStatusReference = properties.documentStatusReference,
    documentStatusEntryIntoForceDate = properties.documentStatusEntryIntoForceDate,
    documentStatusProof = properties.documentStatusProof,
    documentTextProof = properties.documentTextProof,
    otherDocumentNote = properties.otherDocumentNote,
    applicationScopeArea = properties.applicationScopeArea,
    applicationScopeStartDate = properties.applicationScopeStartDate,
    applicationScopeEndDate = properties.applicationScopeEndDate,
    categorizedReference = properties.categorizedReference,
    otherFootnote = properties.otherFootnote,
    footnoteChange = properties.footnoteChange,
    footnoteComment = properties.footnoteComment,
    footnoteDecision = properties.footnoteDecision,
    footnoteStateLaw = properties.footnoteStateLaw,
    footnoteEuLaw = properties.footnoteEuLaw,
    digitalEvidenceLink = properties.digitalEvidenceLink,
    digitalEvidenceRelatedData = properties.digitalEvidenceRelatedData,
    digitalEvidenceExternalDataNote = properties.digitalEvidenceExternalDataNote,
    digitalEvidenceAppendix = properties.digitalEvidenceAppendix,
    celexNumber = properties.celexNumber,
    ageIndicationStart = properties.ageIndicationStart,
    ageIndicationEnd = properties.ageIndicationEnd,
    text = properties.text,
)

private fun mapMetadataToMetadataSections(metadata: List<Metadatum<*>>): List<MetadataSection> {
    val divergentDocumentNumber = metadata.filter { it.type == MetadatumType.DIVERGENT_DOCUMENT_NUMBER }
    val frameKeywords = metadata.filter { it.type == MetadatumType.KEYWORD }
    val risAbbreviationInternationalLaw = metadata.filter { it.type == MetadatumType.RIS_ABBREVIATION_INTERNATIONAL_LAW }
    val unofficialLongTitle = metadata.filter { it.type == MetadatumType.UNOFFICIAL_LONG_TITLE }
    val unofficialShortTitle = metadata.filter { it.type == MetadatumType.UNOFFICIAL_SHORT_TITLE }
    val unofficialAbbreviation = metadata.filter { it.type == MetadatumType.UNOFFICIAL_ABBREVIATION }
    val unofficialReference = metadata.filter { it.type == MetadatumType.UNOFFICIAL_REFERENCE }
    val referenceNumber = metadata.filter { it.type == MetadatumType.REFERENCE_NUMBER }
    val definition = metadata.filter { it.type == MetadatumType.DEFINITION }
    val ageOfMajorityIndication = metadata.filter { it.type == MetadatumType.AGE_OF_MAJORITY_INDICATION }
    val validityRule = metadata.filter { it.type == MetadatumType.VALIDITY_RULE }
    val participationType = metadata.filter { it.type == MetadatumType.PARTICIPATION_TYPE }
    val participationInstitution = metadata.filter { it.type == MetadatumType.PARTICIPATION_INSTITUTION }
    val leadJurisdiction = metadata.filter { it.type == MetadatumType.LEAD_JURISDICTION }
    val leadUnit = metadata.filter { it.type == MetadatumType.LEAD_UNIT }
    val subjectFna = metadata.filter { it.type == MetadatumType.SUBJECT_FNA }
    val subjectGesta = metadata.filter { it.type == MetadatumType.SUBJECT_GESTA }

    return listOf(
        MetadataSection(MetadataSectionName.GENERAL_INFORMATION, frameKeywords + divergentDocumentNumber + risAbbreviationInternationalLaw),
        MetadataSection(MetadataSectionName.HEADINGS_AND_ABBREVIATIONS, unofficialAbbreviation + unofficialShortTitle + unofficialLongTitle),
        MetadataSection(MetadataSectionName.UNOFFICIAL_REFERENCE, unofficialReference),
        MetadataSection(MetadataSectionName.REFERENCE_NUMBER, referenceNumber),
        MetadataSection(MetadataSectionName.DEFINITION, definition),
        MetadataSection(MetadataSectionName.AGE_OF_MAJORITY_INDICATION, ageOfMajorityIndication),
        MetadataSection(MetadataSectionName.VALIDITY_RULE, validityRule),
        MetadataSection(MetadataSectionName.SUBJECT_AREA, subjectFna + subjectGesta),
        MetadataSection(MetadataSectionName.LEAD, leadJurisdiction + leadUnit),
        MetadataSection(MetadataSectionName.PARTICIPATING_INSTITUTIONS, participationInstitution + participationType),
    )
}
