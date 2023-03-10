package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import de.bund.digitalservice.ris.norms.application.port.output.EditNormOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Norm
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
    guid, listOf(), properties.officialLongTitle, properties.risAbbreviation, properties.risAbbreviationInternationalLaw,
    properties.documentNumber, properties.divergentDocumentNumber, properties.documentCategory, properties.frameKeywords,
    properties.documentTypeName, properties.documentNormCategory, properties.documentTemplateName, properties.providerEntity,
    properties.providerDecidingBody, properties.providerIsResolutionMajority, properties.participationType,
    properties.participationInstitution, properties.leadJurisdiction, properties.leadUnit, properties.subjectFna,
    properties.subjectPreviousFna, properties.subjectGesta, properties.subjectBgb3,
    properties.officialShortTitle, properties.officialAbbreviation, properties.unofficialLongTitle,
    properties.unofficialShortTitle, properties.unofficialAbbreviation,
    properties.entryIntoForceDate, properties.entryIntoForceDateState, properties.principleEntryIntoForceDate,
    properties.principleEntryIntoForceDateState, properties.divergentEntryIntoForceDate, properties.divergentEntryIntoForceDateState,
    properties.entryIntoForceNormCategory,
    properties.expirationDate, properties.expirationDateState, properties.isExpirationDateTemp, properties.principleExpirationDate,
    properties.principleExpirationDateState, properties.divergentExpirationDate, properties.divergentExpirationDateState,
    properties.expirationNormCategory, properties.announcementDate, properties.publicationDate, properties.citationDate,
    properties.citationYear,
    properties.printAnnouncementGazette, properties.printAnnouncementYear, properties.printAnnouncementNumber,
    properties.printAnnouncementPage, properties.printAnnouncementInfo, properties.printAnnouncementExplanations,
    properties.digitalAnnouncementMedium, properties.digitalAnnouncementDate, properties.digitalAnnouncementEdition,
    properties.digitalAnnouncementYear, properties.digitalAnnouncementPage, properties.digitalAnnouncementArea,
    properties.digitalAnnouncementAreaNumber, properties.digitalAnnouncementInfo, properties.digitalAnnouncementExplanations,
    properties.euAnnouncementGazette, properties.euAnnouncementYear, properties.euAnnouncementSeries,
    properties.euAnnouncementNumber, properties.euAnnouncementPage, properties.euAnnouncementInfo,
    properties.euAnnouncementExplanations, properties.otherOfficialAnnouncement, properties.unofficialReference,
    properties.completeCitation, properties.statusNote, properties.statusDescription, properties.statusDate,
    properties.statusReference, properties.repealNote, properties.repealArticle, properties.repealDate,
    properties.repealReferences, properties.reissueNote, properties.reissueArticle, properties.reissueDate,
    properties.reissueReference, properties.otherStatusNote, properties.documentStatusWorkNote, properties.documentStatusDescription,
    properties.documentStatusDate, properties.documentStatusReference, properties.documentStatusEntryIntoForceDate,
    properties.documentStatusProof, properties.documentTextProof, properties.otherDocumentNote, properties.applicationScopeArea,
    properties.applicationScopeStartDate, properties.applicationScopeEndDate, properties.categorizedReference,
    properties.otherFootnote, properties.footnoteChange, properties.footnoteComment, properties.footnoteDecision,
    properties.footnoteStateLaw, properties.footnoteEuLaw, properties.validityRule, properties.digitalEvidenceLink,
    properties.digitalEvidenceRelatedData, properties.digitalEvidenceExternalDataNote, properties.digitalEvidenceAppendix,
    properties.referenceNumber, properties.celexNumber, properties.ageIndicationStart, properties.ageIndicationEnd,
    properties.definition, properties.ageOfMajorityIndication, properties.text,
)
