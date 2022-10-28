package de.bund.digitalservice.ris.caselaw;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption.Predefined;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.library.dependencies.SliceRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

class ArchitectureFitnessTest {

  static final String ADAPTER_LAYER_PACKAGES = "de.bund.digitalservice.ris.caselaw.adapter..";
  static final String DOMAIN_LAYER_PACKAGES = "de.bund.digitalservice.ris.caselaw.domain..";

  static JavaClasses classes;

  @BeforeAll
  static void setUp() {
    classes =
        new ClassFileImporter()
            .withImportOption(Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("de.bund.digitalservice.ris.caselaw");
  }

  @Test
  void packagesShouldBeFreeOfCycles() {
    SliceRule rule =
        SlicesRuleDefinition.slices()
            .matching("de.bund.digitalservice.(**)")
            .should()
            .beFreeOfCycles();
    rule.check(classes);
  }

  @Test
  @Disabled("Domain services need to be refactored to not depend on outside packages")
  void domainClassesShouldOnlyDependOnDomainOrStdLibClasses() {
    ArchRule rule =
        ArchRuleDefinition.classes()
            .that()
            .resideInAPackage(DOMAIN_LAYER_PACKAGES)
            .should()
            .onlyDependOnClassesThat()
            .resideInAnyPackage(DOMAIN_LAYER_PACKAGES, "java..");
    rule.check(classes);
  }

  @Test
  void controllerClassesShouldNotResideOutsideOfAdapterPackage() {
    ArchRule rule =
        ArchRuleDefinition.noClasses()
            .that()
            .areAnnotatedWith(Controller.class)
            .or()
            .areAnnotatedWith(RestController.class)
            .should()
            .resideOutsideOfPackage(ADAPTER_LAYER_PACKAGES);
    rule.check(classes);
  }

  @Test
  void controllerClassesShouldNotDependOnEachOther() {
    ArchRule rule =
        ArchRuleDefinition.noClasses()
            .that()
            .haveSimpleNameEndingWith("Controller")
            .should()
            .dependOnClassesThat()
            .resideInAPackage(ADAPTER_LAYER_PACKAGES)
            .andShould()
            .dependOnClassesThat()
            .areAnnotatedWith(Controller.class)
            .andShould()
            .dependOnClassesThat()
            .areAnnotatedWith(RestController.class);
    rule.check(classes);
  }
}
