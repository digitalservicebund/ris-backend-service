package de.bund.digitalservice.ris.caselaw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    return http.authorizeExchange()
        .pathMatchers("/actuator/**")
        .permitAll()
        .anyExchange()
        .authenticated()
        .and()
        .oauth2Login()
        .and()
        .exceptionHandling()
        .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED))
        .and()
        .csrf()
        .disable()
        .headers(
            headers ->
                headers.contentSecurityPolicy(
                    "default-src 'self'; img-src 'self' data:; style-src 'self' 'unsafe-inline'"))
        .build();
  }
}
