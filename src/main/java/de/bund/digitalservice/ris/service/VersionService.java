package de.bund.digitalservice.ris.service;

import de.bund.digitalservice.ris.datamodel.VersionInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class VersionService {

  public Mono<ResponseEntity<Object>> generateVersionInfo() {
    // TODO fill VersionInfo with correct values
    return Mono.just(new VersionInfo()).map(ResponseEntity::ok);
  }
}
