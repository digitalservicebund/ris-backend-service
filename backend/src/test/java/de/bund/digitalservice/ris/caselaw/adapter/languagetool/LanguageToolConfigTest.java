package de.bund.digitalservice.ris.caselaw.adapter.languagetool;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.context.properties.IncompatibleConfigurationException;

class LanguageToolConfigTest {

  // Data provider for valid configurations that should not throw an exception.
  private static Stream<LanguageToolConfig> validConfigurations() {
    // Valid configuration with all fields populated
    LanguageToolConfig config1 = new LanguageToolConfig();
    config1.setDisabledCategories("CAT1,CAT2");
    config1.setDisabledRules("RULE1,RULE2");
    config1.setDisabledCategoriesWithWhitelistedRules(
        Map.of(
            "CAT3", List.of("RULE3", "RULE4"),
            "CAT4", List.of("RULE5")));

    // Valid configuration with empty disabled categories list
    LanguageToolConfig config2 = new LanguageToolConfig();
    config2.setDisabledCategories("");
    config2.setDisabledRules("RULE1,RULE2");
    config2.setDisabledCategoriesWithWhitelistedRules(Map.of("CAT1", List.of("RULE3")));

    // Valid configuration with empty disabled rules list
    LanguageToolConfig config3 = new LanguageToolConfig();
    config3.setDisabledCategories("CAT1,CAT2");
    config3.setDisabledRules("");
    config3.setDisabledCategoriesWithWhitelistedRules(Map.of("CAT3", List.of("RULE3")));

    // All lists are empty
    LanguageToolConfig config4 = new LanguageToolConfig();
    config4.setDisabledCategories("");
    config4.setDisabledRules("");
    config4.setDisabledCategoriesWithWhitelistedRules(Collections.emptyMap());

    return Stream.of(config1, config2, config3, config4);
  }

  // Data provider for invalid configurations that should throw an exception.
  private static Stream<LanguageToolConfig> invalidConfigurations() {
    // Disabled category has whitelisted rules
    LanguageToolConfig config1 = new LanguageToolConfig();
    config1.setDisabledCategories("CAT1,CAT2");
    config1.setDisabledRules("RULE1");
    config1.setDisabledCategoriesWithWhitelistedRules(
        Map.of(
            "CAT1", List.of("RULE3"),
            "CAT3", List.of("RULE4")));

    // Disabled rule is whitelisted in a category
    LanguageToolConfig config2 = new LanguageToolConfig();
    config2.setDisabledCategories("CAT2,CAT3");
    config2.setDisabledRules("RULE1,RULE2");
    config2.setDisabledCategoriesWithWhitelistedRules(
        Map.of(
            "CAT4", List.of("RULE1", "RULE3"),
            "CAT5", List.of("RULE4")));

    return Stream.of(config1, config2);
  }

  @ParameterizedTest
  @MethodSource("validConfigurations")
  void shouldPassValidation(LanguageToolConfig config) {
    assertDoesNotThrow(config::afterPropertiesSet);
  }

  @ParameterizedTest
  @MethodSource("invalidConfigurations")
  void shouldThrowExceptionForIncompatibleConfig(LanguageToolConfig config) {
    assertThrows(IncompatibleConfigurationException.class, config::afterPropertiesSet);
  }
}
