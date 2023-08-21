package de.bund.digitalservice.ris.norms.domain.specification.norm

import de.bund.digitalservice.ris.norms.domain.entity.Article
import de.bund.digitalservice.ris.norms.domain.entity.Book
import de.bund.digitalservice.ris.norms.domain.entity.Chapter
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.Part
import de.bund.digitalservice.ris.norms.domain.entity.Section
import de.bund.digitalservice.ris.norms.domain.entity.SectionElement
import de.bund.digitalservice.ris.norms.domain.entity.Subchapter
import de.bund.digitalservice.ris.norms.domain.entity.Subsection
import de.bund.digitalservice.ris.norms.domain.entity.Subtitle
import de.bund.digitalservice.ris.norms.domain.entity.Title
import de.bund.digitalservice.ris.norms.domain.specification.Specification
import de.bund.digitalservice.ris.norms.domain.specification.SpecificationResult

const val MAX_ALLOWED_DEPTH = 9

val hasValidSectionElementsDepth =
    object : Specification<Norm> {
      override fun evaluate(instance: Norm): SpecificationResult {

        val maxDepthReached = normHasSectionsOverMaxAllowDepth(instance.sections)

        return SpecificationResult.from(
            instance,
            "MAXIMUM_DEPTH_OF_9_IN_SECTION_ELEMENTS",
            "the norm contains a depth in section elements bigger than the maximum depth allowed of '${MAX_ALLOWED_DEPTH}") {
              !maxDepthReached
            }
      }
    }

/** Depth-first approach and depth-count stopping if MAX_ALLOW_DEPTH + 1 reached */
private fun normHasSectionsOverMaxAllowDepth(sections: List<SectionElement>): Boolean {
  var maxDepthReached = false
  sections.forEach { section ->
    if (!maxDepthReached) {
      val depth = isVerticalDepthBiggerThan(MAX_ALLOWED_DEPTH, section)
      if (depth > 9) {
        maxDepthReached = true
        return@forEach
      }
    }
  }
  return maxDepthReached
}

private fun isVerticalDepthBiggerThan(
    maxAllowDepth: Int,
    section: SectionElement,
    currentDepth: Int = 1
): Int {

  if (currentDepth > maxAllowDepth) {
    return currentDepth
  }

  var maxDepth = currentDepth

  if (section !is Article) {
    val childSections =
        when (section) {
          is Book,
          is Part,
          is Chapter,
          is Subchapter,
          is Section,
          is Subsection,
          is Title,
          is Subtitle -> section.childSections
          else -> null
        }

    childSections?.forEach { childSection ->
      val childDepth = isVerticalDepthBiggerThan(maxAllowDepth, childSection, currentDepth + 1)
      maxDepth = maxOf(maxDepth, childDepth)
    }
  }

  return maxDepth
}
