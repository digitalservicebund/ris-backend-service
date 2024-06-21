package de.bund.digitalservice.ris.caselaw.config;

import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Profile("local")
public class WebMvcConfiguration implements WebMvcConfigurer {
  //  private final ObjectMapper objectMapper;

  //  public WebMvcConfiguration(ObjectMapper objectMapper) {
  //    this.objectMapper = objectMapper;
  //  }

  @Override
  public void addCorsMappings(CorsRegistry corsRegistry) {
    corsRegistry.addMapping("/**").allowedOrigins("*").allowedMethods("*");
  }

  //  @Bean
  //  SecurityFilterChain web(HttpSecurity http) throws Exception {
  //    http.authorizeHttpRequests(
  //        customizer -> customizer.anyRequest().permitAll().anyRequest().authenticated());
  //    return http.build();
  //  }

  //  @Override
  //  public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
  //    configurer.defaultCodecs().maxInMemorySize(-1);
  //    configurer.defaultCodecs().jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper));
  //    configurer.defaultCodecs().jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper));
  //  }
}
