package de.bund.digitalservice.ris.norms.conventions.predicate

object Predicates {
    val aKotlinStaticClass = AKotlinStaticClass()

    fun haveAMethodWithName(methodName: String) = HaveAMethodWithName(methodName)

    fun haveExactNumberOfMethods(number: Int) = HaveExactNumberOfMethods(number)
    val haveNoMethod = HaveExactNumberOfMethods(0)
    val haveASingleMethod = HaveExactNumberOfMethods(1)

    fun haveExactNumberOfParameters(number: Int) = HaveExactNumberOfParameters(number)
    val haveNoParameter = HaveExactNumberOfParameters(0)
    val haveASingleParameter = HaveExactNumberOfParameters(1)

    fun haveAParameterWithTypeName(typeName: String) = HaveAParameterWithTypeName(typeName)

    fun haveAMethodWithNameLikeClassPrefix(classPostfix: String) =
        HaveAMethodWithNameLikeClassPrefix(classPostfix)

    fun implementExactNumberOfInterfaces(number: Int) = ImplementExactNumberOfInterfaces(number)
    val implementNoInterface = ImplementExactNumberOfInterfaces(0)
    val implementASingleInterface = ImplementExactNumberOfInterfaces(1)

    fun implementInterfaceWithSamePrefix(classPostfix: String, interfacePostfix: String) =
        ImplementInterfaceWithSamePrefix(classPostfix, interfacePostfix)
}
