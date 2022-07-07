package de.bund.digitalservice.ris.adapter;

import de.bund.digitalservice.ris.domain.DocxConverterService;
import de.bund.digitalservice.ris.domain.docx.Docx2Html;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
