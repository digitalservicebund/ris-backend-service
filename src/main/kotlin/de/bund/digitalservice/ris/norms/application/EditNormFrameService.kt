package de.bund.digitalservice.ris.norms.application

import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import de.bund.digitalservice.ris.norms.application.port.output.EditNormOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class EditNormFrameService(private val editNormOutputPort: EditNormOutputPort) :
    EditNormFrameUseCase {

    override fun editNormFrame(command: EditNormFrameUseCase.Command): Mono<Boolean> {
        val properties = command.properties
        return editNormOutputPort.editNorm(
            Norm(
                guid = command.guid,
                longTitle = properties.longTitle,
                officialShortTitle = properties.officialShortTitle,
                officialAbbreviation = properties.officialAbbreviation,
                referenceNumber = properties.referenceNumber,
                publicationDate = properties.publicationDate,
                announcementDate = properties.announcementDate,
                citationDate = properties.citationDate,
                frameKeywords = properties.frameKeywords,
                authorEntity = properties.authorEntity,
                authorDecidingBody = properties.authorDecidingBody,
                authorIsResolutionMajority = properties.authorIsResolutionMajority,
                leadJurisdiction = properties.leadJurisdiction,
                leadUnit = properties.leadUnit,
                participationType = properties.participationType,
                participationInstitution = properties.participationInstitution,
                documentTypeName = properties.documentTypeName,
                documentNormCategory = properties.documentNormCategory,
                documentTemplateName = properties.documentTemplateName,
                subjectFna = properties.subjectFna,
                subjectPreviousFna = properties.subjectPreviousFna,
                subjectGesta = properties.subjectGesta,
                subjectBgb3 = properties.subjectBgb3
            )
        )
    }
}
