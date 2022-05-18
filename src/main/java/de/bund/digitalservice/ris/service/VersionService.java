package de.bund.digitalservice.ris.service;

import de.bund.digitalservice.ris.datamodel.VersionInfo;
import java.io.File;
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
    String commitSha = "Commit SHA is not available";
    try {
      File file = ResourceUtils.getFile("classpath:.commit-sha");
      commitSha = "Error when trying to read commit SHA file";
      commitSha = Files.readAllLines(file.toPath()).get(0);
    } catch (IOException e) {
      commitSha = "Error when trying to read commit SHA";
    }
    info.setCommitSHA(commitSha);
    return Mono.just(info).map(ResponseEntity::ok);
  }
}
