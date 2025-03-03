package de.bund.digitalservice.ris.caselaw.adapter;

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/portal")
@Slf4j
public class PortalController {
  private final PublicPortalPublicationService publicPortalPublicationService;

  public PortalController(PublicPortalPublicationService publicPortalPublicationService) {

    this.publicPortalPublicationService = publicPortalPublicationService;
  }

  @DeleteMapping(value = "/{documentNumber}")
  @PreAuthorize("@userIsInternal.apply(#oidcUser)")
  public ResponseEntity<Object> removeLDMLFromPortalBucket(
      @AuthenticationPrincipal OidcUser oidcUser, @PathVariable String documentNumber) {

    List<String> deletedDocNumbers = new ArrayList<>();
    deletedDocNumbers.add(documentNumber);

    try {
      this.publicPortalPublicationService.deleteDocumentationUnit(documentNumber);
      this.publicPortalPublicationService.uploadChangelog(new ArrayList<>(), deletedDocNumbers);
      return ResponseEntity.noContent().build();
    } catch (Exception e) {
      log.error("Error deleting document '{}' from bucket", documentNumber, e);
      return ResponseEntity.internalServerError().build();
    }
  }
}
