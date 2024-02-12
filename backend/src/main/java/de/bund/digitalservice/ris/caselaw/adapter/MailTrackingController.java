package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.MailTrackingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("admin")
@Slf4j
public class MailTrackingController {

  private final MailTrackingService service;

  @Autowired
  public MailTrackingController(MailTrackingService service) {
    this.service = service;
  }

  @PostMapping("/webhook")
  @PreAuthorize("permitAll")
  public Mono<ResponseEntity<String>> setPublishState(
      @RequestBody @Valid MailTrackingResponsePayload payload) {

    return service.updatePublishingState(payload.tags().get(0), payload.event());
  }
}
