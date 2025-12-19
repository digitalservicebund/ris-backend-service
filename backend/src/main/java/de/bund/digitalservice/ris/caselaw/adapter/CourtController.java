package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.CourtService;
import de.bund.digitalservice.ris.caselaw.domain.court.Court;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/courts")
@Slf4j
public class CourtController {
  private final CourtService service;

  public CourtController(CourtService service) {
    this.service = service;
  }

  /**
   * Returns court objects in a list
   *
   * @param searchStr An optional search string, which filters the list.
   * @return a list of courts which contains the search string or the whole list if no search string
   *     is given
   */
  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<Court>> getCourts(
      @RequestParam(value = "q", required = false) String searchStr,
      @RequestParam(value = "sz", required = false, defaultValue = "200") Integer size) {
    return ResponseEntity.ok()
        .cacheControl(CacheControlDefaults.staticValues())
        .body(service.getCourts(searchStr, size));
  }

  /**
   * Returns branch locations for a specific court
   *
   * @param type the type of the court
   * @param location the location of the court
   * @return a list of branch locations for the court
   */
  @GetMapping("branchlocations")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<List<String>> getBranchLocationsForCourt(
      @RequestParam(value = "type") String type,
      @RequestParam(value = "location", required = false) String location) {
    return ResponseEntity.ok()
        .cacheControl(CacheControlDefaults.staticValues())
        .body(service.getBranchLocationsForCourt(type, location));
  }
}
