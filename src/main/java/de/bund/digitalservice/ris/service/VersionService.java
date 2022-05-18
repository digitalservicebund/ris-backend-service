package de.bund.digitalservice.ris.service;

import de.bund.digitalservice.ris.datamodel.VersionInfo;
import java.io.IOException;
import java.nio.file.Files;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class VersionService {

  public Mono<ResponseEntity<Object>> generateVersionInfo() {
    VersionInfo info = new VersionInfo();
    info.setVersion("0.0.1");
    String commitSha = "commit SHA not available";
    try {
      commitSha =
          Files.readAllLines(ResourceUtils.getFile("classpath:.commit-sha").toPath()).get(0);
    } catch (IOException ignored) {
    }
    info.setCommitSHA(commitSha);
    return Mono.just(info).map(ResponseEntity::ok);
  }
}
