package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.ris.norms.application.port.input.LoadNormUseCase
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Book
import de.bund.digitalservice.ris.norms.domain.entity.Chapter
import de.bund.digitalservice.ris.norms.domain.entity.ContentElement
import de.bund.digitalservice.ris.norms.domain.entity.FileReference
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Metadatum
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.domain.entity.Part
import de.bund.digitalservice.ris.norms.domain.entity.Section
import de.bund.digitalservice.ris.norms.domain.entity.SectionElement
import de.bund.digitalservice.ris.norms.domain.entity.Subchapter
import de.bund.digitalservice.ris.norms.domain.entity.Subsection
import de.bund.digitalservice.ris.norms.domain.entity.Subtitle
import de.bund.digitalservice.ris.norms.domain.entity.Title
import de.bund.digitalservice.ris.norms.domain.entity.Uncategorized
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
import utils.createContentElements
import utils.createRandomNorm
import utils.createSimpleMetadatasections
import utils.createSimpleSections

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
                metadataSections = createSimpleMetadatasections(),
                sections = createSimpleSections(),
                contents = createContentElements(),
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
      val metadataSections: List<MetadataSectionResponseTestSchema>,
      var eli: String,
      var files: List<FileReferenceResponseTestSchema>,
      val sections: List<SectionResponseTestSchema>,
      val contents: List<ContentResponseTestSchema>,
  ) {
    companion object {
      fun fromUseCaseData(data: Norm): NormResponseTestSchema {
        val sections =
            data.sections.sortedBy { it.order }.map(SectionResponseTestSchema::fromUseCaseData)
        val contents =
            data.contents.sortedBy { it.order }.map(ContentResponseTestSchema::fromUseCaseData)
        val files = data.files.map(FileReferenceResponseTestSchema::fromUseCaseData)
        val metadataSections =
            data.metadataSections.map(MetadataSectionResponseTestSchema::fromUseCaseData)
        return NormResponseTestSchema(
            encodeGuid(data.guid),
            metadataSections,
            encodeEli(data.eli),
            files = files,
            sections,
            contents)
      }
    }
  }

  data class SectionResponseTestSchema
  internal constructor(
      val guid: String,
      var header: String? = null,
      val designation: String,
      val sections: List<SectionResponseTestSchema>? = emptyList(),
      val paragraphs: List<ParagraphResponseTestSchema>? = emptyList()
  ) {
    companion object {
      fun fromUseCaseData(data: SectionElement): SectionResponseTestSchema {
        return when (data) {
          is Book,
          is Chapter,
          is Part,
          is Section,
          is Subchapter,
          is Subsection,
          is Subtitle,
          is Title,
          is Uncategorized ->
              SectionResponseTestSchema(
                  encodeGuid(data.guid),
                  data.header,
                  data.designation,
                  sections = data.childSections?.map { fromUseCaseData(it) })
          is Article -> {
            val paragraphs =
                data.paragraphs
                    .sortedBy { it.order }
                    .map { ParagraphResponseTestSchema.fromUseCaseData(it as Paragraph) }
            SectionResponseTestSchema(
                encodeGuid(data.guid), data.header, data.designation, paragraphs = paragraphs)
          }
        }
      }
    }
  }

  data class ContentResponseTestSchema
  internal constructor(
      val guid: String,
      var order: Int,
      val marker: String? = null,
      val text: String,
  ) {
    companion object {
      fun fromUseCaseData(data: ContentElement): ContentResponseTestSchema =
          ContentResponseTestSchema(encodeGuid(data.guid), data.order, data.marker, data.text)
    }
  }

  data class ParagraphResponseTestSchema
  internal constructor(val guid: String, val marker: String? = null, val text: String) {
    companion object {
      fun fromUseCaseData(data: Paragraph): ParagraphResponseTestSchema {
        return ParagraphResponseTestSchema(encodeGuid(data.guid), data.marker, data.text)
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
