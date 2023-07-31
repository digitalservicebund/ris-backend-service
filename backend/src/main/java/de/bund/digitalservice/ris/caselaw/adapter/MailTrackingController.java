package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.EmailPublishState;
import de.bund.digitalservice.ris.caselaw.domain.MailTrackingService;
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.OpenApiConfiguration;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
@Tag(name = OpenApiConfiguration.CASELAW_TAG)
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
    try {
      UUID documentUnitUuid = UUID.fromString(payload.tags().get(0));
      return service.updatePublishingState(documentUnitUuid, payload.event());

    } catch (IllegalArgumentException e) {
      // No UUID in tag == it's about a forwarded report mail and not the mail to juris
      if (service.getMappedPublishState(payload.event()) == EmailPublishState.ERROR) {
        log.error("Received Mail sending error {} with tags {}", payload.event(), payload.tags());
      }
      return Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).build());
    }
  }
}
