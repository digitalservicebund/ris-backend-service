package de.bund.digitalservice.ris.norms.domain.specification.norm

import de.bund.digitalservice.ris.norms.domain.entity.DocumentSection
import de.bund.digitalservice.ris.norms.domain.entity.Documentation
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.specification.Specification
import de.bund.digitalservice.ris.norms.domain.specification.SpecificationResult

const val MAX_ALLOWED_DEPTH = 9

val hasValidDocumentationDepth =
    object : Specification<Norm> {
      override fun evaluate(instance: Norm): SpecificationResult {

        val maxDepthReached = normHasSectionsOverMaxAllowDepth(instance.documentation)

        return SpecificationResult.from(
            instance,
            "MAXIMUM_DEPTH_OF_9_IN_DOCUMENTATION",
            "the norm contains a depth in documentation bigger than the maximum depth allowed of '${MAX_ALLOWED_DEPTH}") {
              !maxDepthReached
            }
      }
    }

/** Depth-first approach and depth-count stopping if MAX_ALLOW_DEPTH + 1 reached */
private fun normHasSectionsOverMaxAllowDepth(
    documentationList: Collection<Documentation>
): Boolean {
  var maxDepthReached = false
  documentationList.forEach { documentation ->
    if (!maxDepthReached) {
      val depth = isVerticalDepthBiggerThan(MAX_ALLOWED_DEPTH, documentation)
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
    documentation: Documentation,
    currentDepth: Int = 1
): Int {

  if (currentDepth > maxAllowDepth) {
    return currentDepth
  }

  var maxDepth = currentDepth

  if (documentation is DocumentSection) {
    documentation.documentation.forEach { childSection ->
      val childDepth = isVerticalDepthBiggerThan(maxAllowDepth, childSection, currentDepth + 1)
      maxDepth = maxOf(maxDepth, childDepth)
    }
  }

  return maxDepth
}
