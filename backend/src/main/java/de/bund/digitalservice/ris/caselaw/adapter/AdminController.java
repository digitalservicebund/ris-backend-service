package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.MailTrackingService;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/admin")
@Slf4j
public class AdminController {

  private final MailTrackingService mailTrackingService;
  private final EnvironmentService environmentService;
  private final InternalPortalPublicationService internalPortalPublicationService;

  @Autowired
  public AdminController(
      MailTrackingService mailTrackingService,
      EnvironmentService environmentService,
      InternalPortalPublicationService internalPortalPublicationService) {
    this.mailTrackingService = mailTrackingService;
    this.environmentService = environmentService;
    this.internalPortalPublicationService = internalPortalPublicationService;
  }

  @PostMapping("/webhook")
  @PreAuthorize("permitAll")
  public ResponseEntity<String> trackMail(@RequestBody @Valid MailTrackingResponsePayload payload) {
    if (payload != null && payload.tags() != null && !payload.tags().isEmpty()) {
      return mailTrackingService.processMailSendingState(payload.tags().get(0), payload.event());
    }
    return ResponseEntity.badRequest().build();
  }

  @GetMapping("/env")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<String> getEnvironment() {
    return ResponseEntity.ok(environmentService.getEnvironment());
  }

  @GetMapping("/ldml/samples")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<Void> createSampleLdmls() {
    try {
      internalPortalPublicationService.exportSampleLdmls();
    } catch (IOException ex) {
      log.error("Couldn't generate zip file for sample documentation units.", ex);
      return ResponseEntity.internalServerError().build();
    }

    return ResponseEntity.ok().build();
  }

  @GetMapping("/accountManagementUrl")
  @PreAuthorize("isAuthenticated()")
  public ResponseEntity<String> getAccountManagementUrl() {
    return ResponseEntity.ok(environmentService.getAccountManagementUrl());
  }
}
