package de.bund.digitalservice.ris.caselaw.adapter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/user-group")
@Slf4j
public class UserGroupController {

  public UserGroupController() {}

  /** Returns all users groups for the doc office of the current user */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public List<Map<String, String>> getUserGroups() {
    return Arrays.asList(
        Map.of("id", "1", "name", "Extern/Miotke"),
        Map.of("id", "2", "name", "Extern/Busenks"),
        Map.of("id", "3", "name", "Intern"));
  }
}
