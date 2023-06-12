package de.bund.digitalservice.ris.caselaw.adapter;

import static de.bund.digitalservice.ris.caselaw.domain.DocumentUnitStatus.PUBLISHED;

import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
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

  public Function<String, Mono<Boolean>> userHasReadAccess() {
    return documentNumber ->
        Mono.defer(
            () ->
                documentUnitService
                    .getByDocumentNumber(documentNumber)
                    .flatMap(
                        documentUnit ->
                            ReactiveSecurityContextHolder.getContext()
                                .map(SecurityContext::getAuthentication)
                                .map(Authentication::getPrincipal)
                                .flatMap(
                                    principal -> {
                                      if (documentUnit.status() == PUBLISHED
                                          || documentUnit.status() == null) {
                                        return Mono.just(true);
                                      }
                                      return userService
                                          .getDocumentationOffice((OidcUser) principal)
                                          .map(
                                              userOffice ->
                                                  documentUnit
                                                      .coreData()
                                                      .documentationOffice()
                                                      .equals(userOffice));
                                    }))
                    .switchIfEmpty(Mono.just(false))
                    .onErrorReturn(false));
  }
}
