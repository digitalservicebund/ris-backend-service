package de.bund.digitalservice.ris.norms.conventions.predicate

object Predicates {
    fun aKotlinStaticClass() = AKotlinStaticClass()

    fun haveAMethodWithName(methodName: String) = HaveAMethodWithName(methodName)

    fun haveExactNumberOfMethods(number: Int) = HaveExactNumberOfMethods(number)
    fun haveNoMethod() = HaveExactNumberOfMethods(0)
    fun haveASingleMethod() = HaveExactNumberOfMethods(1)

    fun haveExactNumberOfParameters(number: Int) = HaveExactNumberOfParameters(number)
    fun haveNoParameter() = HaveExactNumberOfParameters(0)
    fun haveASingleParameter() = HaveExactNumberOfParameters(1)

    fun haveAParameterWithTypeName(typeName: String) = HaveAParameterWithTypeName(typeName)

    fun haveAMethodWithNameLikeClassPrefix(classPostfix: String) =
        HaveAMethodWithNameLikeClassPrefix(classPostfix)

    fun implementExactNumberOfInterfaces(number: Int) = ImplementExactNumberOfInterfaces(number)
    fun implementNoInterface() = ImplementExactNumberOfInterfaces(0)
    fun implementASingleInterface() = ImplementExactNumberOfInterfaces(1)

    fun implementInterfaceWithSamePrefix(classPostfix: String, interfacePostfix: String) =
        ImplementInterfaceWithSamePrefix(classPostfix, interfacePostfix)
}
