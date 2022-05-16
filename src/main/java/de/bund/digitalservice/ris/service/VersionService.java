package de.bund.digitalservice.ris.service;

import de.bund.digitalservice.ris.datamodel.VersionInfo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class VersionService {

  @Value("${api.version.commit-sha-file:#{null}}")
  private Path COMMIT_SHA_FILE;

  public Mono<ResponseEntity<Object>> generateVersionInfo() {
    VersionInfo info = new VersionInfo();
    info.setVersion("0.0.1");
    String commitSha = "Commit SHA not available";
    if (Objects.nonNull(COMMIT_SHA_FILE) && COMMIT_SHA_FILE.toFile().exists()) {
      try {
        commitSha = Files.readString(COMMIT_SHA_FILE);
      } catch (IOException ignored) {
      }
    }
    info.setCommitSHA(commitSha);
    return Mono.just(info).map(ResponseEntity::ok);
  }
}
