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
    VersionInfo info = new VersionInfo();
    info.setVersion("0.0.1");
    info.setCommitSHA(System.getenv("COMMIT_SHA"));
    return Mono.just(info).map(ResponseEntity::ok);
  }
}
