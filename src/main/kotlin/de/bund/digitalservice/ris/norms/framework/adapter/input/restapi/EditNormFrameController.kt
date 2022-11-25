package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi

import ApiConfiguration
import de.bund.digitalservice.ris.norms.application.port.input.EditNormFrameUseCase
import decodeGuid
import decodeLocalDate
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping(ApiConfiguration.API_BASE_PATH)
class EditNormFrameController(private val editNormFrameService: EditNormFrameUseCase) {

    @PutMapping(path = ["/{guid}"])
    fun editNormFrame(
        @PathVariable guid: String,
        @RequestBody request: NormFramePropertiesRequestSchema
    ): Mono<ResponseEntity<Void>> {
        val properties = request.toUseCaseData()
        val command = EditNormFrameUseCase.Command(decodeGuid(guid), properties)

        return editNormFrameService
            .editNormFrame(command)
            .map { ResponseEntity.noContent().build<Void>() }
            .onErrorReturn(ResponseEntity.internalServerError().build())
    }

    class NormFramePropertiesRequestSchema {
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

        fun toUseCaseData(): EditNormFrameUseCase.NormFrameProperties {
            return EditNormFrameUseCase.NormFrameProperties(
                longTitle = this.longTitle,
                officialShortTitle = this.officialShortTitle,
                officialAbbreviation = this.officialAbbreviation,
                referenceNumber = this.referenceNumber,
                publicationDate = decodeLocalDate(this.publicationDate),
                announcementDate = decodeLocalDate(this.announcementDate),
                citationDate = decodeLocalDate(this.citationDate),
                frameKeywords = this.frameKeywords,
                authorEntity = this.authorEntity,
                authorDecidingBody = this.authorDecidingBody,
                authorIsResolutionMajority = this.authorIsResolutionMajority,
                leadJurisdiction = this.leadJurisdiction,
                leadUnit = this.leadUnit,
                participationType = this.participationType,
                participationInstitution = this.participationInstitution,
                documentTypeName = this.documentTypeName,
                documentNormCategory = this.documentNormCategory,
                documentTemplateName = this.documentTemplateName,
                subjectFna = this.subjectFna,
                subjectPreviousFna = this.subjectPreviousFna,
                subjectGesta = this.subjectGesta,
                subjectBgb3 = this.subjectBgb3
            )
        }
    }
}
