package de.bund.digitalservice.ris.caselaw.adapter.languagetool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.IncompatibleConfigurationException;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "languagetool")
@Data
public class LanguageToolConfig implements InitializingBean {
  private String url;
  private String language;
  private boolean enabled;
  private List<String> disabledRules = new ArrayList<>();
  private List<String> disabledCategories = new ArrayList<>();
  private Map<String, List<String>> disabledCategoriesWithWhitelistedRules = new HashMap<>();

  @Override
  public void afterPropertiesSet() throws IncompatibleConfigurationException {

    for (String categoryId : disabledCategories) {
      if (disabledCategoriesWithWhitelistedRules.containsKey(categoryId)) {
        throw new IncompatibleConfigurationException(
            """
    Category %s is entirely disabled but has whitelisted rules.
    Remove it from the disabledCategories list to allow specific
    rules or remove the disabledCategoriesWithWhitelistedRules config
    to disable all rules in the category
    """
                .formatted(categoryId));
      }

      for (String ruleId : disabledRules) {
        if (disabledCategoriesWithWhitelistedRules.values().stream()
            .anyMatch(list -> list.contains(ruleId))) {
          throw new IncompatibleConfigurationException(
              """
        Rule %s is disabled but is whitelisted in a category.
        Remove it from the disabledRules to allow it or remove it
        from the category in disabledCategoriesWithWhitelistedRules
        to disable it
        """
                  .formatted(ruleId));
        }
      }
    }
  }
}
