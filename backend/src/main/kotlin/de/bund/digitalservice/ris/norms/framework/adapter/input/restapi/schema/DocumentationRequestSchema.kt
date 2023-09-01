package de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.schema

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.DocumentSection
import de.bund.digitalservice.ris.norms.domain.entity.Documentation
import de.bund.digitalservice.ris.norms.domain.value.DocumentSectionType
import java.util.UUID

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes(
    JsonSubTypes.Type(DocumentSectionRequestSchema::class),
    JsonSubTypes.Type(ArticleRequestSchema::class))
sealed interface DocumentationRequestSchema {
  val order: Int
  val marker: String
  val heading: String?

  fun toUseCaseData(): Documentation
}

class DocumentSectionRequestSchema : DocumentationRequestSchema {
  override var order = 1
  lateinit override var marker: String
  lateinit override var heading: String
  lateinit var type: DocumentSectionType
  var documentation: Collection<DocumentationRequestSchema> = emptyList()

  override fun toUseCaseData() =
      DocumentSection(
          guid = UUID.randomUUID(),
          order = order,
          marker = marker,
          heading = heading,
          type = type,
          documentation = documentation.map(DocumentationRequestSchema::toUseCaseData))
}

class ArticleRequestSchema : DocumentationRequestSchema {
  override var order = 1
  lateinit override var marker: String
  override var heading: String? = null
  var paragraphs: Collection<ParagraphRequestSchema> = emptyList()

  override fun toUseCaseData() =
      Article(
          guid = UUID.randomUUID(),
          order = order,
          marker = marker,
          heading = heading,
          paragraphs = paragraphs.map(ParagraphRequestSchema::toUseCaseData),
      )
}
