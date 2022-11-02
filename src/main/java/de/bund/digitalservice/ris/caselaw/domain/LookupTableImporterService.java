package de.bund.digitalservice.ris.caselaw.domain;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.CourtsXML;
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

  private final DocumentTypeRepository documentTypeRepository;

  public LookupTableImporterService(DocumentTypeRepository documentTypeRepository) {
    this.documentTypeRepository = documentTypeRepository;
  }

  public Mono<String> importDocumentTypeLookupTable(ByteBuffer byteBuffer) {
    XmlMapper mapper = new XmlMapper();
    DocumentTypesXML documentTypesXML;
    try {
      documentTypesXML = mapper.readValue(byteBuffer.array(), DocumentTypesXML.class);
    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.NOT_ACCEPTABLE, "Could not map ByteBuffer-content to DocumentTypesXML", e);
    }

    documentTypeRepository.deleteAll().subscribe();

    documentTypesXML
        .getList()
        .forEach(
            documentTypeXML ->
                documentTypeRepository
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

    return Mono.just("Successfully imported the document type lookup table");
  }

  public Mono<String> importCourtLookupTable(ByteBuffer byteBuffer) {
    XmlMapper mapper = new XmlMapper();
    CourtsXML courtsXML;
    try {
      courtsXML = mapper.readValue(byteBuffer.array(), CourtsXML.class);
    } catch (IOException e) {
      throw new ResponseStatusException(
          HttpStatus.NOT_ACCEPTABLE, "Could not map ByteBuffer-content to DocumentTypesXML", e);
    }

    System.out.println(courtsXML);

    // TODO

    return Mono.just("Successfully imported the document type lookup table");
  }
}
