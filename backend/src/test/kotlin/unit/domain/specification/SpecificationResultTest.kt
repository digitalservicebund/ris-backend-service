package de.bund.digitalservice.ris.norms.domain.specification

import de.bund.digitalservice.ris.norms.domain.specification.SpecificationResult.Satisfied
import de.bund.digitalservice.ris.norms.domain.specification.SpecificationResult.Unsatisfied
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchException
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class SpecificationResultTest {
  @Test
  fun `the satisfied result has no violations`() {
    assertThat(Satisfied.violations).isEmpty()
  }

  @Test
  fun `it is not possible to create an unsatisfied result without violations`() {
    val exception = catchException { Unsatisfied() }

    assertThat(exception).isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `it is possible to create an unsatisfied result with variable violations`() {
    val violationOne = SpecificationViolation(Unit, "CODE_ONE", "message one")
    val violationTwo = SpecificationViolation(Unit, "CODE_TWO", "message two")

    val result = Unsatisfied(violationOne, violationTwo)

    assertThat(result.violations).isEqualTo(listOf(violationOne, violationTwo))
  }

  @Test
  fun `it is possible to create an unsatisfied result with list of violations`() {
    val violationOne = SpecificationViolation(Unit, "CODE_ONE", "message one")
    val violationTwo = SpecificationViolation(Unit, "CODE_TWO", "message two")

    val result = Unsatisfied(listOf(violationOne, violationTwo))

    assertThat(result.violations).isEqualTo(listOf(violationOne, violationTwo))
  }

  @Test
  fun `the reciprocal of a satisfied result is false`() {
    assertThat(Satisfied.isNotSatisfied).isFalse()
  }

  @Test
  fun `the reciprocal of an unsatisfied result is true`() {
    val result = Unsatisfied(SpecificationViolation(Unit, "TEST_CODE", "test message"))

    assertThat(result.isNotSatisfied).isTrue()
  }

  @Test
  fun `it does nothing when trying to trow on a satisfied result`() {
    val result = Satisfied

    assertThat(result.throwWhenUnsatisfied()).isEqualTo(result)
  }

  @Test
  fun `it throws an exception including th violations`() {
    val violations =
        listOf(
            SpecificationViolation(Unit, "CODE_ONE", "message one"),
            SpecificationViolation(Unit, "CODE_TWO", "message two"))

    val result = Unsatisfied(violations)
    val exception = catchException { result.throwWhenUnsatisfied() }

    assertThat(exception).isInstanceOf(SpecificationError::class.java)
    assertThat((exception as SpecificationError).violations).containsAll(violations)
  }

  @Nested
  inner class Union {
    @Test
    fun `the combined result is satisfied if both inner are satisfied`() {
      assertThat(Satisfied.and(Satisfied).isSatisfied).isTrue()
    }

    @Test
    fun `the combined result is unsatisfied if any inner is unsatisfied`() {
      val resultLeftUnsatisfied =
          Unsatisfied(SpecificationViolation(Unit, "LEFT_CODE", "left message")).and(Satisfied)

      assertThat(resultLeftUnsatisfied.isSatisfied).isFalse()
      assertThat(resultLeftUnsatisfied.violations).hasSize(1)
      assertThat(resultLeftUnsatisfied.violations[0].code).isEqualTo("LEFT_CODE")

      val resultRightUnsatisfied =
          Satisfied.and(Unsatisfied(SpecificationViolation(Unit, "RIGHT_CODE", "left message")))

      assertThat(resultRightUnsatisfied.isSatisfied).isFalse()
      assertThat(resultRightUnsatisfied.violations).hasSize(1)
      assertThat(resultRightUnsatisfied.violations[0].code).isEqualTo("RIGHT_CODE")
    }

    @Test
    fun `the combined result is unsatisfied if both inner are unsatisfied`() {
      val result =
          Unsatisfied(SpecificationViolation(Unit, "LEFT_CODE", "left message"))
              .and(Unsatisfied(SpecificationViolation(Unit, "RIGHT_CODE", "right message")))

      assertThat(result.isSatisfied).isFalse()
      assertThat(result.violations).hasSize(2)
      assertThat(result.violations.map { it.code }).contains("LEFT_CODE", "RIGHT_CODE")
    }

    @Test
    fun `it allows to use the plus infix operator to combine results`() {
      assertThat((Satisfied + Satisfied).isSatisfied).isTrue()
    }
  }
}
