package utils.factory

import de.bund.digitalservice.ris.norms.domain.entity.DocumentSection
import de.bund.digitalservice.ris.norms.domain.entity.Documentation
import de.bund.digitalservice.ris.norms.domain.value.DocumentSectionType

class DocumentSectionBuilder : DocumentationBuilder() {
  var type: DocumentSectionType = DocumentSectionType.BOOK
  private var documentation = mutableListOf<Documentation>()

  fun documentation(block: DocumentationCollection.() -> Unit) =
      documentation.addAll(DocumentationCollection().apply(block))

  override fun build() =
      DocumentSection(
          guid = guid,
          order = order,
          type = type,
          marker = marker,
          heading = heading,
          documentation = documentation,
      )
}
