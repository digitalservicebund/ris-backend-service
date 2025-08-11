package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.User;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/users")
@Slf4j
public class UserController {
  private final UserService service;

  public UserController(UserService service) {
    this.service = service;
  }

  /**
   * @param oidcUser reads the user path
   * @return users from the same group
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public List<User> getUsers(
      @AuthenticationPrincipal OidcUser oidcUser,
      @RequestParam(value = "q") Optional<String> searchStr) {

    var users = service.getUsers(oidcUser);
    if (searchStr.isPresent() && !searchStr.get().isBlank()) {
      String search = searchStr.get();
      return users.stream()
          .filter(
              user ->
                  (user.email() != null && user.email().startsWith(search))
                      || (user.initials() != null && user.initials().startsWith(search))
                      || (user.name() != null && user.name().startsWith(search)))
          .toList();
    }

    return users;
  }
}
