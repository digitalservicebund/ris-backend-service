package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.EmailPublishState;
import de.bund.digitalservice.ris.caselaw.domain.MailTrackingService;
import de.bund.digitalservice.ris.norms.framework.adapter.input.restapi.OpenApiConfiguration;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
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
    UUID documentUnitUuid;
    try {
      documentUnitUuid = UUID.fromString(payload.tags().get(0));
    } catch (IllegalArgumentException e) {
      // We're not responsible for other sent mails
      return Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).build());
    }
    EmailPublishState emailPublishState = service.getMappedPublishState(payload.event());

    return service
        .setPublishState(documentUnitUuid, emailPublishState)
        .map(
            uuid -> ResponseEntity.status(HttpStatus.OK).body("Publish state was set successfully"))
        .defaultIfEmpty(
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("Publish state could not be set: invalid payload"))
        .onErrorReturn(
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Publish state could not be set"));
  }
}
