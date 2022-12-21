package de.bund.digitalservice.ris.norms.conventions

import com.tngtech.archunit.base.DescribedPredicate.not
import com.tngtech.archunit.core.domain.JavaClass
import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage
import com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage
import com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith
import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption.Predefined
import com.tngtech.archunit.lang.ArchCondition
import com.tngtech.archunit.lang.conditions.ArchPredicates.are
import de.bund.digitalservice.ris.norms.conventions.predicate.Predicates.aKotlinStaticClass

const val BASE_PACKAGE_PATH = "de.bund.digitalservice.ris.norms"

val allClasses =
    ClassFileImporter()
        .withImportOption(Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("$BASE_PACKAGE_PATH..")
        .that(are(not(aKotlinStaticClass)))

val areFromTheDomain = resideInAPackage("$BASE_PACKAGE_PATH.domain..")
val areFromTheApplication = resideInAPackage("$BASE_PACKAGE_PATH.application..")

private val inputPortPackage = "$BASE_PACKAGE_PATH.application.port.input.."
val areFromTheInputPortPackage = resideInAPackage(inputPortPackage)
val beInsideTheInputPortPackage = ArchCondition.from<JavaClass>(areFromTheInputPortPackage)
val areAnInputPort = areFromTheInputPortPackage.and(simpleNameEndingWith("UseCase"))
val anInputPort = areAnInputPort

private val outputPortPackage = "$BASE_PACKAGE_PATH.application.port.output.."
val areFromTheOutputPortPackage = resideInAPackage(outputPortPackage)
val beInsideTheOutputPortPackage = ArchCondition.from<JavaClass>(areFromTheOutputPortPackage)
val areAnOutputPort = areFromTheOutputPortPackage.and(simpleNameEndingWith("OutputPort"))
val anyOutputPort = areAnOutputPort

val areAnyPort = areAnInputPort.or(areAnOutputPort)

val areFromTheServicePackage = resideInAPackage("$BASE_PACKAGE_PATH.application.service..")
val areAService = areFromTheServicePackage.and(simpleNameEndingWith("Service"))

val areFromAnyStandardLibrary =
    resideInAnyPackage("kotlin..", "java..", "org.jetbrains.annotations..")
