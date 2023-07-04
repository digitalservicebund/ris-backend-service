package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import de.bund.digitalservice.ris.norms.application.port.input.LoadNormUseCase
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.FileReference
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.ApiConfiguration
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeEli
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeGuid
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeLocalDateTime
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping(ApiConfiguration.API_NORMS_PATH)
class LoadNormController(private val loadNormService: LoadNormUseCase) {

    @GetMapping(path = ["/{guid}"])
    fun getNormByGuid(@PathVariable guid: String): Mono<ResponseEntity<NormResponseSchema>> {
        val query = LoadNormUseCase.Query(UUID.fromString(guid))

        return loadNormService
            .loadNorm(query)
            .map { norm -> NormResponseSchema.fromUseCaseData(norm) }
            .map { normResponseSchema -> ResponseEntity.ok(normResponseSchema) }
            .defaultIfEmpty(ResponseEntity.notFound().build<NormResponseSchema>())
            .onErrorReturn(ResponseEntity.internalServerError().build())
    }

    data class NormResponseSchema
    private constructor(
        val guid: String,
        val articles: List<ArticleResponseSchema>,
        val metadataSections: List<MetadataSectionResponseSchema>,
        var eli: String,
        var files: List<FileReferenceResponseSchema>,
    ) {
        companion object {
            fun fromUseCaseData(data: Norm): NormResponseSchema {
                val articles = data.articles.map(ArticleResponseSchema::fromUseCaseData)
                val files = data.files.map(FileReferenceResponseSchema::fromUseCaseData)
                val metadataSections = data.metadataSections.map(MetadataSectionResponseSchema::fromUseCaseData)
                return NormResponseSchema(
                    encodeGuid(data.guid),
                    articles,
                    metadataSections,
                    encodeEli(data.eli),
                    files = files,
                )
            }
        }
    }

    data class ArticleResponseSchema
    private constructor(
        val guid: String,
        var title: String? = null,
        val marker: String,
        val paragraphs: List<ParagraphResponseSchema>,
    ) {
        companion object {
            fun fromUseCaseData(data: Article): ArticleResponseSchema {
                val paragraphs = data.paragraphs.map { ParagraphResponseSchema.fromUseCaseData(it) }
                return ArticleResponseSchema(
                    encodeGuid(data.guid),
                    data.title,
                    data.marker,
                    paragraphs,
                )
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

    data class FileReferenceResponseSchema private constructor(val name: String, val hash: String, val createdAt: String) {
        companion object {
            fun fromUseCaseData(data: FileReference) = FileReferenceResponseSchema(
                data.name,
                data.hash,
                encodeLocalDateTime(data.createdAt),
            )
        }
    }

    data class MetadataSectionResponseSchema private constructor(val name: MetadataSectionName, val order: Int, val metadata: List<MetadatumResponseSchema>, val sections: List<MetadataSectionResponseSchema>?) {
        companion object {
            fun fromUseCaseData(metadataSection: MetadataSection): MetadataSectionResponseSchema {
                val metadata = metadataSection.metadata.map { MetadatumResponseSchema.fromUseCaseData(it) }
                val childSections = metadataSection.sections?.map { fromUseCaseData(it) }
                return MetadataSectionResponseSchema(name = metadataSection.name, order = metadataSection.order, metadata = metadata, sections = childSections)
            }
        }
    }

    data class MetadatumResponseSchema private constructor(val value: String, val type: String, val order: Int) {
        companion object {
            fun fromUseCaseData(metadatum: Metadatum<*>): MetadatumResponseSchema {
                val value: String = metadatum.value.toString()
                val type = metadatum.type.name
                return MetadatumResponseSchema(value = value, type = type, order = metadatum.order)
            }
        }
    }
}
