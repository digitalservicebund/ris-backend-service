package de.bund.digitalservice.ris.norms.application

import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import de.bund.digitalservice.ris.norms.application.port.output.EditNormOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDate

@Service
class EditNormFrameService(
    private val editNormOutputPort: EditNormOutputPort
) : EditNormFrameUseCase {

    override fun editNormFrame(command: EditNormFrameUseCase.Command): Mono<Boolean> {
        return editNormOutputPort.editNorm(
            Norm(
                command.guid, command.longTitle, officialShortTitle = command.officialShortTitle,
                officialAbbreviation = command.officialAbbreviation, referenceNumber = command.referenceNumber,
                publicationDate = if (command.publicationDate != null) LocalDate.parse(command.publicationDate) else null,
                announcementDate = if (command.announcementDate != null) LocalDate.parse(command.announcementDate) else null,
                citationDate = if (command.citationDate != null) LocalDate.parse(command.citationDate) else null,
                frameKeywords = command.frameKeywords, authorEntity = command.authorEntity, authorDecidingBody = command.authorDecidingBody,
                authorIsResolutionMajority = command.authorIsResolutionMajority, leadJurisdiction = command.leadJurisdiction,
                leadUnit = command.leadUnit, participationType = command.participationType, participationInstitution = command.participationInstitution,
                documentTypeName = command.documentTypeName, documentNormCategory = command.documentNormCategory,
                documentTemplateName = command.documentTemplateName, subjectFna = command.subjectFna,
                subjectPreviousFna = command.subjectPreviousFna, subjectGesta = command.subjectGesta, subjectBgb3 = command.subjectBgb3
            )
        )
    }
}
