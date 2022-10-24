package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.DocxConverterService;
import de.bund.digitalservice.ris.caselaw.domain.docx.Docx2Html;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/documentunitdocx")
public class DocumentUnitDocxController {

  private final DocxConverterService service;

  public DocumentUnitDocxController(DocxConverterService service) {
    this.service = service;
  }

  @GetMapping
  public Mono<ResponseEntity<List<String>>> get() {
    return service.getDocxFiles().map(ResponseEntity::ok);
  }

  @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ResponseEntity<Docx2Html>> html(@PathVariable String id) {
    return service
        .getHtml(id)
        .map(ResponseEntity::ok)
        .onErrorReturn(ResponseEntity.internalServerError().build());
  }
}
