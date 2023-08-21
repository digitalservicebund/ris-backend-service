import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Book
import de.bund.digitalservice.ris.norms.domain.entity.Chapter
import de.bund.digitalservice.ris.norms.domain.entity.ContentElement
import de.bund.digitalservice.ris.norms.domain.entity.Part
import de.bund.digitalservice.ris.norms.domain.entity.Section
import de.bund.digitalservice.ris.norms.domain.entity.SectionElement
import de.bund.digitalservice.ris.norms.domain.entity.Subchapter
import de.bund.digitalservice.ris.norms.domain.entity.Subsection
import de.bund.digitalservice.ris.norms.domain.entity.Subtitle
import de.bund.digitalservice.ris.norms.domain.entity.Title
import de.bund.digitalservice.ris.norms.domain.entity.Uncategorized
import java.util.*
import utils.factory.Sections

class SectionElementBuilder<T : SectionElement>(private val clazz: Class<T>) {
  var header: String? = null
  var guid: UUID = UUID.randomUUID()
  var designation: String = ""
  var order: Int = 1
  private val childSections = mutableListOf<SectionElement>()
  var paragraphs: Collection<ContentElement> = emptyList()

  fun childSections(block: Sections.() -> Unit) = childSections.addAll(Sections().apply(block))

  fun build(): SectionElement {
    return when (clazz) {
      Book::class.java -> Book(header!!, guid, designation, order, childSections)
      Part::class.java -> Part(header!!, guid, designation, order, childSections)
      Chapter::class.java -> Chapter(header!!, guid, designation, order, childSections)
      Subchapter::class.java -> Subchapter(header!!, guid, designation, order, childSections)
      Section::class.java -> Section(header!!, guid, designation, order, childSections)
      Article::class.java -> Article(header, guid, designation, order, paragraphs)
      Subsection::class.java -> Subsection(header!!, guid, designation, order, childSections)
      Title::class.java -> Title(header!!, guid, designation, order, childSections)
      Subtitle::class.java -> Subtitle(header!!, guid, designation, order, childSections)
      Uncategorized::class.java -> Uncategorized(header!!, guid, designation, order, childSections)
      else -> throw IllegalArgumentException("Unsupported class: ${clazz.simpleName}")
    }
  }
}
