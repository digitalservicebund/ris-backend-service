package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.EventRecord;
import de.bund.digitalservice.ris.caselaw.domain.HandoverEntityType;
import de.bund.digitalservice.ris.caselaw.domain.HandoverMail;
import de.bund.digitalservice.ris.caselaw.domain.HandoverService;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEdition;
import de.bund.digitalservice.ris.caselaw.domain.LegalPeriodicalEditionService;
import de.bund.digitalservice.ris.caselaw.domain.XmlTransformationResult;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/caselaw/legalperiodicaledition")
@Slf4j
public class LegalPeriodicalEditionController {
  private final LegalPeriodicalEditionService service;

  private final HandoverService handoverService;

  public LegalPeriodicalEditionController(
      LegalPeriodicalEditionService service, HandoverService handoverService) {
    this.service = service;
    this.handoverService = handoverService;
  }

  @GetMapping(value = "/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<LegalPeriodicalEdition> getById(@NonNull @PathVariable UUID uuid) {
    try {
      return ResponseEntity.ok(service.getById(uuid).orElseThrow());
    } catch (NoSuchElementException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public List<LegalPeriodicalEdition> getLegalPeriodicals(
      @RequestParam(value = "legal_periodical_id") UUID legalPeriodicalId) {
    return service.getLegalPeriodicalEditions(legalPeriodicalId);
  }

  @PutMapping(
      consumes = MediaType.APPLICATION_JSON_VALUE,
      produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public LegalPeriodicalEdition save(
      @Valid @RequestBody LegalPeriodicalEdition legalPeriodicalEdition) {
    return service.saveLegalPeriodicalEdition(legalPeriodicalEdition);
  }

  @DeleteMapping(value = "/{editionId}")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> delete(@NonNull @PathVariable UUID editionId) {
    var deleted = service.delete(editionId);
    if (deleted) {
      return ResponseEntity.ok().build();
    }
    return ResponseEntity.noContent().build();
  }

  /**
   * Hands over the references of the edition to jDV as XML via email.
   *
   * @param uuid UUID of the documentation unit
   * @param oidcUser the logged-in user, used to forward the response email
   * @return the email sent containing the XML or an empty response with status code 400 * if the
   *     user is not authorized
   */
  @PutMapping(value = "/{uuid}/handover", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<HandoverMail> handoverEditionAsMail(
      @PathVariable UUID uuid, @AuthenticationPrincipal OidcUser oidcUser) {

    // TODO
    return ResponseEntity.ok(
        HandoverMail.builder()
            .entityId(uuid)
            .entityType(HandoverEntityType.EDITION)
            .success(false)
            .build());
  }

  /**
   * Get all events of a edition (can be handover events, received handover reports,
   * import/migration events)
   *
   * @param uuid id of the edition
   * @return ordered list of event records (newest first) or an empty response with status code 400
   *     if the user is not authorized
   */
  @GetMapping(value = "/{uuid}/handover", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public List<EventRecord> getEventLog(@PathVariable UUID uuid) {
    return handoverService.getEventLog(uuid, HandoverEntityType.EDITION);
  }

  /**
   * Get the XML preview of an edition.
   *
   * @param uuid id of the edition
   * @return the XML preview or an empty response with status code 400 if the user is not authorized
   *     or an empty response if the documentation unit does not exist
   */
  @GetMapping(value = "/{uuid}/preview-xml", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("isAuthenticated()")
  public List<XmlTransformationResult> getXmlPreview(@PathVariable UUID uuid) {
    try {
      return handoverService.createEditionPreviewXml(uuid);
    } catch (IOException e) {
      return List.of();
    }
  }
}
