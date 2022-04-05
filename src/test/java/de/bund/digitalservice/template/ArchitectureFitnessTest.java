package de.bund.digitalservice.template;

import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "de.bund.digitalservice")
class ArchitectureFitnessTest {

  @ArchTest
  static final ArchRule preventPackageImportCycles =
      slices().matching("de.bund.digitalservice.(**)").should().beFreeOfCycles();
}
