package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi

import ApiConfiguration
import de.bund.digitalservice.ris.norms.application.port.input.ImportNormUseCase
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
            .map { guid -> URI("${ApiConfiguration.API_BASE_PATH}/$guid") }
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

        fun toUseCaseData(): ImportNormUseCase.NormData {
            return ImportNormUseCase.NormData(
                longTitle = longTitle,
                articles = articles.map { it.toUseCaseData() },
                officialShortTitle = officialShortTitle,
                officialAbbreviation = officialAbbreviation,
                referenceNumber = referenceNumber,
                publicationDate = publicationDate,
                announcementDate = announcementDate,
                citationDate = citationDate,
                frameKeywords = frameKeywords,
                authorEntity = authorEntity,
                authorDecidingBody = authorDecidingBody,
                authorIsResolutionMajority = authorIsResolutionMajority,
                leadJurisdiction = leadJurisdiction,
                leadUnit = leadUnit,
                participationType = participationType,
                participationInstitution = participationInstitution,
                documentTypeName = documentTypeName,
                documentNormCategory = documentNormCategory,
                documentTemplateName = documentTemplateName,
                subjectFna = subjectFna,
                subjectPreviousFna = subjectPreviousFna,
                subjectGesta = subjectGesta,
                subjectBgb3 = subjectBgb3
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
