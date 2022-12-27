package de.bund.digitalservice.ris.norms.conventions.predicate

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass

class HaveAMethodWithName(private val methodName: String) :
    DescribedPredicate<JavaClass>("have a method with name $methodName") {
    override fun test(item: JavaClass): Boolean {
        for (method in item.methods) {
            if (method.name == this.methodName) {
                return true
            }
        }

        return false
    }
}
