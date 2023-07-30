package de.bund.digitalservice.ris.norms.domain.specification

import de.bund.digitalservice.ris.norms.domain.specification.SpecificationResult.Satisfied
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ChainedSpecificationsTest {
  @Test
  fun `it allows to chain a single specification with a single instance`() {
    assertThat(chain(greaterThanFive).with(4).evaluateAll().isSatisfied).isFalse()
    assertThat(chain(greaterThanFive).with(6).evaluateAll().isSatisfied).isTrue()
  }

  @Test
  fun `it allows to chain a single specification with a variable number of instances`() {
    assertThat(chain(greaterThanFive).with(3, 4, 5).evaluateAll().isSatisfied).isFalse()
    assertThat(chain(greaterThanFive).with(6, 7, 8).evaluateAll().isSatisfied).isTrue()
  }

  @Test
  fun `it allows to chain a single specification with a list of instances`() {
    assertThat(chain(greaterThanFive).with(listOf(3, 4, 5)).evaluateAll().isSatisfied).isFalse()
    assertThat(chain(greaterThanFive).with(listOf(6, 7, 8)).evaluateAll().isSatisfied).isTrue()
  }

  @Test
  fun `it allows to chain a multiple specification with a some instance(s)`() {
    assertThat(chain(greaterThanFive).with(3, 4).and(isTrue).with(false).evaluateAll().isSatisfied)
        .isFalse()
    assertThat(chain(greaterThanFive).with(6, 7).and(isTrue).with(true).evaluateAll().isSatisfied)
        .isTrue()
  }

  @Test
  fun `it alls the correct specification with the correct instance`() {
    val specificationOne = mockk<Specification<Int>>()
    every { specificationOne.evaluate(any<List<Int>>()) } returns Satisfied

    val specificationTwo = mockk<Specification<Int>>()
    every { specificationTwo.evaluate(any<List<Int>>()) } returns Satisfied

    chain(specificationOne).with(1).and(specificationTwo).with(2).evaluateAll()

    verify(exactly = 1) {
      specificationOne.evaluate(withArg<List<Int>> { assertThat(it).isEqualTo(listOf(1)) })
    }
    verify(exactly = 1) {
      specificationTwo.evaluate(withArg<List<Int>> { assertThat(it).isEqualTo(listOf(2)) })
    }
  }
}

private val greaterThanFive =
    Specification.fromPredicate<Int>("LESS_OR_EQUAL_FIVE", "number is not greater than 5") {
      it > 5
    }

private val isTrue = Specification.fromPredicate<Boolean>("IS_FALSE", "boolean is not true") { it }
