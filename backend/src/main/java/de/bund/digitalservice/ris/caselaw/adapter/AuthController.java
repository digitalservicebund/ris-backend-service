package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.ApiKey;
import de.bund.digitalservice.ris.caselaw.domain.ImportApiKeyException;
import de.bund.digitalservice.ris.caselaw.domain.User;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

  private final UserService userService;
  private final AuthService authService;

  public AuthController(UserService userService, AuthService authService) {
    this.userService = userService;
    this.authService = authService;
  }

  @GetMapping(value = "me")
  @PreAuthorize("isAuthenticated()")
  public Mono<ResponseEntity<User>> getUser(@AuthenticationPrincipal OidcUser oidcUser) {

    return userService.getUser(oidcUser).map(ResponseEntity::ok);
  }

  /**
   * Get the api key for the importer and the user.
   *
   * @param oidcUser current user via openid connect system
   * @return the last/current api key for the user or null if no api key exist
   */
  @GetMapping(value = "api-key/import")
  @PreAuthorize("isAuthenticated()")
  public Mono<ResponseEntity<ApiKey>> getImportApiKey(@AuthenticationPrincipal OidcUser oidcUser) {
    ApiKey apiKey = authService.getImportApiKey(oidcUser);
    return Mono.just(ResponseEntity.ok(apiKey));
  }

  /**
   * Generate an api key for the importer and the user. <br>
   * Returns a bad response (HTTP status code: 400) if the generation throws an {@link
   * ImportApiKeyException}. This will happen if a valid key for the user exist.
   *
   * @param oidcUser current user via openid connect system
   * @return the new generated api key
   */
  @PutMapping(value = "api-key/import")
  @PreAuthorize("isAuthenticated()")
  public Mono<ResponseEntity<ApiKey>> generateImportApiKey(
      @AuthenticationPrincipal OidcUser oidcUser) {
    try {
      ApiKey apiKey = authService.generateImportApiKey(oidcUser);
      return Mono.just(ResponseEntity.ok(apiKey));
    } catch (ImportApiKeyException ignored) {
    }

    return Mono.just(ResponseEntity.badRequest().build());
  }

  /**
   * Invalidate an existing api key for the importer and the user. <br>
   * Returns a bad response (HTTP status code: 400) if no api key in the header or the key doesn't
   * exist or doesn't belong to the user.
   *
   * @param oidcUser current user via openid connect system
   * @param request http request to get the header information
   * @return the last/current api key for the user
   */
  @PostMapping(value = "api-key/import/invalidate")
  @PreAuthorize("isAuthenticated()")
  public Mono<ResponseEntity<ApiKey>> invalidateImportApiKey(
      @AuthenticationPrincipal OidcUser oidcUser, ServerHttpRequest request) {
    String apiKey = request.getHeaders().getFirst("X-API-KEY");

    if (apiKey == null) {
      return Mono.just(ResponseEntity.badRequest().build());
    }

    try {
      ApiKey lastApiKey = authService.invalidateImportApiKey(oidcUser, apiKey);
      return Mono.just(ResponseEntity.ok(lastApiKey));
    } catch (ImportApiKeyException ignored) {
    }

    return Mono.just(ResponseEntity.badRequest().build());
  }
}
