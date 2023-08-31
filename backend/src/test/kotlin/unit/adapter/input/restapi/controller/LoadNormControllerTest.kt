package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.norms.application.port.input.LoadNormUseCase
import de.bund.digitalservice.ris.norms.domain.entity.*
import de.bund.digitalservice.ris.norms.domain.value.MetadataSectionName
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeEli
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeGuid
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeLocalDateTime
import io.mockk.every
import io.mockk.slot
import io.mockk.verify
import java.util.*
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
import utils.createSimpleDocumentation
import utils.createSimpleMetadataSections

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
    val norm =
        createRandomNorm()
            .copy(
                metadataSections = createSimpleMetadataSections(),
                documentation = createSimpleDocumentation(),
            )
    val responseJson =
        convertLoadNormResponseTestSchemaToJson(NormResponseTestSchema.fromUseCaseData(norm))

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
  fun `it sends a not found response with an json error list if the load norm service responds with empty`() {
    every { loadNormService.loadNorm(any()) } returns Mono.empty()

    webClient
        .mutateWith(csrf())
        .get()
        .uri("/api/v1/norms/72631e54-78a4-11d0-bcf7-00aa00b7b32a")
        .exchange()
        .expectStatus()
        .isNotFound()
        .expectBody()
        .json(
            """
                {
                  "errors" : [
                      {
                        "code" : "NOT_FOUND",
                        "instance" : "72631e54-78a4-11d0-bcf7-00aa00b7b32a",
                        "message": ""
                      }
                  ]
                }
                """
                .trimIndent(),
        )
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
  internal constructor(
      val guid: String,
      val metadataSections: Collection<MetadataSectionResponseTestSchema>,
      var eli: String,
      var files: Collection<FileReferenceResponseTestSchema>,
      val recitals: RecitalsResponseTestSchema?,
      val formula: FormulaResponseTestSchema?,
      val documentation: Collection<DocumentationResponseTestSchema>,
      val conclusion: ConclusionResponseTestSchema?,
  ) {
    companion object {
      fun fromUseCaseData(data: Norm): NormResponseTestSchema {
        val documentation = data.documentation.map(DocumentationResponseTestSchema::fromUseCaseData)
        val files = data.files.map(FileReferenceResponseTestSchema::fromUseCaseData)
        val metadataSections =
            data.metadataSections.map(MetadataSectionResponseTestSchema::fromUseCaseData)

        return NormResponseTestSchema(
            guid = encodeGuid(data.guid),
            metadataSections = metadataSections,
            eli = encodeEli(data.eli),
            files = files,
            recitals = data.recitals?.let(RecitalsResponseTestSchema::fromUseCaseData),
            formula = data.formula?.let(FormulaResponseTestSchema::fromUseCaseData),
            documentation = documentation,
            conclusion = data.conclusion?.let(ConclusionResponseTestSchema::fromUseCaseData),
        )
      }
    }
  }

  abstract interface DocumentationResponseTestSchema {
    val guid: String
    val order: Int
    val marker: String
    val heading: String?

    companion object {
      fun fromUseCaseData(data: Documentation): DocumentationResponseTestSchema {
        return when (data) {
          is DocumentSection -> DocumentSectionResponseTestSchema.fromUseCaseData(data)
          is Article -> ArticleResponseTestSchema.fromUseCaseData(data)
        }
      }
    }
  }

  data class DocumentSectionResponseTestSchema
  internal constructor(
      override val guid: String,
      override val order: Int,
      override val marker: String,
      override val heading: String?,
      val type: String,
      val documentation: Collection<DocumentationResponseTestSchema>,
  ) : DocumentationResponseTestSchema {
    companion object {
      fun fromUseCaseData(data: DocumentSection) =
          DocumentSectionResponseTestSchema(
              guid = encodeGuid(data.guid),
              order = data.order,
              marker = data.marker,
              heading = data.heading,
              type = data.type.toString(),
              documentation =
                  data.documentation.map(DocumentationResponseTestSchema::fromUseCaseData))
    }
  }

  data class ArticleResponseTestSchema
  internal constructor(
      override val guid: String,
      override val order: Int,
      override val marker: String,
      override val heading: String?,
      val paragraphs: Collection<ParagraphResponseSchema>,
  ) : DocumentationResponseTestSchema {
    companion object {
      fun fromUseCaseData(data: Article) =
          ArticleResponseTestSchema(
              guid = encodeGuid(data.guid),
              order = data.order,
              marker = data.marker,
              heading = data.heading,
              paragraphs = data.paragraphs.map(ParagraphResponseSchema::fromUseCaseData),
          )
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

  data class RecitalsResponseTestSchema
  private constructor(
      val guid: String,
      val marker: String?,
      val heading: String?,
      val text: String
  ) {
    companion object {
      fun fromUseCaseData(data: Recitals): RecitalsResponseTestSchema {
        return RecitalsResponseTestSchema(
            encodeGuid(data.guid), data.marker, data.heading, data.text)
      }
    }
  }

  data class FormulaResponseTestSchema private constructor(val guid: String, val text: String) {
    companion object {
      fun fromUseCaseData(data: Formula): FormulaResponseTestSchema {
        return FormulaResponseTestSchema(encodeGuid(data.guid), data.text)
      }
    }
  }

  data class ConclusionResponseTestSchema private constructor(val guid: String, val text: String) {
    companion object {
      fun fromUseCaseData(data: Conclusion): ConclusionResponseTestSchema {
        return ConclusionResponseTestSchema(encodeGuid(data.guid), data.text)
      }
    }
  }

  data class FileReferenceResponseTestSchema
  internal constructor(
      val guid: String,
      val name: String,
      val hash: String,
      val createdAt: String
  ) {
    companion object {
      fun fromUseCaseData(data: FileReference) =
          FileReferenceResponseTestSchema(
              encodeGuid(data.guid),
              data.name,
              data.hash,
              encodeLocalDateTime(data.createdAt),
          )
    }
  }

  data class MetadataSectionResponseTestSchema
  internal constructor(
      val guid: String,
      val name: MetadataSectionName,
      val order: Int,
      val metadata: List<MetadatumResponseTestSchema>,
      val sections: List<MetadataSectionResponseTestSchema>?
  ) {
    companion object {
      fun fromUseCaseData(metadataSection: MetadataSection): MetadataSectionResponseTestSchema {
        val metadata =
            metadataSection.metadata.map { MetadatumResponseTestSchema.fromUseCaseData(it) }
        val childSections = metadataSection.sections?.map { fromUseCaseData(it) }
        return MetadataSectionResponseTestSchema(
            guid = encodeGuid(metadataSection.guid),
            name = metadataSection.name,
            order = metadataSection.order,
            metadata = metadata,
            sections = childSections)
      }
    }
  }

  data class MetadatumResponseTestSchema
  internal constructor(val guid: String, val value: String, val type: String, val order: Int) {
    companion object {
      fun fromUseCaseData(metadatum: Metadatum<*>): MetadatumResponseTestSchema {
        val value: String = metadatum.value.toString()
        val type = metadatum.type.name
        return MetadatumResponseTestSchema(
            guid = encodeGuid(metadatum.guid), value = value, type = type, order = metadatum.order)
      }
    }
  }
}
