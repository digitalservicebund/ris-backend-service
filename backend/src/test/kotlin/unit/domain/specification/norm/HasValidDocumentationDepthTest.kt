package de.bund.digitalservice.ris.norms.domain.specification.norm

import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.DocumentSection
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.value.DocumentSectionType
import io.mockk.every
import io.mockk.mockk
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class HasValidSectionElementsDepthTest {

  @Test
  fun `it is satisfied if the norm contains a documentation depth of max 9 including an article at the end`() {
    val instance = mockk<Norm>()
    every { instance.guid } returns UUID.randomUUID()

    val nestedStructure = createNestedStructureWithArticleAtTheEnd(9)

    every { instance.documentation } returns listOf(nestedStructure)

    assertThat(hasValidDocumentationDepth.evaluate(instance).isSatisfied).isTrue()
  }

  @Test
  fun `it is not satisfied if the norm contains a documentation depth bigger than 9`() {
    val instance = mockk<Norm>()
    every { instance.guid } returns UUID.randomUUID()

    val nestedStructure = createNestedStructureWithArticleAtTheEnd(10)

    every { instance.documentation } returns listOf(nestedStructure)

    assertThat(hasValidDocumentationDepth.evaluate(instance).isSatisfied).isFalse()
  }

  private fun createNestedStructureWithArticleAtTheEnd(depth: Int): DocumentSection {
    val article =
        Article(guid = UUID.randomUUID(), order = 1, marker = "Marker 1", heading = "Heading 1")
    var currentSection =
        DocumentSection(
            guid = UUID.randomUUID(),
            order = 1,
            type = DocumentSectionType.SECTION,
            marker = "Marker 2",
            heading = "Heading 2",
            documentation = listOf(article))
    repeat(depth - 2) {
      val nestedSection =
          DocumentSection(
              guid = UUID.randomUUID(),
              order = 1,
              type = DocumentSectionType.SECTION,
              marker = "Marker " + (it + 3),
              heading = "Heading " + (it + 3),
              documentation = listOf(currentSection))
      currentSection = nestedSection
    }
    return currentSection
  }
}
