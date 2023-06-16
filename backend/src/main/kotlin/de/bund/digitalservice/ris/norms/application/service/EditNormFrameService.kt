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
    guid = guid,
    articles = listOf(),
    metadataSections = properties.metadataSections,
    officialLongTitle = properties.officialLongTitle,
    risAbbreviation = properties.risAbbreviation,
    documentNumber = properties.documentNumber,
    documentCategory = properties.documentCategory,
    officialShortTitle = properties.officialShortTitle,
    officialAbbreviation = properties.officialAbbreviation,
    announcementDate = properties.announcementDate,
    publicationDate = properties.publicationDate,
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
    otherFootnote = properties.otherFootnote,
    footnoteChange = properties.footnoteChange,
    footnoteComment = properties.footnoteComment,
    footnoteDecision = properties.footnoteDecision,
    footnoteStateLaw = properties.footnoteStateLaw,
    footnoteEuLaw = properties.footnoteEuLaw,
    celexNumber = properties.celexNumber,
    text = properties.text,
)
