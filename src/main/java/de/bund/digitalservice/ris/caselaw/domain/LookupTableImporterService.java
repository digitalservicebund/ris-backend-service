package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.DocumentTypesXML;
import java.io.IOException;
import java.nio.ByteBuffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class LookupTableImporterService {

  public Mono<String> importLookupTable(ByteBuffer byteBuffer) {
    XmlMapper mapper = new XmlMapper();
    DocumentTypesXML documentTypesXML;
    try {
      documentTypesXML = mapper.readValue(byteBuffer.array(), DocumentTypesXML.class);
    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.NOT_ACCEPTABLE, "Could not map ByteBuffer-content to DocumentTypesXML", e);
    }

    System.out.println(documentTypesXML);
    // TODO ...

    return Mono.just("Successfully imported the lookup table");
  }
}
