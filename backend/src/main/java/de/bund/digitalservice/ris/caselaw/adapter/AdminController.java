package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.MailTrackingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/admin")
@Slf4j
public class AdminController {

  private final MailTrackingService mailTrackingService;
  private final EnvService envService;

  @Autowired
  public AdminController(MailTrackingService mailTrackingService, EnvService envService) {
    this.mailTrackingService = mailTrackingService;
    this.envService = envService;
  }

  @PostMapping("/webhook")
  @PreAuthorize("permitAll")
  public Mono<ResponseEntity<String>> setPublishState(
      @RequestBody @Valid MailTrackingResponsePayload payload) {
    if (payload != null && payload.tags() != null && !payload.tags().isEmpty()) {
      return Mono.just(
          mailTrackingService.updatePublishingState(payload.tags().get(0), payload.event()));
    }
    return Mono.just(ResponseEntity.badRequest().build());
  }

  @GetMapping("/env")
  @PreAuthorize("isAuthenticated()")
  public Mono<ResponseEntity<String>> getEnv() {
    return envService.getEnv().map(ResponseEntity::ok);
  }
}
