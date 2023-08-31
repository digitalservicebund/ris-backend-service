package utils.factory

import de.bund.digitalservice.ris.norms.domain.entity.*
import java.util.UUID

fun norm(block: NormBuilder.() -> Unit): Norm = NormBuilder().apply(block).build()

class NormBuilder {
  var guid: UUID = UUID.randomUUID()
  var eGesetzgebung: Boolean = false

  private val metadataSections = mutableListOf<MetadataSection>()
  private var recitals: Recitals? = null
  private var formula: Formula? = null
  private val documentation = mutableListOf<Documentation>()
  private var conclusion: Conclusion? = null
  private val files = mutableListOf<FileReference>()

  fun metadataSections(block: MetadataSections.() -> Unit) =
      metadataSections.addAll(MetadataSections().apply(block))

  fun files(block: Files.() -> Unit) = files.addAll(Files().apply(block))

  fun documentation(block: DocumentationCollection.() -> Unit) =
      documentation.addAll(DocumentationCollection().apply(block))

  fun recitals(block: RecitalsBuilder.() -> Unit) {
    recitals = RecitalsBuilder().apply(block).build()
  }

  fun formula(block: FormulaBuilder.() -> Unit) {
    formula = FormulaBuilder().apply(block).build()
  }

  fun conclusion(block: ConclusionBuilder.() -> Unit) {
    conclusion = ConclusionBuilder().apply(block).build()
  }

  fun build(): Norm =
      Norm(
          guid = guid,
          metadataSections = metadataSections,
          files = files,
          recitals = recitals,
          formula = formula,
          documentation = documentation,
          conclusion = conclusion,
          eGesetzgebung = eGesetzgebung,
      )
}

class MetadataSections : ArrayList<MetadataSection>() {
  fun metadataSection(block: MetadataSectionBuilder.() -> Unit) =
      add(MetadataSectionBuilder().apply(block).build())
}

class Articles : ArrayList<Article>() {
  fun article(block: ArticleBuilder.() -> Unit) = add(ArticleBuilder().apply(block).build())
}

class Files : ArrayList<FileReference>() {
  fun file(block: FileBuilder.() -> Unit) = add(FileBuilder().apply(block).build())
}

class DocumentationCollection : ArrayList<Documentation>() {
  fun documentSection(block: DocumentSectionBuilder.() -> Unit) =
      add(DocumentSectionBuilder().apply(block).build())

  fun article(block: ArticleBuilder.() -> Unit) = add(ArticleBuilder().apply(block).build())
}
