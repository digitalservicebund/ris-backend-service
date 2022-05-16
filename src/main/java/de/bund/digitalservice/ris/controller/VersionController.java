package de.bund.digitalservice.ris.controller;

import de.bund.digitalservice.ris.service.VersionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1")
@Slf4j
public class VersionController {
  private final VersionService service;

  public VersionController(VersionService service) {
    Assert.notNull(service, "VersionService is null");

    this.service = service;
  }

  @GetMapping(value = "version")
  public Mono<ResponseEntity<Object>> getVersion() {
    log.info("version info requested");

    return service.generateVersionInfo();
  }
}
