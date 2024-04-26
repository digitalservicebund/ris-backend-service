package de.bund.digitalservice.ris.caselaw.config;

import de.bund.digitalservice.ris.caselaw.adapter.UnleashService;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import io.getunleash.DefaultUnleash;
import io.getunleash.Unleash;
import io.getunleash.util.UnleashConfig;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;

@Configuration
public class FeatureToggleConfig {
  @Value("${unleash.appName:unleash-proxy}")
  private String appName;

  @Value("${unleash.apiUrl:}")
  private String apiUrl;

  @Value("${unleash.apiToken:}")
  private String apiToken;

  public Unleash unleash() {
    UnleashConfig config =
        UnleashConfig.builder().appName(appName).unleashAPI(apiUrl).apiKey(apiToken).build();

    return new DefaultUnleash(config);
  }

  @Bean
  @Profile({"production", "uat", "staging"})
  public FeatureToggleService featureToggleService(ConfigurableEnvironment environment) {
    String env;
    if (Arrays.asList(environment.getActiveProfiles()).contains("production")) {
      env = "prod";
    } else if (Arrays.asList(environment.getActiveProfiles()).contains("uat")) {
      env = "uat";
    } else {
      env = "dev";
    }

    return new UnleashService(unleash(), env);
  }

  @Bean
  @Profile({"!production & !staging & !uat"})
  public FeatureToggleService featureToggleServiceMock() {
    return new FeatureToggleService() {
      @Override
      public List<String> getFeatureToggleNames() {
        return Collections.emptyList();
      }

      @Override
      public boolean isEnabled(String toggleName) {
        return true;
      }
    };
  }
}
