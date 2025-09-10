package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import de.bund.digitalservice.ris.caselaw.domain.UserGroup;
import de.bund.digitalservice.ris.caselaw.domain.UserGroupService;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import java.util.List;
import java.util.Optional;
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
  private final UserGroupService service;
  private final UserService userService;

  public UserGroupController(UserGroupService service, UserService userService) {
    this.service = service;
    this.userService = userService;
  }

  /**
   * Returns user groups for the doc office of the current user that are not internal.
   *
   * @param oidcUser current user via openid connect system
   * @return A list of external user groups.
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public List<UserGroup> getUserGroups(@AuthenticationPrincipal OidcUser oidcUser) {
    Optional<DocumentationOffice> office = userService.getDocumentationOffice(oidcUser);
    if (office.isEmpty()) {
      log.atError()
          .setMessage("Could not identify logged in user or their documentation office")
          .addKeyValue("name", oidcUser.getName())
          .log();
      return List.of();
    }
    return service.getExternalUserGroups(office.get());
  }
}
