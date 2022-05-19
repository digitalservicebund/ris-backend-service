package de.bund.digitalservice.ris.service;

import de.bund.digitalservice.ris.datamodel.VersionInfo;
import java.nio.file.Files;
import java.util.stream.Stream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class VersionService {

  @SneakyThrows
  public Mono<ResponseEntity<VersionInfo>> generateVersionInfo() {
    String commitShaFallback = "commit SHA not available";

    return Flux.using(
            () -> Files.lines(ResourceUtils.getFile("classpath:.commit-sha").toPath()),
            Flux::fromStream,
            Stream::close)
        .next()
        .defaultIfEmpty(commitShaFallback)
        .doOnError(e -> log.error(e.getMessage()))
        .onErrorResume(e -> Mono.just(commitShaFallback))
        .flatMap(line -> Mono.just(VersionInfo.builder().version("0.0.1").commitSHA(line).build()))
        .flatMap(versionInfo -> Mono.just(ResponseEntity.ok(versionInfo)));
  }
}
