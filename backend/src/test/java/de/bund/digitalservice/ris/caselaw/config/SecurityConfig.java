package de.bund.digitalservice.ris.caselaw.config;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockOidcLogin;

import java.util.Collections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.OidcLoginMutator;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  public static OidcLoginMutator getMockLogin() {
    return mockOidcLogin()
        .idToken(
            token ->
                token.claims(
                    claims -> {
                      claims.put("groups", Collections.singletonList("/DigitalService"));
                      claims.put("name", "testUser");
                    }));
  }

  @Bean
  @Primary
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    return http.authorizeExchange()
        .anyExchange()
        .permitAll()
        .and()
        .csrf()
        .disable()
        .headers(
            headers ->
                headers.contentSecurityPolicy(
                    "default-src 'self'; img-src 'self' data:; style-src 'self' 'unsafe-inline'"))
        .build();
  }

  @Bean
  public AuthenticationManager
      authenticationManager() { // to delete default username and password that is printed in the
    // log every time, you can provide here any auth manager
    // (InMemoryAuthenticationManager, etc) as you need
    return authentication -> {
      throw new UnsupportedOperationException();
    };
  }
}
