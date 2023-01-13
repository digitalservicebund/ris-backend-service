package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.LookupTableImporterService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateRepository;
import java.nio.ByteBuffer;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
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

    String doctypesXml =
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
    ByteBuffer byteBuffer = ByteBuffer.wrap(doctypesXml.getBytes());
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

  @Captor private ArgumentCaptor<List<CourtDTO>> courtDTOlistCaptor;

  @Test
  void testImportCourtLookupTable() {
    List<CourtDTO> courtsDTO =
        List.of(
            CourtDTO.builder()
                .id(9L)
                .version("1.0")
                .courttype("type123")
                .courtlocation("location123")
                .newEntry(true)
                .build());

    when(courtRepository.deleteAll()).thenReturn(Mono.empty());
    when(courtRepository.saveAll(courtsDTO)).thenReturn(Flux.fromIterable(courtsDTO));

    String courtsXml =
        """
            <?xml version="1.0"?>
            <juris-table>
                <juris-gericht id="9" version="1.0">
                    <gertyp>type123</gertyp>
                    <gerort>location123</gerort>
                    <spruchkoerper>
                        <name>Staatsanwaltschaft</name>
                    </spruchkoerper>
                </juris-gericht>
            </juris-table>
            """;
    ByteBuffer byteBuffer = ByteBuffer.wrap(courtsXml.getBytes());

    StepVerifier.create(service.importCourtLookupTable(byteBuffer))
        .consumeNextWith(
            courtDTO -> assertEquals("Successfully imported the court lookup table", courtDTO))
        .verifyComplete();

    verify(courtRepository).deleteAll();
    // this was already tested by using when(courtRepository.saveAll(courtsDTO)), but we leave it
    // here as an example how to use captors
    verify(courtRepository).saveAll(courtDTOlistCaptor.capture());
    assertThat(courtDTOlistCaptor.getValue())
        .extracting("courttype", "courtlocation")
        .containsExactly(tuple("type123", "location123"));
  }

  @Test
  void testImportStateLookupTable() {
    List<StateDTO> statesDto =
        List.of(
            StateDTO.builder()
                .id(3L)
                .changeindicator('A')
                .version("1.0")
                .jurisshortcut("jurisabk123")
                .label("bezeichnung123")
                .newEntry(true)
                .build());

    when(stateRepository.deleteAll()).thenReturn(Mono.empty());
    when(stateRepository.saveAll(statesDto)).thenReturn(Flux.fromIterable(statesDto));

    String statesXml =
        """
            <?xml version="1.0"?>
            <juris-table>
                <juris-buland id="3" aendkz="A" version="1.0">
                    <jurisabk>jurisabk123</jurisabk>
                    <bezeichnung>bezeichnung123</bezeichnung>
                </juris-buland>
            </juris-table>
            """;
    ByteBuffer byteBuffer = ByteBuffer.wrap(statesXml.getBytes());

    StepVerifier.create(service.importStateLookupTable(byteBuffer))
        .consumeNextWith(
            stateDTO -> assertEquals("Successfully imported the state lookup table", stateDTO))
        .verifyComplete();

    verify(stateRepository).deleteAll();
    verify(stateRepository).saveAll(anyCollection());
  }
}
