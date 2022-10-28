package de.bund.digitalservice.ris.caselaw.adapter;

import de.bund.digitalservice.ris.caselaw.domain.LookupTableImporterService;
import java.nio.ByteBuffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("api/v1/lookuptableimporter")
@Slf4j
public class LookupTableImporterController {

  private final LookupTableImporterService service;

  public LookupTableImporterController(LookupTableImporterService service) {
    this.service = service;
  }

  // in Postman go to "Body", select "raw" and "XML" and paste the XML-contents
  @PutMapping(value = "doktyp")
  public Mono<ResponseEntity<String>> importLookupTable(@RequestBody ByteBuffer byteBuffer) {
    return service
        .importLookupTable(byteBuffer)
        .map(resultString -> ResponseEntity.status(HttpStatus.OK).body(resultString))
        .onErrorReturn(
            ResponseEntity.internalServerError().body("Could not import the lookup table"));
  }
}
