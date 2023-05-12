package utils.factory

import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import utils.randomString
import java.util.UUID

fun article(block: ArticleBuilder.() -> Unit): Article = ArticleBuilder().apply(block).build()

class ArticleBuilder {
    var guid: UUID = UUID.randomUUID()
    var title: String = randomString(100)
    var marker: String = randomString(1)
    private var paragraphs = mutableListOf<Paragraph>()

    fun paragraphs(block: Paragraphs.() -> Unit) = paragraphs.addAll(Paragraphs().apply(block))

    fun build(): Article = Article(guid, title, marker, paragraphs)
}

class Paragraphs : ArrayList<Paragraph>() {
    fun paragraph(block: ParagraphBuilder.() -> Unit) = add(ParagraphBuilder().apply(block).build())
}
