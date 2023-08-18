package utils.factory

import ContentElementBuilder
import SectionElementBuilder
import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Book
import de.bund.digitalservice.ris.norms.domain.entity.Chapter
import de.bund.digitalservice.ris.norms.domain.entity.Closing
import de.bund.digitalservice.ris.norms.domain.entity.ContentElement
import de.bund.digitalservice.ris.norms.domain.entity.FileReference
import de.bund.digitalservice.ris.norms.domain.entity.MetadataSection
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Paragraph
import de.bund.digitalservice.ris.norms.domain.entity.Part
import de.bund.digitalservice.ris.norms.domain.entity.Preamble
import de.bund.digitalservice.ris.norms.domain.entity.Section
import de.bund.digitalservice.ris.norms.domain.entity.SectionElement
import de.bund.digitalservice.ris.norms.domain.entity.Subchapter
import de.bund.digitalservice.ris.norms.domain.entity.Subsection
import de.bund.digitalservice.ris.norms.domain.entity.Subtitle
import de.bund.digitalservice.ris.norms.domain.entity.Title
import java.util.UUID

fun norm(block: NormBuilder.() -> Unit): Norm = NormBuilder().apply(block).build()

class NormBuilder {
  var guid: UUID = UUID.randomUUID()

  private val metadataSections = mutableListOf<MetadataSection>()
  private val sections = mutableListOf<SectionElement>()
  private val contents = mutableListOf<ContentElement>()
  private val files = mutableListOf<FileReference>()

  fun metadataSections(block: MetadataSections.() -> Unit) =
      metadataSections.addAll(MetadataSections().apply(block))

  fun articles(block: Articles.() -> Unit) = sections.addAll(Articles().apply(block))

  fun files(block: Files.() -> Unit) = files.addAll(Files().apply(block))

  fun sections(block: Sections.() -> Unit) = sections.addAll(Sections().apply(block))

  fun contents(block: Contents.() -> Unit) = contents.addAll(Contents().apply(block))

  fun build(): Norm =
      Norm(
          guid = guid,
          metadataSections = metadataSections,
          files = files,
          sections = sections,
          contents = contents)
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

class Sections : ArrayList<SectionElement>() {
  fun book(block: SectionElementBuilder<Book>.() -> Unit) =
      add(SectionElementBuilder(Book::class.java).apply(block).build())

  fun part(block: SectionElementBuilder<Part>.() -> Unit) =
      add(SectionElementBuilder(Part::class.java).apply(block).build())

  fun chapter(block: SectionElementBuilder<Chapter>.() -> Unit) =
      add(SectionElementBuilder(Chapter::class.java).apply(block).build())

  fun subchapter(block: SectionElementBuilder<Subchapter>.() -> Unit) =
      add(SectionElementBuilder(Subchapter::class.java).apply(block).build())

  fun section(block: SectionElementBuilder<Section>.() -> Unit) =
      add(SectionElementBuilder(Section::class.java).apply(block).build())

  fun article(block: SectionElementBuilder<Article>.() -> Unit) =
      add(SectionElementBuilder(Article::class.java).apply(block).build())

  fun subsection(block: SectionElementBuilder<Subsection>.() -> Unit) =
      add(SectionElementBuilder(Subsection::class.java).apply(block).build())

  fun title(block: SectionElementBuilder<Title>.() -> Unit) =
      add(SectionElementBuilder(Title::class.java).apply(block).build())

  fun subtitle(block: SectionElementBuilder<Subtitle>.() -> Unit) =
      add(SectionElementBuilder(Subtitle::class.java).apply(block).build())
}

class Contents : ArrayList<ContentElement>() {
  fun preamble(block: ContentElementBuilder<Preamble>.() -> Unit) =
      add(ContentElementBuilder(Preamble::class.java).apply(block).build())

  fun paragraph(block: ContentElementBuilder<Paragraph>.() -> Unit) =
      add(ContentElementBuilder(Paragraph::class.java).apply(block).build())

  fun closing(block: ContentElementBuilder<Closing>.() -> Unit) =
      add(ContentElementBuilder(Closing::class.java).apply(block).build())
}
