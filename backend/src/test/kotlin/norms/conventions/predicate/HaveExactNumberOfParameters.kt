package de.bund.digitalservice.ris.norms.conventions.predicate

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaMethod

class HaveExactNumberOfParameters(private val number: Int) :
    DescribedPredicate<JavaMethod>("have exactly $number parameter(s)") {
    override fun test(item: JavaMethod): Boolean {
        return item.parameters.size == this.number
    }
}
