package de.bund.digitalservice.ris.norms.domain.specification

import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * Helper to create [ChainedSpecifications] with an immediate switch to a
 * [OpenChainedSpecifications] to improve the code readability and the flow.
 *
 * ```kotlin
 * chain(isPositive + greaterThanFive + LessThan(20))
 *     .with(7)
 *     .and(GreaterThan(10) + lessThanSeven)
 *     .with(3, *someNumber.toTypedArray())
 *     .and(isSuperSpecial)
 *     .and(listOfSomething)
 *     .evaluateAll()
 * ```
 */
inline fun <reified T : Any> chain(specification: Specification<T>) =
    ChainedSpecifications().and(specification)

/**
 * Allows to chain multiple specifications and link them with some instance(s). Finally all
 * specifications get evaluated with their linked instances and a single result is produced.
 *
 * For a fluid and type safe syntax, this uses the [OpenChainedSpecifications] for alternation.
 * [ChainedSpecifications] can be either evaluated or extended with another chain link. It makes
 * sure that each chain link have compatible types for the respective specification and instance(s).
 *
 * See [chain] for examples how this would be used with a fluid and clean syntax.
 */
class ChainedSpecifications(
    final val specificationChain: SpecificationChain = emptyList(),
) {
  /**
   * Evaluates all chained specifications with their linked instance(s) and combines the results
   * into one. An empty chain is always satisfied.
   */
  fun evaluateAll() =
      specificationChain
          .map { (specification, instances, type) ->
            specification.evaluate(instances.map { type.cast(it) })
          }
          .fold(SpecificationResult.Satisfied as SpecificationResult) { overallResult, result ->
            overallResult + result
          }

  /**
   * Prepares a new link in the chain with a specification. Alternates to a
   * [OpenChainedSpecifications] that requires to define the instance[s] to link the specification
   * with.
   */
  inline fun <reified T : Any> and(specification: Specification<T>) =
      OpenChainedSpecifications(specificationChain, specification, T::class)
}

/**
 * The complementary for the [ChainedSpecifications] to complete a new chain link. It receives the
 * already existing chain and a lonely specification that must be linked to some instance(s) for
 * evaluation. Can only be used to create a longer [ChainedSpecifications] again. Is generic over
 * the type of the specification to ensure that only instance(s) of the correct type can be linked.
 */
class OpenChainedSpecifications<T : Any>(
    private final val specificationChain: SpecificationChain,
    private final val lonelySpecification: Specification<T>,
    private final val type: KClass<T>
) {
  /**
   * Links specification with some instance(s) to create a new chain link in the resulting
   * [OpenChainedSpecification].
   */
  fun with(instances: List<T>): ChainedSpecifications {
    val link = SpecificationChainLink(lonelySpecification, instances, type)
    @Suppress("UNCHECKED_CAST") // Type variance is fine here.
    return ChainedSpecifications((specificationChain + link) as SpecificationChain)
  }

  /** Convenience overload function to link a list of instances. */
  fun with(vararg instance: T) = with(instance.asList())
}

typealias SpecificationChain = List<SpecificationChainLink<Any>>

/**
 * Link in the chain for a [OpenChainedSpecifications]. Pairs a specification of for a certain data
 * type with some instances of the same type to evaluate at a later point in time.
 *
 * This also needs to "manually" preserve the type information to resolve the covariance issues when
 * merging chain links of different types.
 */
data class SpecificationChainLink<T : Any>(
    final val specification: Specification<T>,
    final val instances: List<T>,
    final val type: KClass<T>,
)
