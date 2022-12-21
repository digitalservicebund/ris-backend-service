package de.bund.digitalservice.ris.norms.conventions.predicate

import com.tngtech.archunit.base.DescribedPredicate
import com.tngtech.archunit.core.domain.JavaClass
import java.lang.reflect.Modifier

/**
 * In Kotlin files it is possible to define top-level functions and variables. When this gets
 * compiled to Java, it automatically creates a "helper" class with static fields for all top-level
 * items. The class receives a postfix of `Kt`.
 *
 * In ArchUnit tests this can become an issue as these classes want to be ignored or get special
 * treatment.
 */
class AKotlinStaticClass : DescribedPredicate<JavaClass>("an auto-generated Kotlin static class") {
    // TODO: Check why these classes are not identifiable as synthetic classes.
    override fun test(item: JavaClass): Boolean {
        val nonStaticMethods = item.methods.filter { !Modifier.isStatic(it.reflect().getModifiers()) }
        val nonStaticFields = item.fields.filter { !Modifier.isStatic(it.reflect().getModifiers()) }

        val hasSpecialPostfix = item.name.endsWith("Kt")
        val everythingIsStatic = nonStaticMethods.size == 0 && nonStaticFields.size == 0

        return hasSpecialPostfix && everythingIsStatic
    }
}
