package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi

import ApiConfiguration
import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
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
        val command = EditNormFrameUseCase.Command(
            UUID.fromString(guid), request.longTitle, request.officialShortTitle, request.officialAbbreviation,
            request.referenceNumber, request.publicationDate, request.announcementDate, request.citationDate, request.frameKeywords, request.authorEntity,
            request.authorDecidingBody, request.authorIsResolutionMajority, request.leadJurisdiction, request.leadUnit, request.participationType,
            request.participationInstitution, request.documentTypeName, request.documentNormCategory, request.documentTemplateName,
            request.subjectFna, request.subjectPreviousFna, request.subjectGesta, request.subjectBgb3
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
