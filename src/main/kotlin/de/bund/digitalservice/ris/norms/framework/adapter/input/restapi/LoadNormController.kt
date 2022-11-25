package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi

import de.bund.digitalservice.ris.norms.application.port.input.LoadNormUseCase
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import encodeGuid
import encodeLocalDate
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping(ApiConfiguration.API_BASE_PATH)
class LoadNormController(private val loadNormService: LoadNormUseCase) {

    @GetMapping(path = ["/{guid}"])
    fun getNormByGuid(@PathVariable guid: String): Mono<ResponseEntity<NormResponseSchema>> {
        val query = LoadNormUseCase.Query(UUID.fromString(guid))

        return loadNormService
            .loadNorm(query)
            .map({ norm -> NormResponseSchema.fromUseCaseData(norm) })
            .map({ normResponseSchema -> ResponseEntity.ok(normResponseSchema) })
            .defaultIfEmpty(ResponseEntity.notFound().build<NormResponseSchema>())
            .onErrorReturn(ResponseEntity.internalServerError().build())
    }

    data class NormResponseSchema
    private constructor(
        val guid: String,
        val longTitle: String,
        val articles: List<ArticleResponseSchema>,
        val officialShortTitle: String?,
        val officialAbbreviation: String?,
        val referenceNumber: String?,
        val publicationDate: String?,
        val announcementDate: String?,
        val citationDate: String?,
        val frameKeywords: String?,
        val authorEntity: String?,
        val authorDecidingBody: String?,
        val authorIsResolutionMajority: Boolean?,
        val leadJurisdiction: String?,
        val leadUnit: String?,
        val participationType: String?,
        val participationInstitution: String?,
        val documentTypeName: String?,
        val documentNormCategory: String?,
        val documentTemplateName: String?,
        val subjectFna: String?,
        val subjectPreviousFna: String?,
        val subjectGesta: String?,
        val subjectBgb3: String?
    ) {
        companion object {
            fun fromUseCaseData(data: Norm): NormResponseSchema {
                val articles = data.articles.map { ArticleResponseSchema.fromUseCaseData(it) }
                return NormResponseSchema(
                    guid = encodeGuid(data.guid),
                    longTitle = data.longTitle,
                    articles = articles,
                    officialShortTitle = data.officialShortTitle,
                    officialAbbreviation = data.officialAbbreviation,
                    referenceNumber = data.referenceNumber,
                    publicationDate = encodeLocalDate(data.publicationDate),
                    announcementDate = encodeLocalDate(data.announcementDate),
                    citationDate = encodeLocalDate(data.citationDate),
                    frameKeywords = data.frameKeywords,
                    authorEntity = data.authorEntity,
                    authorDecidingBody = data.authorDecidingBody,
                    authorIsResolutionMajority = data.authorIsResolutionMajority,
                    leadJurisdiction = data.leadJurisdiction,
                    leadUnit = data.leadUnit,
                    participationType = data.participationType,
                    participationInstitution = data.participationInstitution,
                    documentTypeName = data.documentTypeName,
                    documentNormCategory = data.documentNormCategory,
                    documentTemplateName = data.documentTemplateName,
                    subjectFna = data.subjectFna,
                    subjectPreviousFna = data.subjectPreviousFna,
                    subjectGesta = data.subjectGesta,
                    subjectBgb3 = data.subjectBgb3
                )
            }
        }
    }

    data class ArticleResponseSchema
    private constructor(
        val guid: String,
        var title: String? = null,
        val marker: String,
        val paragraphs: List<ParagraphResponseSchema>
    ) {
        companion object {
            fun fromUseCaseData(data: Article): ArticleResponseSchema {
                val paragraphs = data.paragraphs.map { ParagraphResponseSchema.fromUseCaseData(it) }
                return ArticleResponseSchema(encodeGuid(data.guid), data.title, data.marker, paragraphs)
            }
        }
    }

    data class ParagraphResponseSchema
    private constructor(val guid: String, val marker: String? = null, val text: String) {
        companion object {
            fun fromUseCaseData(data: Paragraph): ParagraphResponseSchema {
                return ParagraphResponseSchema(encodeGuid(data.guid), data.marker, data.text)
            }
        }
    }
}
