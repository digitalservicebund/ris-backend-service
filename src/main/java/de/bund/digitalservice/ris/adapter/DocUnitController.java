package de.bund.digitalservice.ris.adapter;

import de.bund.digitalservice.ris.domain.DocUnit;
import de.bund.digitalservice.ris.domain.DocUnitCreationInfo;
import de.bund.digitalservice.ris.domain.DocUnitService;
import de.bund.digitalservice.ris.domain.ExportObject;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

@RestController
@RequestMapping("api/v1/docunits")
@Slf4j
public class DocUnitController {
  private final DocUnitService service;

  public DocUnitController(DocUnitService service) {
    this.service = service;
  }

  @PostMapping(value = "")
  public Mono<ResponseEntity<DocUnit>> generateNewDocUnit(
      @RequestBody DocUnitCreationInfo docUnitCreationInfo) {
    return service
        .generateNewDocUnit(docUnitCreationInfo)
        .retryWhen(Retry.backoff(5, Duration.ofSeconds(2)).jitter(0.75))
        .map(docUnit -> ResponseEntity.status(HttpStatus.CREATED).body(docUnit))
        .onErrorReturn(ResponseEntity.internalServerError().body(DocUnit.EMPTY));
  }

  @PutMapping(value = "/{uuid}/file")
  public Mono<ResponseEntity<DocUnit>> attachFileToDocUnit(
      @PathVariable UUID uuid,
      @RequestBody Flux<ByteBuffer> byteBufferFlux,
      @RequestHeader HttpHeaders httpHeaders) {

    return service.attachFileToDocUnit(uuid, byteBufferFlux, httpHeaders);
  }

  @DeleteMapping(value = "/{uuid}/file")
  public Mono<ResponseEntity<DocUnit>> removeFileFromDocUnit(@PathVariable UUID uuid) {

    return service.removeFileFromDocUnit(uuid);
  }

  @GetMapping(value = "")
  public Mono<ResponseEntity<Flux<DocUnit>>> getAll() {
    log.info("All DocUnits were requested");

    return service.getAll();
  }

  @GetMapping(value = "/{documentnumber}")
  public Mono<ResponseEntity<DocUnit>> getByDocumentnumber(
      @NonNull @PathVariable String documentnumber) {
    if (documentnumber.length() != 14) {
      return Mono.just(ResponseEntity.unprocessableEntity().body(DocUnit.EMPTY));
    }
    return service.getByDocumentnumber(documentnumber);
  }

  @DeleteMapping(value = "/{uuid}")
  public Mono<ResponseEntity<String>> deleteByUuid(@PathVariable UUID uuid) {

    return service.deleteByUuid(uuid);
  }

  @PutMapping(value = "/{uuid}/docx", consumes = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ResponseEntity<DocUnit>> updateByUuid(
      @PathVariable UUID uuid, @RequestBody DocUnit docUnit) {
    if (!uuid.equals(docUnit.getUuid())) {
      return Mono.just(ResponseEntity.unprocessableEntity().body(DocUnit.EMPTY));
    }
    return service.updateDocUnit(docUnit);
  }

  @PutMapping(value = "/{uuid}/publish", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ResponseEntity<ExportObject>> publishDocumentUnit(@PathVariable UUID uuid) {
    return service
        .publish(uuid)
        .map(ResponseEntity::ok)
        .doOnError(ex -> ResponseEntity.internalServerError().build());
  }

  @GetMapping(value = "/{uuid}/publish", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ResponseEntity<ExportObject>> getLastPublishedXml(@PathVariable UUID uuid) {
    return service
        .getLastPublishedXml(uuid)
        .map(ResponseEntity::ok)
        .doOnError(ex -> ResponseEntity.internalServerError().build());
  }
}
