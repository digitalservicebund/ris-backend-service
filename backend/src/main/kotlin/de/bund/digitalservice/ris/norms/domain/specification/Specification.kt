package de.bund.digitalservice.ris.norms.domain.specification

interface Specification<T> {
    fun isSatisfiedBy(instance: T): Boolean

    operator fun not() = NotSpecification(this)

    fun and(other: Specification<T>) = AndSpecification(this, other)

    fun or(other: Specification<T>) = OrSpecification(this, other)
}

class NotSpecification<T>(val inner: Specification<T>) : Specification<T> {
    override fun isSatisfiedBy(instance: T) = !inner.isSatisfiedBy(instance)
}

class AndSpecification<T>(val left: Specification<T>, val right: Specification<T>) :
    Specification<T> {

    override fun isSatisfiedBy(instance: T) =
        left.isSatisfiedBy(instance) && right.isSatisfiedBy(instance)
}

class OrSpecification<T>(val left: Specification<T>, val right: Specification<T>) :
    Specification<T> {

    override fun isSatisfiedBy(instance: T) =
        left.isSatisfiedBy(instance) || right.isSatisfiedBy(instance)
}
