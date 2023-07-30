package de.bund.digitalservice.ris.norms.domain.specification

/**
 * Core that represents the specification pattern from domain-driven-design. It is used to specify
 * business rules and characteristics of data. A specification is written for a specific data type
 * and can be evaluated for a (list of) instance(s) of that type. The result can either describe
 * that the instance(s) satisfy the specification or not. In the latter case, a list of violations
 * expresses which characteristics (a) certain instance(s) does not fulfill.
 *
 * ```kotlin
 * assertThat(greaterThanFive.evaluate(6).isSatisfied).isTrue()
 *
 * assertThat(greaterThanFive.evaluate(4, 5).isSatisfied).isFalse()
 * assertThat(greaterThanFive.evaluate(listOf(4, 5)).violations).isNotEmpty()
 * ```
 *
 * Specifications for the same data type can be combined together to create more complex
 * specifications and allow for better re-usability and good complexity management.
 *
 * ```kotlin
 * assertThat(greaterThanFive.and(lessThanSeven).evaluate(6).isSatisfied).isTrue()
 * ```
 *
 * A specification might be configurable if it is implemented as a class with a non empty
 * constructor.
 *
 * ```kotlin
 * assertThat(GreaterThan(5).evaluate(6).isSatisfied).isTrue()
 * ```
 *
 * The specification pattern is traditionally used for multiple use cases like data retrieval,
 * in-memory validation, bulk editing or construction-to-order. So far, this specification
 * implementation only supports the in-memory validation use-case.
 *
 * @sample de.bund.digitalservice.ris.norms.domain.specification.SpecificationSamples.alternativeSpecificationDefinitions
 */
interface Specification<T : Any> {
  /** Core function that must be implemented to evaluate a single instance. */
  fun evaluate(instance: T): SpecificationResult

  /** Convenience overload function to evaluate variable number of instances. */
  fun evaluate(vararg instance: T) = evaluate(instance.asList())

  /** Convenience overload function to evaluate a list of instances. */
  fun evaluate(instances: Collection<T>) =
      instances
          .map { evaluate(it) }
          .fold(SpecificationResult.Satisfied as SpecificationResult) { overallResult, result ->
            overallResult + result
          }

  /**
   * Allows to combine this specification with another one for the same data type. The resolving
   * specification is satisfied if both inner specifications are satisfied. Potential violations of
   * both inner specifications are merged into a single list. Can also be achieved using the [plus]
   * infix operator.
   */
  fun and(other: Specification<T>) = AndSpecification(this, other) as Specification<T>

  /**
   * Convenience implementation of the [and] function to use with the infix `+` operator. Becomes
   * handy at certain usages to keep the code easy to read.
   */
  operator fun plus(other: Specification<T>) = and(other)

  companion object {
    /**
     * Specifications can be implemented using classes or objects. An alternative approach for more
     * simple specifications is to describe it with a predicate plus the violation properties. The
     * returned object is not configurable and is only able to be unsatisfied for a single kind of
     * violation.
     *
     * ```kotlin
     * Specification.fromPredicate("CODE", "message") { instance -> instance > 5 }
     * ```
     */
    fun <T : Any> fromPredicate(
        violationCode: String,
        violationMessage: String,
        predicate: (T) -> Boolean,
    ) =
        object : Specification<T> {
          override fun evaluate(instance: T): SpecificationResult {
            val violation = SpecificationViolation(instance, violationCode, violationMessage)
            return SpecificationResult.from(violation, { predicate(instance) })
          }
        }
  }
}

private class AndSpecification<T : Any>(val left: Specification<T>, val right: Specification<T>) :
    Specification<T> {

  override fun evaluate(instance: T): SpecificationResult {
    val leftResult = left.evaluate(instance)
    val rightResult = right.evaluate(instance)

    return if (leftResult.isSatisfied && rightResult.isSatisfied) {
      SpecificationResult.Satisfied
    } else {
      SpecificationResult.Unsatisfied(leftResult.violations + rightResult.violations)
    }
  }
}
