package de.bund.digitalservice.ris.controller;

import de.bund.digitalservice.ris.datamodel.DocUnit;
import de.bund.digitalservice.ris.service.DocUnitService;
import java.nio.ByteBuffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
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

  @PostMapping(value = "")
  public Mono<ResponseEntity<DocUnit>> generateNewDocUnit() {

    return service.generateNewDocUnit();
  }

  @PutMapping(value = "/{id}/file")
  public Mono<ResponseEntity<DocUnit>> attachFileToDocUnit(
      @PathVariable int id,
      @RequestBody Flux<ByteBuffer> byteBufferFlux,
      @RequestHeader HttpHeaders httpHeaders) {

    return service.attachFileToDocUnit(id, byteBufferFlux, httpHeaders);
  }

  @GetMapping(value = "")
  public Mono<ResponseEntity<Flux<DocUnit>>> getAll() {
    log.info("All DocUnits were requested");

    return service.getAll();
  }

  @GetMapping(value = "/{id}")
  public Mono<ResponseEntity<DocUnit>> getById(@PathVariable int id) {
    return service.getById(id);
  }

  @PutMapping(value = "/{id}/docx", consumes = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ResponseEntity<DocUnit>> updateById(
      @PathVariable int id, @RequestBody DocUnit docUnit) {
    if (id != docUnit.getId()) {
      return Mono.just(ResponseEntity.internalServerError().body(DocUnit.EMPTY));
    }
    return service.updateDocUnit(docUnit);
  }
}
