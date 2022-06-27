package de.bund.digitalservice.ris.adapter;

import de.bund.digitalservice.ris.domain.DocUnit;
import de.bund.digitalservice.ris.domain.DocUnitCreationInfo;
import de.bund.digitalservice.ris.domain.DocUnitService;
import java.nio.ByteBuffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;
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

@RestController
@RequestMapping("api/v1/docunits")
@Slf4j
public class DocUnitController {
  private final DocUnitService service;

  public DocUnitController(DocUnitService service) {
    Assert.notNull(service, "DocUnitService is null");

    this.service = service;
  }

  // only general check, the service will check if such a doc unit exists
  private boolean isInvalidId(String id) {
    try {
      Long.parseLong(id);
      return false;
    } catch (NumberFormatException e) {
      return true;
    }
  }

  @PostMapping(value = "")
  public Mono<ResponseEntity<DocUnit>> generateNewDocUnit(
      @RequestBody DocUnitCreationInfo docUnitCreationInfo) {
    return service
        .generateNewDocUnit(docUnitCreationInfo)
        .map(docUnit -> ResponseEntity.status(HttpStatus.CREATED).body(docUnit))
        .onErrorReturn(ResponseEntity.internalServerError().body(DocUnit.EMPTY));
  }

  @PutMapping(value = "/{id}/file")
  public Mono<ResponseEntity<DocUnit>> attachFileToDocUnit(
      @PathVariable String id,
      @RequestBody Flux<ByteBuffer> byteBufferFlux,
      @RequestHeader HttpHeaders httpHeaders) {
    if (isInvalidId(id)) {
      return Mono.just(ResponseEntity.unprocessableEntity().body(DocUnit.EMPTY));
    }

    return service.attachFileToDocUnit(id, byteBufferFlux, httpHeaders);
  }

  @DeleteMapping(value = "/{id}/file")
  public Mono<ResponseEntity<DocUnit>> removeFileFromDocUnit(@PathVariable String id) {
    if (isInvalidId(id)) {
      return Mono.just(ResponseEntity.unprocessableEntity().body(DocUnit.EMPTY));
    }
    return service.removeFileFromDocUnit(id);
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

  @DeleteMapping(value = "/{id}")
  public Mono<ResponseEntity<String>> deleteById(@PathVariable String id) {
    if (isInvalidId(id)) {
      return Mono.just(ResponseEntity.unprocessableEntity().body("invalid DocUnit id"));
    }
    return service.deleteById(id);
  }

  @PutMapping(value = "/{id}/docx", consumes = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ResponseEntity<DocUnit>> updateById(
      @PathVariable String id, @RequestBody DocUnit docUnit) {
    if (isInvalidId(id) || !Long.valueOf(id).equals(docUnit.getId())) {
      return Mono.just(ResponseEntity.unprocessableEntity().body(DocUnit.EMPTY));
    }
    return service.updateDocUnit(docUnit);
  }
}
