package de.bund.digitalservice.ris.caselaw;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClass.Predicates;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption.Predefined;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.library.dependencies.SliceRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

class ArchitectureFitnessTest {

  static final String ADAPTER_LAYER_PACKAGES = "de.bund.digitalservice.ris.caselaw.adapter..";
  static final String R2DBC_LAYER_PACKAGES =
      "de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc..";
  static final String JPA_LAYER_PACKAGES =
      "de.bund.digitalservice.ris.caselaw.adapter.database.jpa..";
  static final String DOMAIN_LAYER_PACKAGES = "de.bund.digitalservice.ris.caselaw.domain..";
  static final String RIS_PACKAGES = "de.bund.digitalservice.ris..";

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
            .matching("de.bund.digitalservice.ris.caselaw.(*)")
            .should()
            .beFreeOfCycles();
    rule.check(classes);
  }

  @Test
  @Disabled(
      "We currently change some stuff to use DTOs instead of Domain objects to move them into another repo. During this time this rule is violated in some spots.")
  void domainClassesShouldOnlyDependOnDomainOrExternalLibClasses() {
    ArchRule rule =
        ArchRuleDefinition.classes()
            .that()
            .resideInAPackage(DOMAIN_LAYER_PACKAGES)
            .should()
            .onlyDependOnClassesThat(
                Predicates.resideInAPackage(DOMAIN_LAYER_PACKAGES)
                    .or(Predicates.resideOutsideOfPackage(RIS_PACKAGES)));
    rule.check(classes);
  }

  @Test
  @Disabled("Domain classes should only have private fields")
  void domainClassesShouldOnlyHavePrivateFields() {
    ArchRule rule =
        ArchRuleDefinition.fields()
            .that()
            .areDeclaredInClassesThat()
            .areNotEnums()
            .and()
            .areDeclaredInClassesThat()
            .resideInAPackage(DOMAIN_LAYER_PACKAGES)
            .should()
            .bePrivate();
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

  @Test
  void onlyRepositoryInterfacesWithoutExtensionAllowedInDomain() {
    ArchRule rule =
        ArchRuleDefinition.classes()
            .that()
            .resideInAPackage(DOMAIN_LAYER_PACKAGES)
            .and()
            .haveSimpleNameEndingWith("Repository")
            .should()
            .beInterfaces()
            .andShould()
            .notBeAssignableTo(Object.class);
    rule.check(classes);
  }

  @Test
  @Disabled("Domain service interfaces should only be implemented in the adapter package")
  void ImplementationOfDomainServiceInterfacesOnlyAllowedInTheAdapterPackage() {
    DescribedPredicate<JavaClass> predicate =
        Predicates.resideInAPackage(DOMAIN_LAYER_PACKAGES).and(Predicates.INTERFACES);
    ArchRule rule =
        ArchRuleDefinition.classes()
            .that()
            .implement(predicate)
            .and()
            .areAnnotatedWith(Service.class)
            .should()
            .resideInAPackage(ADAPTER_LAYER_PACKAGES);
    rule.check(classes);
  }

  @Test
  void ImplementationOfDomainRepositoryInterfacesOnlyAllowedInTheAdapterPackage() {
    DescribedPredicate<JavaClass> predicate =
        Predicates.resideInAPackage(DOMAIN_LAYER_PACKAGES)
            .and(Predicates.INTERFACES)
            .and(Predicates.simpleNameEndingWith("Repository"));
    ArchRule rule =
        ArchRuleDefinition.classes()
            .that()
            .implement(predicate)
            .and()
            .areAnnotatedWith(Repository.class)
            .should()
            .resideInAPackage(ADAPTER_LAYER_PACKAGES);
    rule.check(classes);
  }

  @Test
  void repositoryInJPAAdapterPackageAreInterfacesWhichExtendJpaCrudRepository() {
    ArchRule rule =
        ArchRuleDefinition.classes()
            .that()
            .resideInAPackage(JPA_LAYER_PACKAGES)
            .and()
            .haveSimpleNameEndingWith("Repository")
            .should()
            .beInterfaces()
            .andShould()
            .beAssignableTo(JpaRepository.class);
    rule.check(classes);
  }

  @Test
  void allRestControllerEndpointsUsePreAuthorize() {
    ArchRule rule =
        ArchRuleDefinition.classes()
            .that()
            .areAnnotatedWith(RestController.class)
            .should(
                new ArchCondition<>("have all public methods annotated with @PreAuthorize") {
                  @Override
                  public void check(JavaClass javaClass, ConditionEvents conditionEvents) {
                    for (JavaMethod method : javaClass.getMethods()) {
                      if (method.getModifiers().contains(JavaModifier.PUBLIC)
                          && !method.isAnnotatedWith(PreAuthorize.class)) {
                        conditionEvents.add(
                            new SimpleConditionEvent(
                                method,
                                false,
                                String.format(
                                    "Method %s is not annotated with @PreAuthorize",
                                    javaClass.getSimpleName() + "." + method.getName())));
                      }
                    }
                  }
                });
    rule.check(classes);
  }
}
