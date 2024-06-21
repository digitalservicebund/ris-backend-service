package de.bund.digitalservice.ris.caselaw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
  @Bean
  SecurityFilterChain web(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(
            customizer ->
                customizer
                    .requestMatchers(
                        "/actuator/**", "/admin/webhook", "/api/webjars/**", "/api/docs.*/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2Login(oauth2 -> oauth2.failureHandler(new RedirectingAuthenticationFailureHandler()))
        .exceptionHandling(
            httpSecurityExceptionHandlingConfigurer ->
                httpSecurityExceptionHandlingConfigurer.authenticationEntryPoint(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
        .csrf(AbstractHttpConfigurer::disable)
        .headers(
            httpSecurityHeadersConfigurer ->
                httpSecurityHeadersConfigurer
                    .contentSecurityPolicy(
                        contentSecurityPolicyConfig ->
                            contentSecurityPolicyConfig.policyDirectives(
                                "default-src 'self'; img-src 'self' data:; style-src 'self' 'unsafe-inline'; script-src 'self' 'unsafe-eval'; connect-src 'self' *.sentry.io data:"))
                    .contentTypeOptions(contentTypeOptionsConfig -> {})
                    .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                    .referrerPolicy(
                        referrerPolicyConfig ->
                            referrerPolicyConfig.policy(
                                ReferrerPolicyHeaderWriter.ReferrerPolicy
                                    .STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                    .permissionsPolicy(
                        permissionsPolicyConfig ->
                            permissionsPolicyConfig.policy(
                                "accelerometer=(), ambient-light-sensor=(), autoplay=(), battery=(), camera=(), cross-origin-isolated=(), "
                                    + "display-capture=(), document-domain=(), encrypted-media=(), execution-while-not-rendered=(), "
                                    + "execution-while-out-of-viewport=(), fullscreen=(), geolocation=(), gyroscope=(), keyboard-map=(), "
                                    + "magnetometer=(), microphone=(), midi=(), navigation-override=(), payment=(), picture-in-picture=(), "
                                    + "publickey-credentials-get=(), screen-wake-lock=(), sync-xhr=(), usb=(), web-share=(), xr-spatial-tracking=(), "
                                    + "clipboard-read=(self), clipboard-write=(self), gamepad=(), speaker-selection=(), conversion-measurement=(), "
                                    + "focus-without-user-activation=(self), hid=(), idle-detection=(), interest-cohort=(), serial=(), sync-script=(), "
                                    + "trust-token-redemption=(), window-placement=(), vertical-scroll=(self)")));

    return http.build();
  }
}
