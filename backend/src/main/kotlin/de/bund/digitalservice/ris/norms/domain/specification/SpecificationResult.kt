package de.bund.digitalservice.ris.norms.domain.specification

/**
 * Represent the result of an evaluated specification for a specific (list of) instance(s). There
 * are two possible result types: a satisfied or unsatisfied result. For flexibility it is possible
 * to determine the satisfaction on a generic level using the property, or inspecting the variance
 * (works exhaustively). Works similar to [kotlin.Result].
 *
 * ```kotlin
 * val result = greaterThanFive.evaluate(6)
 *
 * if (result.isSatisfied) println("satisfied")
 *
 * if (result == Satisfied) println("satisfied")
 *
 * when (result) {
 *   Satisfied -> println("satisfied")
 *   is Unsatisfied -> println("unsatisfied due to ${result.violations}")
 * }
 * ```
 *
 * In case the specification is not satisfied, a list of [SpecificationViolation]s is included. For
 * generic, simple usage, the violation property is always given. For satisfied results this would
 * be empty. Unsatisfied results **must** have some violations.
 */
sealed class SpecificationResult(
    val isSatisfied: Boolean,
    val violations: List<SpecificationViolation>,
) {
  /** Reciprocal of [isSatisfied] for convenience like the standard library does. */
  val isNotSatisfied: Boolean
    get() = !isSatisfied

  /**
   * Shortcut to throw a [SpecificationError] including the violations if the result is
   * [Unsatisfied]. Allows to quickly ensure certain data characteristics that are required. Is a
   * no-operation if it is satisfied and result remains as is.
   *
   * @exception [SpecificationError]
   */
  fun throwWhenUnsatisfied(): SpecificationResult {
    if (isSatisfied) return this else throw SpecificationError(violations)
  }

  /**
   * Merges multiple results of various evaluations of the same or different specifications. The
   * overall result is satisfied if all merged results are satisfied. The violations includes the
   * sum of all merged results. In contrast to [Specification.and], this is used to get a single
   * result when evaluating various specifications for a various set of data (types). See also
   * [ChainedSpecifications] for a more fluid syntax.
   *
   * ```kotlin
   * greaterThanFive.evaluate(6, 10).and(isTrue(false).evaluate())
   * ```
   */
  fun and(other: SpecificationResult): SpecificationResult {
    val isSatisfied = this.isSatisfied && other.isSatisfied
    return if (isSatisfied) Satisfied else Unsatisfied(this.violations + other.violations)
  }

  /**
   * Convenience implementation of the [and] function to use with the infix `+` operator. Becomes
   * handy at certain usages to keep the code easy to read.
   */
  operator fun plus(other: SpecificationResult) = and(other)

  companion object {

    fun from(instance: Any, code: String, message: String, predicate: () -> Boolean) =
        if (predicate()) {
          Satisfied
        } else {
          Unsatisfied(SpecificationViolation(instance, code, message))
        }
  }

  /**
   * Variance of a [SpecificationResult] that represents that a specification is satisfied for an
   * evaluated (list of) instance(s). The list of [violations] is always empty.
   */
  object Satisfied : SpecificationResult(true, emptyList())

  /**
   * Variance of a [SpecificationResult] that represents that a specification is **not** satisfied
   * for an evaluated (list of) instance(s). The list of [violations] is must never be empty
   * (ensured by `init` hook).
   */
  class Unsatisfied(violations: List<SpecificationViolation>) :
      SpecificationResult(false, violations) {

    constructor(vararg violation: SpecificationViolation) : this(violation.asList()) {}

    init {
      require(violations.isNotEmpty()) {
        "Specification can not be unsatisfied but have no validations."
      }
    }
  }
}

/**
 * Represents a cause why an [instance] does not satisfy a certain specification. The usage of
 * [code] and [message] properties support the usage for API communication.
 *
 * As specification results including violations can be merged for specifications with various data
 * types, there is no type information for the instance that caused the violation.
 */
data class SpecificationViolation(
    val instance: Any,
    val code: String,
    val message: String,
)

/**
 * Implements [Error] to easily convert an unsatisfied [SpecificationResult] into a throwable
 * exception that contains all violations.
 *
 * It is not possible to have a [SpecificationError] without any violations.
 */
data class SpecificationError(val violations: List<SpecificationViolation>) :
    Exception("specification is unsatisfied due to:\n\n${violations.joinToString("\n\n")}") {
  init {
    require(violations.isNotEmpty()) { "A specification error must list some violations" }
  }
}
