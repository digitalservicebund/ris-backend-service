package utils.factory

import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import java.util.UUID
import utils.randomString

fun article(block: ArticleBuilder.() -> Unit): Article = ArticleBuilder().apply(block).build()

class ArticleBuilder {
  var guid: UUID = UUID.randomUUID()
  var header: String = randomString(100)
  var designation: String = randomString(1)
  var order: Int = 1
  private var paragraphs = mutableListOf<Paragraph>()

  fun paragraphs(block: Paragraphs.() -> Unit) = paragraphs.addAll(Paragraphs().apply(block))

  fun build(): Article =
      Article(
          guid = guid,
          header = header,
          designation = designation,
          order = order,
          paragraphs = paragraphs)
}

class Paragraphs : ArrayList<Paragraph>() {
  fun paragraph(block: ParagraphBuilder.() -> Unit) = add(ParagraphBuilder().apply(block).build())
}
