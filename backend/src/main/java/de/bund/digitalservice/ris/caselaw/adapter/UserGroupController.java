package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentationOfficeUserGroup;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/user-group")
@Slf4j
public class UserGroupController {
  private final KeycloakUserService service;

  public UserGroupController(KeycloakUserService service) {
    this.service = service;
  }

  /**
   * Returns user groups for the doc office of the current user that are not internal.
   *
   * @param oidcUser The user.
   * @return A list of external user groups.
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public List<DocumentationOfficeUserGroup> getUserGroups(
      @AuthenticationPrincipal OidcUser oidcUser) {
    return service.getUserGroups(oidcUser);
  }
}
