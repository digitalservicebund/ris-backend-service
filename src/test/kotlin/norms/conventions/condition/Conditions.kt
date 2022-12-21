package de.bund.digitalservice.ris.norms.conventions.condition

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaMethod
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.conditions.ArchPredicates.be
import de.bund.digitalservice.ris.norms.conventions.predicate.AKotlinStaticClass
import de.bund.digitalservice.ris.norms.conventions.predicate.Predicates

object Conditions {
    fun beAKotlinStaticClass() = ArchCondition.from<JavaClass>(be(AKotlinStaticClass()))

    fun haveAMethodWithName(methodName: String) =
        ArchCondition.from<JavaClass>(Predicates.haveAMethodWithName(methodName))

    fun haveExactNumberOfMethods(number: Int) =
        ArchCondition.from<JavaClass>(Predicates.haveExactNumberOfMethods(number))
    fun haveNoMethod() = ArchCondition.from<JavaClass>(Predicates.haveNoMethod())
    fun haveASingleMethod() = ArchCondition.from<JavaClass>(Predicates.haveASingleMethod())

    fun haveExactNumberOfParameters(number: Int) =
        ArchCondition.from<JavaMethod>(Predicates.haveExactNumberOfParameters(number))
    fun haveNoParameter() = ArchCondition.from<JavaMethod>(Predicates.haveNoParameter())
    fun haveASingleParameter() = ArchCondition.from<JavaMethod>(Predicates.haveASingleParameter())

    fun haveAParameterWithTypeName(typeName: String) =
        ArchCondition.from<JavaMethod>(Predicates.haveAParameterWithTypeName(typeName))

    fun haveAMethodWithNameLikeClassPrefix(classPostfix: String) =
        HaveAMethodWithNameLikeClassPrefix(classPostfix)

    fun implementExactNumberOfInterfaces(number: Int) =
        ArchCondition.from<JavaClass>(Predicates.implementExactNumberOfInterfaces(number))
    fun implementNoInterface() = ArchCondition.from<JavaClass>(Predicates.implementNoInterface())
    fun implementASingleInterface() =
        ArchCondition.from<JavaClass>(Predicates.implementASingleInterface())

    fun implementInterfaceWithSamePrefix(classPostfix: String, interfacePostfix: String) =
        ImplementInterfaceWithSamePrefix(classPostfix, interfacePostfix)
}
