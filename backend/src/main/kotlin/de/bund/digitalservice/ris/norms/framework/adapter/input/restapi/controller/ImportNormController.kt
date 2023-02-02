package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import de.bund.digitalservice.ris.norms.application.port.input.ImportNormUseCase
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.ApiConfiguration
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.decodeLocalDate
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.decodeUndefinedDate
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeGuid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.net.URI

@RestController
@RequestMapping(ApiConfiguration.API_BASE_PATH)
class ImportNormController(private val importNormService: ImportNormUseCase) {
    @PostMapping
    fun createNorm(@RequestBody resource: NormRequestSchema): Mono<ResponseEntity<Void>> {
        val command = ImportNormUseCase.Command(resource.toUseCaseData())

        return importNormService
            .importNorm(command)
            .map { guid -> encodeGuid(guid) }
            .map { encodedGuid -> URI("${ApiConfiguration.API_BASE_PATH}/$encodedGuid") }
            .map { uri -> ResponseEntity.created(uri).build<Void>() }
            .onErrorReturn(ResponseEntity.internalServerError().build())
    }

    class NormRequestSchema {
        lateinit var officialLongTitle: String
        var articles: List<ArticleRequestSchema> = listOf()
        var officialShortTitle: String? = null
        var officialAbbreviation: String? = null
        var referenceNumber: String? = null
        var announcementDate: String? = null
        var citationDate: String? = null
        var frameKeywords: String? = null
        var providerEntity: String? = null
        var providerDecidingBody: String? = null
        var providerIsResolutionMajority: Boolean? = null
        var leadJurisdiction: String? = null
        var leadUnit: String? = null
        var participationType: String? = null
        var participationInstitution: String? = null
        var subjectFna: String? = null
        var subjectGesta: String? = null
        val documentNumber: String? = null
        val documentCategory: String? = null
        val risAbbreviationInternationalLaw: String? = null
        val unofficialReference: String? = null
        val applicationScopeArea: String? = null
        val applicationScopeStartDate: String? = null
        val applicationScopeEndDate: String? = null
        val validityRule: String? = null
        val celexNumber: String? = null
        val definition: String? = null
        val categorizedReference: String? = null
        val otherFootnote: String? = null
        val expirationDate: String? = null
        val entryIntoForceDate: String? = null
        var unofficialLongTitle: String? = null
        var unofficialShortTitle: String? = null
        var unofficialAbbreviation: String? = null
        var risAbbreviation: String? = null
        val printAnnouncementGazette: String? = null
        val printAnnouncementYear: String? = null
        val printAnnouncementPage: String? = null
        val documentStatusWorkNote: String? = null
        val documentStatusDescription: String? = null
        var documentStatusDate: String? = null
        val statusNote: String? = null
        val statusDescription: String? = null
        val statusDate: String? = null
        val statusReference: String? = null
        val repealNote: String? = null
        val repealArticle: String? = null
        val repealDate: String? = null
        val repealReferences: String? = null
        val reissueNote: String? = null
        val reissueArticle: String? = null
        val reissueDate: String? = null
        val reissueReference: String? = null
        val otherStatusNote: String? = null
        val text: String? = null
        val ageOfMajorityIndication: String? = null
        val divergentExpirationDate: String? = null
        val divergentExpirationDateState: String? = null
        val principleExpirationDate: String? = null
        val principleExpirationDateState: String? = null
        val expirationNormCategory: String? = null
        val divergentEntryIntoForceDate: String? = null
        val divergentEntryIntoForceDateState: String? = null
        val principleEntryIntoForceDate: String? = null
        val principleEntryIntoForceDateState: String? = null
        var entryIntoForceNormCategory: String? = null
        val entryIntoForceDateState: String? = null
        val expirationDateState: String? = null

        fun toUseCaseData(): ImportNormUseCase.NormData {
            return ImportNormUseCase.NormData(
                officialLongTitle = this.officialLongTitle,
                articles = this.articles.map { it.toUseCaseData() },
                officialShortTitle = this.officialShortTitle,
                officialAbbreviation = this.officialAbbreviation,
                referenceNumber = this.referenceNumber,
                announcementDate = decodeLocalDate(this.announcementDate),
                citationDate = decodeLocalDate(this.citationDate),
                frameKeywords = this.frameKeywords,
                providerEntity = this.providerEntity,
                providerDecidingBody = this.providerDecidingBody,
                providerIsResolutionMajority = this.providerIsResolutionMajority,
                leadJurisdiction = this.leadJurisdiction,
                leadUnit = this.leadUnit,
                participationType = this.participationType,
                participationInstitution = this.participationInstitution,
                subjectFna = this.subjectFna,
                subjectGesta = this.subjectGesta,
                documentNumber = this.documentNumber,
                documentCategory = this.documentCategory,
                risAbbreviationInternationalLaw = this.risAbbreviationInternationalLaw,
                unofficialReference = this.unofficialReference,
                applicationScopeArea = this.applicationScopeArea,
                applicationScopeStartDate = decodeLocalDate(this.applicationScopeStartDate),
                applicationScopeEndDate = decodeLocalDate(this.applicationScopeEndDate),
                validityRule = this.validityRule,
                celexNumber = this.celexNumber,
                definition = this.definition,
                categorizedReference = this.categorizedReference,
                otherFootnote = this.otherFootnote,
                expirationDate = decodeLocalDate(this.expirationDate),
                entryIntoForceDate = decodeLocalDate(this.entryIntoForceDate),
                unofficialLongTitle = this.unofficialLongTitle,
                unofficialShortTitle = this.unofficialShortTitle,
                unofficialAbbreviation = this.unofficialAbbreviation,
                risAbbreviation = this.risAbbreviation,
                printAnnouncementGazette = this.printAnnouncementGazette,
                printAnnouncementYear = this.printAnnouncementYear,
                printAnnouncementPage = this.printAnnouncementPage,
                documentStatusWorkNote = this.documentStatusWorkNote,
                documentStatusDescription = this.documentStatusDescription,
                documentStatusDate = decodeLocalDate(this.documentStatusDate),
                statusNote = this.statusNote,
                statusDescription = this.statusDescription,
                statusDate = decodeLocalDate(this.statusDate),
                statusReference = this.statusReference,
                repealNote = this.repealNote,
                repealArticle = this.repealArticle,
                repealDate = decodeLocalDate(this.repealDate),
                repealReferences = this.repealReferences,
                reissueNote = this.reissueNote,
                reissueArticle = this.reissueArticle,
                reissueDate = decodeLocalDate(this.reissueDate),
                reissueReference = this.reissueReference,
                otherStatusNote = this.otherStatusNote,
                text = this.text,
                ageOfMajorityIndication = this.ageOfMajorityIndication,
                divergentExpirationDate = decodeLocalDate(this.divergentExpirationDate),
                divergentExpirationDateState = decodeUndefinedDate(this.divergentExpirationDateState),
                principleExpirationDate = decodeLocalDate(this.principleExpirationDate),
                principleExpirationDateState = decodeUndefinedDate(this.principleExpirationDateState),
                expirationNormCategory = this.expirationNormCategory,
                divergentEntryIntoForceDate = decodeLocalDate(this.divergentEntryIntoForceDate),
                divergentEntryIntoForceDateState = decodeUndefinedDate(this.divergentEntryIntoForceDateState),
                principleEntryIntoForceDate = decodeLocalDate(this.principleEntryIntoForceDate),
                principleEntryIntoForceDateState = decodeUndefinedDate(this.principleEntryIntoForceDateState),
                entryIntoForceNormCategory = this.entryIntoForceNormCategory,
                entryIntoForceDateState = decodeUndefinedDate(this.entryIntoForceDateState),
                expirationDateState = decodeUndefinedDate(this.expirationDateState),
            )
        }
    }

    class ArticleRequestSchema {
        var title: String? = null
        lateinit var marker: String
        var paragraphs: List<ParagraphRequestSchema> = listOf()

        fun toUseCaseData(): ImportNormUseCase.ArticleData {
            return ImportNormUseCase.ArticleData(
                title,
                marker,
                paragraphs.map { it.toUseCaseData() },
            )
        }
    }

    class ParagraphRequestSchema {
        var marker: String? = null
        lateinit var text: String

        fun toUseCaseData(): ImportNormUseCase.ParagraphData {
            return ImportNormUseCase.ParagraphData(marker, text)
        }
    }
}
