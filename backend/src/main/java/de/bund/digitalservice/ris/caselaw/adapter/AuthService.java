package de.bund.digitalservice.ris.caselaw.adapter;

import static de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus.PUBLISHED;

import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AuthService {

  private final UserService userService;
  private final DocumentUnitService documentUnitService;

  public AuthService(UserService userService, DocumentUnitService documentUnitService) {
    this.userService = userService;
    this.documentUnitService = documentUnitService;
  }

  @Bean
  public Function<String, Mono<Boolean>> userHasReadAccess() {
    return documentNumber ->
        Mono.defer(
            () ->
                documentUnitService
                    .getByDocumentNumber(documentNumber)
                    .flatMap(
                        documentUnit -> {
                          if (documentUnit.status() == PUBLISHED) {
                            return Mono.just(true);
                          } else {
                            return ReactiveSecurityContextHolder.getContext()
                                .map(SecurityContext::getAuthentication)
                                .map(Authentication::getPrincipal)
                                .flatMap(
                                    principal ->
                                        userService
                                            .getDocumentationOffice((OidcUser) principal)
                                            .map(
                                                userOffice ->
                                                    documentUnit
                                                        .coreData()
                                                        .documentationOffice()
                                                        .equals(userOffice)))
                                .defaultIfEmpty(false);
                          }
                        })
                    .defaultIfEmpty(false)
                    .onErrorReturn(false));
  }
}
