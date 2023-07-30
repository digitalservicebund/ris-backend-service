package de.bund.digitalservice.ris.norms.domain.specification

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SpecificationTest {
  @Test
  fun `it allows to evaluate a specification with a single instance`() {
    assertThat(greaterThanFive.evaluate(4).isSatisfied).isFalse()
    assertThat(greaterThanFive.evaluate(6).isSatisfied).isTrue()
  }

  @Test
  fun `it allows to evaluate a specification with variable number of instances`() {
    assertThat(greaterThanFive.evaluate(3, 4, 5).isSatisfied).isFalse()
    assertThat(greaterThanFive.evaluate(6, 7, 8).isSatisfied).isTrue()
  }

  @Test
  fun `it allows to evaluate a specification with a collection of instances`() {
    assertThat(greaterThanFive.evaluate(listOf(3, 4, 5)).isSatisfied).isFalse()
    assertThat(greaterThanFive.evaluate(listOf(6, 7, 8)).isSatisfied).isTrue()
  }

  @Test
  fun `it includes the evaluted instance(s) with error codes and message when unsatisfied`() {
    val result = greaterThanFive.evaluate(1, 6, 7, 2)

    assertThat(result.violations).hasSize(2)
    assertThat(result.violations)
        .contains(
            SpecificationViolation(1, "LESS_OR_EQUAL_FIVE", "number '1' is not greater than 5"),
            SpecificationViolation(2, "LESS_OR_EQUAL_FIVE", "number '2' is not greater than 5"),
        )
  }

  @Test
  fun `it allows to create specification from a predicate`() {
    val lessThanSeven = Specification.fromPredicate<Int>("TEST_CODE", "test message") { it < 7 }

    assertThat(lessThanSeven.evaluate(6).isSatisfied).isTrue()

    val unsatisfiedResult = lessThanSeven.evaluate(8)
    assertThat(unsatisfiedResult.violations).hasSize(1)
    assertThat(unsatisfiedResult.violations[0].code).isEqualTo("TEST_CODE")
    assertThat(unsatisfiedResult.violations[0].message).isEqualTo("test message")
  }

  @Nested
  inner class AndSpecification {
    @Test
    fun `the overall result is satisfied if both inner results are`() {
      assertThat(greaterThanFive.and(greaterThanTen).evaluate(12).isSatisfied).isTrue()
    }

    @Test
    fun `the overall result is unsatisfied if any inner result is unsatisfied`() {
      val resultLeftUnsatisfied = greaterThanFive.and(lessThanFive).evaluate(4)

      assertThat(resultLeftUnsatisfied.isSatisfied).isFalse()
      assertThat(resultLeftUnsatisfied.violations).hasSize(1)
      assertThat(resultLeftUnsatisfied.violations[0].code).isEqualTo("LESS_OR_EQUAL_FIVE")

      val resultRightUnsatisfied = greaterThanFive.and(lessThanFive).evaluate(8)
      assertThat(resultRightUnsatisfied.isSatisfied).isFalse()
      assertThat(resultRightUnsatisfied.violations).hasSize(1)
      assertThat(resultRightUnsatisfied.violations[0].code).isEqualTo("GREATER_OR_EQUAL_FIVE")
    }

    @Test
    fun `the overall result is unsatisfied if both inner results are unsatisfied`() {
      val result = greaterThanFive.and(lessThanFive).evaluate(5)

      assertThat(result.isSatisfied).isFalse()
      assertThat(result.violations).hasSize(2)
      assertThat(result.violations.map { it.code })
          .contains("LESS_OR_EQUAL_FIVE", "GREATER_OR_EQUAL_FIVE")
    }

    @Test
    fun `it allows to use the plus infix operator to combine specifications`() {
      assertThat((greaterThanFive + greaterThanTen).evaluate(12).isSatisfied).isTrue()
    }
  }
}

private val greaterThanFive =
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

private val lessThanFive =
    object : Specification<Int> {
      override fun evaluate(instance: Int) =
          SpecificationResult.from(
              instance,
              "GREATER_OR_EQUAL_FIVE",
              "number '$instance' is not less than 5",
          ) {
            instance < 5
          }
    }

private val greaterThanTen =
    object : Specification<Int> {
      override fun evaluate(instance: Int) =
          SpecificationResult.from(
              instance,
              "LESS_OR_EQUAL_TEN",
              "number '$instance' is not greater than 10",
          ) {
            instance > 10
          }
    }
