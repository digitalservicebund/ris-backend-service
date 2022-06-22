package de.bund.digitalservice.ris;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ArchitectureFitnessTest {

  static final String DOMAIN_LAYER_PACKAGES = "de.bund.digitalservice.ris.domain..";

  static JavaClasses classes;

  @BeforeAll
  static void setUp() {
    classes = new ClassFileImporter().importPackages("de.bund.digitalservice.ris");
  }

  @Test
  void preventPackageImportCycles() {
    ArchRule rule =
        SlicesRuleDefinition.slices()
            .matching("de.bund.digitalservice.(**)")
            .should()
            .beFreeOfCycles();
    rule.check(classes);
  }
}
