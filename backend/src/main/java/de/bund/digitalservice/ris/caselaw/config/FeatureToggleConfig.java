package de.bund.digitalservice.ris.caselaw.config;

import de.bund.digitalservice.ris.caselaw.adapter.UnleashService;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import io.getunleash.DefaultUnleash;
import io.getunleash.Unleash;
import io.getunleash.util.UnleashConfig;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import reactor.core.publisher.Mono;

@Configuration
public class FeatureToggleConfig {
  @Autowired private ConfigurableEnvironment environment;

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
  @Profile({"production", "staging"})
  public FeatureToggleService featureToggleService() {
    String env;
    if (Arrays.stream(environment.getActiveProfiles()).anyMatch("production"::equals)) {
      String sentryEnvironment =
          (String) environment.getSystemEnvironment().get("SENTRY_ENVIRONMENT");
      if (sentryEnvironment != null && sentryEnvironment.equals("uat")) {
        env = "uat";
      } else {
        env = "prod";
      }
    } else {
      env = "dev";
    }

    return new UnleashService(unleash(), env);
  }

  @Bean
  @Profile({"!production & !staging"})
  public FeatureToggleService featureToggleServiceMock() {
    return new FeatureToggleService() {
      @Override
      public List<String> getFeatureToggleNames() {
        return Collections.emptyList();
      }

      @Override
      public Mono<Boolean> isEnabled(String toggleName) {
        return Mono.just(true);
      }
    };
  }
}
