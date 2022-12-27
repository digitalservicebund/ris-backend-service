package de.bund.digitalservice.ris.norms.conventions.predicate

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaMethod

class HaveAParameterWithTypeName(private val typeName: String) :
    DescribedPredicate<JavaMethod>("have a parameter of type name '$typeName'") {
    override fun test(item: JavaMethod): Boolean {
        val matchingParameters =
            item.parameters
                .map({ it.type.getName() })
                .filter({ it == typeName || it.endsWith("\$$typeName") })

        return matchingParameters.size > 0
    }
}
