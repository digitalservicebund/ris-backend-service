package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi

import ApiConfiguration
import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase.Command
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping(ApiConfiguration.API_BASE_PATH)
class EditNormFrameController(private val editNormFrameService: EditNormFrameUseCase) {

    @PutMapping(path = ["/{guid}"])
    fun editNormFrame(
        @PathVariable guid: String,
        @RequestBody request: EditNormFrameRequestSchema
    ): Mono<ResponseEntity<Void>> {
        val command =
            EditNormFrameUseCase.Command(
                guid = UUID.fromString(guid),
                longTitle = request.longTitle,
                officialShortTitle = request.officialShortTitle,
                officialAbbreviation = request.officialAbbreviation,
                referenceNumber = request.referenceNumber,
                publicationDate = request.publicationDate,
                announcementDate = request.announcementDate,
                citationDate = request.citationDate,
                frameKeywords = request.frameKeywords,
                authorEntity = request.authorEntity,
                authorDecidingBody = request.authorDecidingBody,
                authorIsResolutionMajority = request.authorIsResolutionMajority,
                leadJurisdiction = request.leadJurisdiction,
                leadUnit = request.leadUnit,
                participationType = request.participationType,
                participationInstitution = request.participationInstitution,
                documentTypeName = request.documentTypeName,
                documentNormCategory = request.documentNormCategory,
                documentTemplateName = request.documentTemplateName,
                subjectFna = request.subjectFna,
                subjectPreviousFna = request.subjectPreviousFna,
                subjectGesta = request.subjectGesta,
                subjectBgb3 = request.subjectBgb3
            )

        return editNormFrameService
            .editNormFrame(command)
            .map { ResponseEntity.noContent().build<Void>() }
            .onErrorReturn(ResponseEntity.internalServerError().build())
    }

    class EditNormFrameRequestSchema {
        lateinit var longTitle: String
        var officialShortTitle: String? = null
        var officialAbbreviation: String? = null
        var referenceNumber: String? = null
        var publicationDate: String? = null
        var announcementDate: String? = null
        var citationDate: String? = null
        var frameKeywords: String? = null
        var authorEntity: String? = null
        var authorDecidingBody: String? = null
        var authorIsResolutionMajority: Boolean? = null
        var leadJurisdiction: String? = null
        var leadUnit: String? = null
        var participationType: String? = null
        var participationInstitution: String? = null
        var documentTypeName: String? = null
        var documentNormCategory: String? = null
        var documentTemplateName: String? = null
        var subjectFna: String? = null
        var subjectPreviousFna: String? = null
        var subjectGesta: String? = null
        var subjectBgb3: String? = null
    }
}
