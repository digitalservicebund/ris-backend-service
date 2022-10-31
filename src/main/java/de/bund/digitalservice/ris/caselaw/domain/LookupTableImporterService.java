package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.DocumentTypeRepository;
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

  private final DocumentTypeRepository repository;

  public LookupTableImporterService(DocumentTypeRepository repository) {
    this.repository = repository;
  }

  public Mono<String> importLookupTable(ByteBuffer byteBuffer) {
    XmlMapper mapper = new XmlMapper();
    DocumentTypesXML documentTypesXML;
    try {
      documentTypesXML = mapper.readValue(byteBuffer.array(), DocumentTypesXML.class);
    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.NOT_ACCEPTABLE, "Could not map ByteBuffer-content to DocumentTypesXML", e);
    }

    repository.deleteAll().subscribe();

    documentTypesXML
        .getList()
        .forEach(
            documentTypeXML ->
                repository
                    .save(
                        DocumentTypeDTO.builder()
                            .id(documentTypeXML.getId())
                            .changeDateClient(documentTypeXML.getChangeDateClient())
                            .changeIndicator(documentTypeXML.getChangeIndicator())
                            .version(documentTypeXML.getVersion())
                            .jurisShortcut(documentTypeXML.getJurisShortcut())
                            .documentType(documentTypeXML.getDocumentType())
                            .multiple(documentTypeXML.getMultiple())
                            .label(documentTypeXML.getLabel())
                            .superlabel1(documentTypeXML.getSuperlabel1())
                            .superlabel2(documentTypeXML.getSuperlabel2())
                            .build())
                    .subscribe());

    return Mono.just("Successfully imported the lookup table");
  }
}
