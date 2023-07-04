package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.norms.application.port.input.LoadNormUseCase
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.FileReference
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeEli
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeGuid
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeLocalDateTime
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import utils.convertLoadNormResponseTestSchemaToJson
import utils.createRandomNorm
import utils.createSimpleSections
import java.util.*

@ExtendWith(SpringExtension::class)
@WebFluxTest(controllers = [LoadNormController::class])
@WithMockUser
class LoadNormControllerTest {
    @Autowired lateinit var webClient: WebTestClient

    @MockkBean lateinit var loadNormService: LoadNormUseCase

    @Test
    fun `it calls the load norm service with the correct query to get a norm by GUID`() {
        val norm =
            Norm(
                UUID.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8"),
            )

        every { loadNormService.loadNorm(any()) } returns Mono.just(norm)

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/761b5537-5aa5-4901-81f7-fbf7e040a7c8")
            .exchange()

        val query = slot<LoadNormUseCase.Query>()
        verify(exactly = 1) { loadNormService.loadNorm(capture(query)) }
        assertThat(query.captured.guid.toString()).isEqualTo("761b5537-5aa5-4901-81f7-fbf7e040a7c8")
    }

    @Test
    fun `it responds with ok status if the norm was loaded successfully`() {
        val norm =
            Norm(
                UUID.fromString("761b5537-5aa5-4901-81f7-fbf7e040a7c8"),
            )

        every { loadNormService.loadNorm(any()) } returns Mono.just(norm)

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/761b5537-5aa5-4901-81f7-fbf7e040a7c8")
            .exchange()
            .expectStatus()
            .isOk()
    }

    @Test
    fun `it maps the norm entity to the expected data schema`() {
        val norm = createRandomNorm().copy(
            metadataSections = createSimpleSections(),
        )
        val responseJson = convertLoadNormResponseTestSchemaToJson(NormResponseTestSchema.fromUseCaseData(norm))

        every { loadNormService.loadNorm(any()) } returns Mono.just(norm)

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/72631e54-78a4-11d0-bcf7-00aa00b7b32a")
            .exchange()
            .expectBody()
            .json(responseJson, true)
    }

    @Test
    fun `it sends a not found response with empty body if the load norm service responds with empty`() {
        every { loadNormService.loadNorm(any()) } returns Mono.empty()

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/72631e54-78a4-11d0-bcf7-00aa00b7b32a")
            .exchange()
            .expectStatus()
            .isNotFound()
            .expectBody()
            .isEmpty()
    }

    @Test
    fun `it sends an internal error response if the load norm service throws an exception`() {
        every { loadNormService.loadNorm(any()) } throws Error()

        webClient
            .mutateWith(csrf())
            .get()
            .uri("/api/v1/norms/72631e54-78a4-11d0-bcf7-00aa00b7b32a")
            .exchange()
            .expectStatus()
            .is5xxServerError()
    }

    data class NormResponseTestSchema
    private constructor(
        val guid: String,
        val articles: List<ArticleResponseTestSchema>,
        val metadataSections: List<MetadataSectionResponseTestSchema>,
        var eli: String,
        var files: List<FileReferenceResponseTestSchema>,
    ) {
        companion object {
            fun fromUseCaseData(data: Norm): NormResponseTestSchema {
                val articles = data.articles.map(ArticleResponseTestSchema::fromUseCaseData)
                val files = data.files.map(FileReferenceResponseTestSchema::fromUseCaseData)
                val metadataSections = data.metadataSections.map(MetadataSectionResponseTestSchema::fromUseCaseData)
                return NormResponseTestSchema(
                    encodeGuid(data.guid),
                    articles,
                    metadataSections,
                    encodeEli(data.eli),
                    files = files,
                )
            }
        }
    }

    data class ArticleResponseTestSchema
    private constructor(
        val guid: String,
        var title: String? = null,
        val marker: String,
        val paragraphs: List<ParagraphResponseTestSchema>,
    ) {
        companion object {
            fun fromUseCaseData(data: Article): ArticleResponseTestSchema {
                val paragraphs = data.paragraphs.map { ParagraphResponseTestSchema.fromUseCaseData(it) }
                return ArticleResponseTestSchema(
                    encodeGuid(data.guid),
                    data.title,
                    data.marker,
                    paragraphs,
                )
            }
        }
    }

    data class ParagraphResponseTestSchema
    private constructor(val guid: String, val marker: String? = null, val text: String) {
        companion object {
            fun fromUseCaseData(data: Paragraph): ParagraphResponseTestSchema {
                return ParagraphResponseTestSchema(encodeGuid(data.guid), data.marker, data.text)
            }
        }
    }

    data class FileReferenceResponseTestSchema private constructor(val name: String, val hash: String, val createdAt: String) {
        companion object {
            fun fromUseCaseData(data: FileReference) = FileReferenceResponseTestSchema(
                data.name,
                data.hash,
                encodeLocalDateTime(data.createdAt),
            )
        }
    }

    data class MetadataSectionResponseTestSchema private constructor(val name: MetadataSectionName, val order: Int, val metadata: List<MetadatumResponseTestSchema>, val sections: List<MetadataSectionResponseTestSchema>?) {
        companion object {
            fun fromUseCaseData(metadataSection: MetadataSection): MetadataSectionResponseTestSchema {
                val metadata = metadataSection.metadata.map { MetadatumResponseTestSchema.fromUseCaseData(it) }
                val childSections = metadataSection.sections?.map { fromUseCaseData(it) }
                return MetadataSectionResponseTestSchema(name = metadataSection.name, order = metadataSection.order, metadata = metadata, sections = childSections)
            }
        }
    }

    data class MetadatumResponseTestSchema private constructor(val value: String, val type: String, val order: Int) {
        companion object {
            fun fromUseCaseData(metadatum: Metadatum<*>): MetadatumResponseTestSchema {
                val value: String = metadatum.value as String
                val type = metadatum.type.name
                return MetadatumResponseTestSchema(value = value, type = type, order = metadatum.order)
            }
        }
    }
}
