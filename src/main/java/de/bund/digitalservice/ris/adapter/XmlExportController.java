package de.bund.digitalservice.ris.adapter;

import de.bund.digitalservice.ris.domain.XmlExportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/xmlexport")
public class XmlExportController {
  private final XmlExportService service;

  public XmlExportController(XmlExportService service) {
    this.service = service;
  }

  @GetMapping(value = "juris")
  public Mono<ResponseEntity<String>> exportJurisXml() {
    return service.exportJurisXml();
  }
}
