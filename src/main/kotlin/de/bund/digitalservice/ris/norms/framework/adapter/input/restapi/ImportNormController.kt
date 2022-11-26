package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi

import ApiConfiguration
import de.bund.digitalservice.ris.norms.application.port.input.ImportNormUseCase
import decodeLocalDate
import encodeGuid
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
        lateinit var longTitle: String
        var articles: List<ArticleRequestSchema> = listOf()
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
        var unofficialTitle: String? = null
        var unofficialShortTitle: String? = null
        var unofficialAbbreviation: String? = null
        var risAbbreviation: String? = null

        fun toUseCaseData(): ImportNormUseCase.NormData {
            return ImportNormUseCase.NormData(
                longTitle = this.longTitle,
                articles = this.articles.map { it.toUseCaseData() },
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
                subjectBgb3 = this.subjectBgb3,
                unofficialTitle = this.unofficialTitle,
                unofficialShortTitle = this.unofficialShortTitle,
                unofficialAbbreviation = this.unofficialAbbreviation,
                risAbbreviation = this.risAbbreviation
            )
        }
    }

    class ArticleRequestSchema {
        var title: String? = null
        lateinit var marker: String
        var paragraphs: List<ParagraphRequestSchema> = listOf()

        fun toUseCaseData(): ImportNormUseCase.ArticleData {
            return ImportNormUseCase.ArticleData(title, marker, paragraphs.map { it.toUseCaseData() })
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
