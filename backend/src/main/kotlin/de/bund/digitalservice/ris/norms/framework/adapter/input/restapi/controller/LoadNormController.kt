package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.controller

import de.bund.digitalservice.ris.OpenApiConfiguration
import de.bund.digitalservice.ris.exceptions.exception.NotFoundWithInstanceException
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
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.ApiConfiguration
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeEli
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeGuid
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.encodeLocalDateTime
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import java.net.URI
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping(ApiConfiguration.API_NORMS_PATH)
@Tag(name = OpenApiConfiguration.NORMS_TAG)
class LoadNormController(private val loadNormService: LoadNormUseCase) {

  @GetMapping(path = ["/{guid}"])
  @Operation(summary = "Load a single norm", description = "Retrieves a single norm given its guid")
  @ApiResponses(
      ApiResponse(responseCode = "200", description = "Norm was found"),
      ApiResponse(responseCode = "404", description = "No norm found for this query"),
  )
  fun getNormByGuid(
      @Parameter(
          name = "guid", description = "the unique guid identifier of a norm", required = true)
      @PathVariable
      guid: String
  ): Mono<ResponseEntity<NormResponseSchema>> {
    val query = LoadNormUseCase.Query(UUID.fromString(guid))
    return loadNormService
        .loadNorm(query)
        .map { norm -> NormResponseSchema.fromUseCaseData(norm) }
        .map { normResponseSchema -> ResponseEntity.ok(normResponseSchema) }
        .switchIfEmpty(Mono.error(NotFoundWithInstanceException(URI(guid))))
  }

  data class NormResponseSchema
  internal constructor(
      val guid: String,
      val metadataSections: List<MetadataSectionResponseSchema>,
      var eli: String,
      var files: List<FileReferenceResponseSchema>,
      val sections: List<SectionResponseSchema>,
      val contents: List<ContentResponseSchema>,
  ) {
    companion object {
      fun fromUseCaseData(data: Norm): NormResponseSchema {
        val sections =
            data.sections.sortedBy { it.order }.map(SectionResponseSchema::fromUseCaseData)
        val contents =
            data.contents.sortedBy { it.order }.map(ContentResponseSchema::fromUseCaseData)
        val files = data.files.map(FileReferenceResponseSchema::fromUseCaseData)
        val metadataSections =
            data.metadataSections.map(MetadataSectionResponseSchema::fromUseCaseData)
        return NormResponseSchema(
            encodeGuid(data.guid),
            metadataSections,
            encodeEli(data.eli),
            files = files,
            sections,
            contents)
      }
    }
  }

  data class SectionResponseSchema
  internal constructor(
      val guid: String,
      var header: String? = null,
      val designation: String,
      val sections: List<SectionResponseSchema>? = emptyList(),
      val paragraphs: List<ParagraphResponseSchema>? = emptyList()
  ) {
    companion object {
      fun fromUseCaseData(data: SectionElement): SectionResponseSchema {
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
              SectionResponseSchema(
                  encodeGuid(data.guid),
                  data.header,
                  data.designation,
                  sections = data.childSections?.map { fromUseCaseData(it) })
          is Article -> {
            val paragraphs =
                data.paragraphs
                    .sortedBy { it.order }
                    .map { ParagraphResponseSchema.fromUseCaseData(it as Paragraph) }
            SectionResponseSchema(
                encodeGuid(data.guid), data.header, data.designation, paragraphs = paragraphs)
          }
        }
      }
    }
  }

  data class ContentResponseSchema
  internal constructor(
      val guid: String,
      var order: Int,
      val marker: String? = null,
      val text: String,
  ) {
    companion object {
      fun fromUseCaseData(data: ContentElement): ContentResponseSchema =
          ContentResponseSchema(encodeGuid(data.guid), data.order, data.marker, data.text)
    }
  }

  data class ParagraphResponseSchema
  internal constructor(val guid: String, val marker: String? = null, val text: String) {
    companion object {
      fun fromUseCaseData(data: Paragraph): ParagraphResponseSchema {
        return ParagraphResponseSchema(encodeGuid(data.guid), data.marker, data.text)
      }
    }
  }

  data class FileReferenceResponseSchema
  internal constructor(
      val guid: String,
      val name: String,
      val hash: String,
      val createdAt: String
  ) {
    companion object {
      fun fromUseCaseData(data: FileReference) =
          FileReferenceResponseSchema(
              encodeGuid(data.guid),
              data.name,
              data.hash,
              encodeLocalDateTime(data.createdAt),
          )
    }
  }

  data class MetadataSectionResponseSchema
  internal constructor(
      val guid: String,
      val name: MetadataSectionName,
      val order: Int,
      val metadata: List<MetadatumResponseSchema>,
      val sections: List<MetadataSectionResponseSchema>?
  ) {
    companion object {
      fun fromUseCaseData(metadataSection: MetadataSection): MetadataSectionResponseSchema {
        val metadata = metadataSection.metadata.map { MetadatumResponseSchema.fromUseCaseData(it) }
        val childSections = metadataSection.sections?.map { fromUseCaseData(it) }
        return MetadataSectionResponseSchema(
            guid = encodeGuid(metadataSection.guid),
            name = metadataSection.name,
            order = metadataSection.order,
            metadata = metadata,
            sections = childSections)
      }
    }
  }

  data class MetadatumResponseSchema
  internal constructor(val guid: String, val value: String, val type: String, val order: Int) {
    companion object {
      fun fromUseCaseData(metadatum: Metadatum<*>): MetadatumResponseSchema {
        val value: String = metadatum.value.toString()
        val type = metadatum.type.name
        return MetadatumResponseSchema(
            guid = encodeGuid(metadatum.guid), value = value, type = type, order = metadatum.order)
      }
    }
  }
}
