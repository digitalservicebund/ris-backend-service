package de.bund.digitalservice.ris.caselaw.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.CourtRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.state.StateRepository;
import java.nio.ByteBuffer;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@Import(LookupTableImporterService.class)
class LookupTableImporterServiceTest {

  @SpyBean private LookupTableImporterService service;

  @MockBean private DocumentTypeRepository documentTypeRepository;

  @MockBean private JPADocumentTypeRepository jpaDocumentTypeRepository;

  @MockBean private CourtRepository courtRepository;

  @MockBean private StateRepository stateRepository;

  @Test
  void testImportDocumentTypeLookupTable() {
    when(documentTypeRepository.deleteAll()).thenReturn(Mono.empty());

    String doctypeXml =
        """
        <?xml version="1.0" encoding="utf-8"?>
        <juris-table>
          <juris-doktyp id="1" aendkz="N" version="1.0">
            <jurisabk>ÄN</jurisabk>
            <dokumentart>N</dokumentart>
            <mehrfach>Ja</mehrfach>
            <bezeichnung>Änderungsnorm</bezeichnung>
          </juris-doktyp>
        </juris-table>""";
    ByteBuffer byteBuffer = ByteBuffer.wrap(doctypeXml.getBytes());
    List<JPADocumentTypeDTO> documentTypeDTOs =
        List.of(
            JPADocumentTypeDTO.builder()
                .id(1L)
                .changeIndicator('N')
                .version("1.0")
                .jurisShortcut("ÄN")
                .documentType('N')
                .multiple("Ja")
                .label("Änderungsnorm")
                .build());

    StepVerifier.create(service.importDocumentTypeLookupTable(byteBuffer))
        .consumeNextWith(
            documentTypeDTO ->
                assertEquals(
                    "Successfully imported the document type lookup table", documentTypeDTO))
        .verifyComplete();

    verify(documentTypeRepository, never()).deleteAll();
    verify(documentTypeRepository, never()).saveAll(anyCollection());
    verify(jpaDocumentTypeRepository, atMostOnce()).deleteAll();
    verify(jpaDocumentTypeRepository, atMostOnce()).saveAll(documentTypeDTOs);
  }

  @Test
  void testImportCourtLookupTable() {
    when(courtRepository.deleteAll()).thenReturn(Mono.empty());

    String courtXml =
        """
            <?xml version="1.0"?>
            <juris-table>
                <juris-gericht id="1" version="1.0">
                    <gertyp>type123</gertyp>
                    <gerort>location123</gerort>
                    <spruchkoerper>
                        <name>Staatsanwaltschaft</name>
                    </spruchkoerper>
                </juris-gericht>
            </juris-table>
            """;
    ByteBuffer byteBuffer = ByteBuffer.wrap(courtXml.getBytes());

    StepVerifier.create(service.importCourtLookupTable(byteBuffer))
        .consumeNextWith(
            courtDTO -> assertEquals("Successfully imported the court lookup table", courtDTO))
        .verifyComplete();

    verify(courtRepository).deleteAll();
  }
}
