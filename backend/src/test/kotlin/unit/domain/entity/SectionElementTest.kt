package de.bund.digitalservice.ris.norms.domain.entity

import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SectionElementTest {

  @Test
  fun `can create a book without child sections`() {
    val guid = UUID.randomUUID()
    val book = Book("Book Header", guid, "Book Designation", 1)

    assertThat(book.guid).isEqualTo(guid)
    assertThat(book.header).isEqualTo("Book Header")
    assertThat(book.designation).isEqualTo("Book Designation")
    assertThat(book.order).isEqualTo(1)
    assertThat(book.childSections).isNull()
  }

  @Test
  fun `can create a book with a child chapter`() {
    val guid = UUID.randomUUID()
    val childSections =
        listOf(Chapter("Chapter Header", UUID.randomUUID(), "Chapter Designation", 1))
    val book = Book("Book Header", guid, "Book Designation", 1, childSections)

    assertThat(book.guid).isEqualTo(guid)
    assertThat(book.header).isEqualTo("Book Header")
    assertThat(book.designation).isEqualTo("Book Designation")
    assertThat(book.order).isEqualTo(1)

    assertThat(book.childSections).hasSize(1)
    val childSection = book.childSections?.first()
    assertThat(childSection).isNotNull
    assertThat(childSection).isInstanceOf(Chapter::class.java)
    (childSection as? Chapter)?.let {
      assertThat(it.designation).isEqualTo("Chapter Designation")
      assertThat(it.order).isEqualTo(1)
      assertThat(it.header).isEqualTo("Chapter Header")
    }
  }

  @Test
  fun `can create a part without child sections`() {
    val guid = UUID.randomUUID()
    val part = Part("Part Header", guid, "Part Designation", 2)

    assertThat(part.guid).isEqualTo(guid)
    assertThat(part.header).isEqualTo("Part Header")
    assertThat(part.designation).isEqualTo("Part Designation")
    assertThat(part.order).isEqualTo(2)
    assertThat(part.childSections).isNull()
  }

  @Test
  fun `can create a chapter without child sections`() {
    val guid = UUID.randomUUID()
    val chapter = Chapter("Chapter Header", guid, "Chapter Designation", 3)

    assertThat(chapter.guid).isEqualTo(guid)
    assertThat(chapter.header).isEqualTo("Chapter Header")
    assertThat(chapter.designation).isEqualTo("Chapter Designation")
    assertThat(chapter.order).isEqualTo(3)
    assertThat(chapter.childSections).isNull()
  }

  @Test
  fun `can create a subchapter without child sections`() {
    val guid = UUID.randomUUID()
    val subchapter = Subchapter("Subchapter Header", guid, "Subchapter Designation", 4)

    assertThat(subchapter.guid).isEqualTo(guid)
    assertThat(subchapter.header).isEqualTo("Subchapter Header")
    assertThat(subchapter.designation).isEqualTo("Subchapter Designation")
    assertThat(subchapter.order).isEqualTo(4)
    assertThat(subchapter.childSections).isNull()
  }

  @Test
  fun `can create a section without child sections`() {
    val guid = UUID.randomUUID()
    val section = Section("Section Header", guid, "Section Designation", 5)

    assertThat(section.guid).isEqualTo(guid)
    assertThat(section.header).isEqualTo("Section Header")
    assertThat(section.designation).isEqualTo("Section Designation")
    assertThat(section.order).isEqualTo(5)
    assertThat(section.childSections).isNull()
  }

  @Test
  fun `can create a subsection without child sections`() {
    val guid = UUID.randomUUID()
    val subsection = Subsection("Subsection Header", guid, "Subsection Designation", 3)

    assertThat(subsection.header).isEqualTo("Subsection Header")
    assertThat(subsection.guid).isEqualTo(guid)
    assertThat(subsection.designation).isEqualTo("Subsection Designation")
    assertThat(subsection.order).isEqualTo(3)
    assertThat(subsection.childSections).isNull()
  }

  @Test
  fun `can create a title without child sections`() {
    val guid = UUID.randomUUID()
    val title = Title("Title Header", guid, "Title Designation", 7)

    assertThat(title.guid).isEqualTo(guid)
    assertThat(title.header).isEqualTo("Title Header")
    assertThat(title.designation).isEqualTo("Title Designation")
    assertThat(title.order).isEqualTo(7)
    assertThat(title.childSections).isNull()
  }

  @Test
  fun `can create a title with a child subtitle`() {
    val guid = UUID.randomUUID()
    val childSections =
        listOf(Subtitle("Subtitle Header", UUID.randomUUID(), "Subtitle Designation", 1))
    val title = Title("Title Header", guid, "Title Designation", 1, childSections)

    assertThat(title.guid).isEqualTo(guid)
    assertThat(title.header).isEqualTo("Title Header")
    assertThat(title.designation).isEqualTo("Title Designation")
    assertThat(title.order).isEqualTo(1)

    assertThat(title.childSections).hasSize(1)
    val childSection = title.childSections?.first()
    assertThat(childSection).isNotNull
    assertThat(childSection).isInstanceOf(Subtitle::class.java)
    (childSection as? Subtitle)?.let {
      assertThat(it.designation).isEqualTo("Subtitle Designation")
      assertThat(it.order).isEqualTo(1)
      assertThat(it.header).isEqualTo("Subtitle Header")
    }
  }

  @Test
  fun `can create a subtitle without child sections`() {
    val guid = UUID.randomUUID()
    val subtitle = Subtitle("Subtitle Header", guid, "Subtitle Designation", 8)

    assertThat(subtitle.guid).isEqualTo(guid)
    assertThat(subtitle.header).isEqualTo("Subtitle Header")
    assertThat(subtitle.designation).isEqualTo("Subtitle Designation")
    assertThat(subtitle.order).isEqualTo(8)
    assertThat(subtitle.childSections).isNull()
  }

  @Test
  fun `can create an uncategorized without child sections`() {
    val guid = UUID.randomUUID()
    val subtitle = Subtitle("Uncategorized Header", guid, "Uncategorized Designation", 9)

    assertThat(subtitle.guid).isEqualTo(guid)
    assertThat(subtitle.header).isEqualTo("Uncategorized Header")
    assertThat(subtitle.designation).isEqualTo("Uncategorized Designation")
    assertThat(subtitle.order).isEqualTo(9)
    assertThat(subtitle.childSections).isNull()
  }

  @Test
  fun `can create an article without paragraphs`() {
    val guid = UUID.randomUUID()
    val article =
        Article("Article Header", guid, "Article Designation", 10, paragraphs = emptyList())

    assertThat(article.guid).isEqualTo(guid)
    assertThat(article.header).isEqualTo("Article Header")
    assertThat(article.designation).isEqualTo("Article Designation")
    assertThat(article.order).isEqualTo(10)
    assertThat(article.paragraphs).isEmpty()
  }

  @Test
  fun `can create an article with two paragraph`() {
    val firstParagraphGuid = UUID.randomUUID()
    val secondParagraphGuid = UUID.randomUUID()
    val articleGuid = UUID.randomUUID()
    val article =
        Article(
            "Article Header",
            articleGuid,
            "Article Designation",
            11,
            paragraphs =
                listOf(
                    Paragraph(firstParagraphGuid, 1, "A", "First Paragraph Text"),
                    Paragraph(secondParagraphGuid, 2, "B", "Second Paragraph Text")))

    assertThat(article.guid).isEqualTo(articleGuid)
    assertThat(article.header).isEqualTo("Article Header")
    assertThat(article.designation).isEqualTo("Article Designation")
    assertThat(article.order).isEqualTo(11)
    assertThat(article.paragraphs).hasSize(2)

    val firstParagraph = article.paragraphs.first()
    assertThat(firstParagraph).isInstanceOf(Paragraph::class.java)
    (firstParagraph as? Paragraph)?.let {
      assertThat(it.guid).isEqualTo(firstParagraphGuid)
      assertThat(it.marker).isEqualTo("A")
      assertThat(it.order).isEqualTo(1)
      assertThat(it.text).isEqualTo("First Paragraph Text")
    }

    val secondParagraph = article.paragraphs.last()
    assertThat(secondParagraph).isInstanceOf(Paragraph::class.java)
    (secondParagraph as? Paragraph)?.let {
      assertThat(it.guid).isEqualTo(secondParagraphGuid)
      assertThat(it.marker).isEqualTo("B")
      assertThat(it.order).isEqualTo(2)
      assertThat(it.text).isEqualTo("Second Paragraph Text")
    }
  }
}
