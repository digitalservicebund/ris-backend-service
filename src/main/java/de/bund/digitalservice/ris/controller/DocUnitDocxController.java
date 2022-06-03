package de.bund.digitalservice.ris.controller;

import de.bund.digitalservice.ris.datamodel.docx.Docx2Html;
import de.bund.digitalservice.ris.service.DocxConverterService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/docunitdocx")
public class DocUnitDocxController {

  private final DocxConverterService service;

  public DocUnitDocxController(DocxConverterService service) {
    Assert.notNull(service, "docx converter service is null");

    this.service = service;
  }

  @GetMapping
  public Mono<ResponseEntity<List<String>>> get() {
    return service.getDocxFiles();
  }

  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<Docx2Html> html(@PathVariable String id) {
    return service.getHtml(id);
  }
}
