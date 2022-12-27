package de.bund.digitalservice.ris.norms.conventions.predicate

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass

class ImplementExactNumberOfInterfaces(private val number: Int) :
    DescribedPredicate<JavaClass>("implement exactly $number interface(s)") {
    override fun test(item: JavaClass): Boolean {
        return item.interfaces.size == this.number
    }
}
