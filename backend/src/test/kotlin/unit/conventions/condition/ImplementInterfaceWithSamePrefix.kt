package de.bund.digitalservice.ris.norms.conventions.condition

import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.ConditionEvents
import com.tngtech.archunit.lang.SimpleConditionEvent
import de.bund.digitalservice.ris.norms.conventions.predicate.ImplementInterfaceWithSamePrefix

class ImplementInterfaceWithSamePrefix(
    private val classPostfix: String,
    private val interfacePostfix: String,
) :
    ArchCondition<JavaClass>(
        "have the same prefix as the interface it implements for the class postfix '$classPostfix'" +
            " and interface postfix '$interfacePostfix'",
    ) {
    override fun check(item: JavaClass, events: ConditionEvents) {
        val predicate = ImplementInterfaceWithSamePrefix(this.classPostfix, this.interfacePostfix)
        val prefixIsCorrect = predicate.test(item)

        if (!prefixIsCorrect) {
            val expectedClassName = predicate.getExpectedClassName(item)
            val message = "expected class '${item.simpleName}' to have name '$expectedClassName'"
            val event = SimpleConditionEvent.violated(item, message)
            events.add(event)
        }
    }
}
