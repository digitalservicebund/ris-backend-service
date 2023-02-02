package de.bund.digitalservice.ris.norms.conventions.predicate

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith
import de.bund.digitalservice.ris.norms.conventions.predicate.Predicates.implementASingleInterface

/**
 * Example: `LoadNormService` with postfix `Service` implements interface with name
 * `LoadNormUseCase` for the interface postfix `UseCase` (matching prefix is 'LoadNorm')
 */
class ImplementInterfaceWithSamePrefix(
    private val classPostfix: String,
    private val interfacePostfix: String,
) :
    DescribedPredicate<JavaClass>(
        "with postfix '$classPostfix' has the same prefix as the interface it implements with postfix '$interfacePostfix'",
    ) {
    override fun test(item: JavaClass): Boolean {
        val classHasPostfix = simpleNameEndingWith(this.classPostfix).test(item)
        val classImplementsSingleInterface = implementASingleInterface().test(item)

        if (!classHasPostfix || !classImplementsSingleInterface) {
            return false
        }

        val interfaceHasPostfix =
            simpleNameEndingWith(this.interfacePostfix).test(item.rawInterfaces.elementAt(0))

        if (!interfaceHasPostfix) {
            return false
        }

        val expectedClassName = this.getExpectedClassName(item)
        return item.simpleName == expectedClassName
    }

    fun getExpectedClassName(item: JavaClass): String {
        val firstInterface = item.rawInterfaces.elementAt(0)
        val interfacePrefix = firstInterface.simpleName.replace(this.interfacePostfix, "")
        return interfacePrefix + classPostfix
    }
}
