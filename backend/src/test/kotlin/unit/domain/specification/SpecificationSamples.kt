package de.bund.digitalservice.ris.norms.domain.specification

import org.assertj.core.api.Assertions.assertThat

class SpecificationSamples {
  fun alternativeSpecificationDefinitions() {
    class GreaterThan(val limit: Int) : Specification<Int> {
      override fun evaluate(instance: Int): SpecificationResult {
        return if (instance < limit) {
          SpecificationResult.Satisfied
        } else
            SpecificationResult.Unsatisfied(
                SpecificationViolation(
                    instance,
                    "TOO_SMALL",
                    "number '$instance' is not greater than $limit",
                ),
            )
      }
    }

    assertThat(GreaterThan(5).evaluate(6).isSatisfied).isTrue()

    val greaterThanFive =
        object : Specification<Int> {
          override fun evaluate(instance: Int) =
              SpecificationResult.from(
                  instance,
                  "LESS_OR_EQUAL_FIVE",
                  "number '$instance' is not greater than 5",
              ) {
                instance > 5
              }
        }

    assertThat(greaterThanFive.evaluate(6).isSatisfied).isTrue()

    val lessThanSeven =
        Specification.fromPredicate<Int>(
            "GREATER_OR_EQUAL_SEVEN",
            "number is greater than seven",
        ) {
          it < 7
        }

    assertThat(lessThanSeven.evaluate(6).isSatisfied).isTrue()
  }
}

private val someNumbers = listOf(8, 9)
private val listOfSomething = listOf(null, 1, 5)

private val isPositive =
    Specification.fromPredicate<Int>("IS_NULL", "must not be null") { instance -> instance > 0 }
private val greaterThanFive =
    Specification.fromPredicate<Int>(
        "IS_LESS_OR_EQUAL_FIVE",
        "number it not greater than 5",
    ) {
      it > 5
    }
private val lessThanSeven =
    Specification.fromPredicate<Int>(
        "IS_GREATER_OR_EQUAL_SEVEN",
        "number it not less than 5",
    ) {
      it < 7
    }

private class GreaterThan(val limit: Int) : Specification<Int> {
  override fun evaluate(instance: Int): SpecificationResult {
    return if (instance < limit) {
      SpecificationResult.Satisfied
    } else
        SpecificationResult.Unsatisfied(
            SpecificationViolation(
                instance,
                "TOO_SMALL",
                "number '$instance' is not greater than $limit",
            ),
        )
  }
}

private class LessThan(val limit: Int) : Specification<Int> {
  override fun evaluate(instance: Int): SpecificationResult {
    return if (instance < limit) {
      SpecificationResult.Satisfied
    } else
        SpecificationResult.Unsatisfied(
            SpecificationViolation(
                instance,
                "TOO_BIG",
                "number '$instance' is not less than $limit",
            ),
        )
  }
}

private val isSuperSpecial =
    Specification.fromPredicate<Any>("IS_NOT_SPECICAL", "must be special") { instance ->
      instance is Number
    }
