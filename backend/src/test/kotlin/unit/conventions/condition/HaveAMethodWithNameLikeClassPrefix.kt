package de.bund.digitalservice.ris.norms.conventions.condition

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import de.bund.digitalservice.ris.norms.conventions.predicate.HaveAMethodWithNameLikeClassPrefix

class HaveAMethodWithNameLikeClassPrefix(private val classPostfix: String) :
    ArchCondition<JavaClass>(
        "have a method named like class prefix where prefix is '$classPostfix'",
    ) {
    override fun check(item: JavaClass, events: ConditionEvents) {
        val predicate = HaveAMethodWithNameLikeClassPrefix(this.classPostfix)
        val methodExists = predicate.test(item)

        if (!methodExists) {
            val expecteMethodName = predicate.getExpectedMethodName(item)
            val message =
                "expected '${item.simpleName}' to end with '${this.classPostfix}' and have method with name '$expecteMethodName'"
            val event = SimpleConditionEvent.violated(item, message)
            events.add(event)
        }
    }
}
