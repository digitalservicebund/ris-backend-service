package utils.factory

import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph

fun article(block: ArticleBuilder.() -> Unit): Article = ArticleBuilder().apply(block).build()

class ArticleBuilder : DocumentationBuilder() {
  private var paragraphs = mutableListOf<Paragraph>()

  fun paragraphs(block: Paragraphs.() -> Unit) = paragraphs.addAll(Paragraphs().apply(block))

  override fun build() =
      Article(
          guid = guid,
          order = order,
          paragraphs = paragraphs,
          marker = marker,
          heading = heading,
      )
}

class Paragraphs : ArrayList<Paragraph>() {
  fun paragraph(block: ParagraphBuilder.() -> Unit) = add(ParagraphBuilder().apply(block).build())
}
