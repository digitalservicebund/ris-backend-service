package de.bund.digitalservice.ris.norms.conventions.predicate

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass

class HaveExactNumberOfMethods(private val number: Int) :
    DescribedPredicate<JavaClass>("have exactly $number method(s)") {
    override fun test(item: JavaClass): Boolean {
        return item.methods.size == this.number
    }
}
