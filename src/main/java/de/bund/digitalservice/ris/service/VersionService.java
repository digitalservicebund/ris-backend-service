package de.bund.digitalservice.ris.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class VersionService {

  public Mono<ResponseEntity<String>> generateVersionInfo() {
    // TODO JSON object
    return Mono.just("version 0.0.1").map(ResponseEntity::ok);
  }
}
