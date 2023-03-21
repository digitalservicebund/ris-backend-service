package de.bund.digitalservice.ris.norms.domain.specification

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class SpecificationTest {
    private val alwaysTrue = object : Specification<Unit> {
        override fun isSatisfiedBy(instance: Unit) = true
    }

    private val alwaysFalse = object : Specification<Unit> {
        override fun isSatisfiedBy(instance: Unit) = false
    }

    @Test
    fun `it allows negate a specification`() {
        val negated = alwaysFalse.not()

        assertThat(negated.isSatisfiedBy(Unit)).isTrue()
    }

    @Test
    fun `it allows negate a specification via operator`() {
        val negated = !alwaysFalse

        assertThat(negated.isSatisfiedBy(Unit)).isTrue()
    }

    @Test
    fun `is satisfied if both anded specifications are satisfied`() {
        val anded = alwaysTrue.and(alwaysTrue)

        assertThat(anded.isSatisfiedBy(Unit)).isTrue()
    }

    @Test
    fun `is not satisfied if any of the anded specifications is not satified`() {
        val andedLeftUnsatisfied = alwaysFalse.and(alwaysTrue)
        val andedRightUnsatisfied = alwaysTrue.and(alwaysFalse)
        val andedBothUnsatisfied = alwaysFalse.and(alwaysFalse)

        assertThat(andedLeftUnsatisfied.isSatisfiedBy(Unit)).isFalse()
        assertThat(andedRightUnsatisfied.isSatisfiedBy(Unit)).isFalse()
        assertThat(andedBothUnsatisfied.isSatisfiedBy(Unit)).isFalse()
    }

    @Test
    fun `is satisfied if any of the ored specifications is satisfied`() {
        val oredLeftSatisfied = alwaysTrue.or(alwaysFalse)
        val oredRightSatisfied = alwaysFalse.or(alwaysTrue)
        val oredBothSatisfied = alwaysTrue.or(alwaysTrue)

        assertThat(oredLeftSatisfied.isSatisfiedBy(Unit)).isTrue()
        assertThat(oredRightSatisfied.isSatisfiedBy(Unit)).isTrue()
        assertThat(oredBothSatisfied.isSatisfiedBy(Unit)).isTrue()
    }

    @Test
    fun `is not satisfied if both ored specifications are not satisfied`() {
        val ored = alwaysFalse.and(alwaysFalse)

        assertThat(ored.isSatisfiedBy(Unit)).isFalse()
    }

    @Test
    fun `it allows to do chaining of operations with specifications`() {
        val chained = alwaysTrue.and(alwaysTrue.or(alwaysFalse)).not().or(alwaysTrue)

        assertThat(chained.isSatisfiedBy(Unit)).isTrue()
    }
}
