package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.LookupTableImporterService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAFieldOfLawDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAFieldOfLawLinkRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAFieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPAKeywordDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPANormDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CitationStyleDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseCitationStyleRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseFieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateRepository;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockedStatic;
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

  @MockBean private DatabaseDocumentTypeRepository databaseDocumentTypeRepository;

  @MockBean private JPADocumentTypeRepository jpaDocumentTypeRepository;

  @MockBean private DatabaseCourtRepository databaseCourtRepository;

  @MockBean private StateRepository stateRepository;

  @MockBean private DatabaseCitationStyleRepository databaseCitationStyleRepository;

  @MockBean private DatabaseFieldOfLawRepository fieldOfLawRepository;

  @MockBean private JPAFieldOfLawRepository jpaFieldOfLawRepository;

  @MockBean private JPAFieldOfLawLinkRepository jpaFieldOfLawLinkRepository;

  @Test
  void testImportDocumentTypeLookupTable() {
    when(databaseDocumentTypeRepository.deleteAll()).thenReturn(Mono.empty());

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

    verify(databaseDocumentTypeRepository, never()).deleteAll();
    verify(databaseDocumentTypeRepository, never()).saveAll(anyCollection());
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

    when(databaseCourtRepository.deleteAll()).thenReturn(Mono.empty());
    when(databaseCourtRepository.saveAll(courtsDTO)).thenReturn(Flux.fromIterable(courtsDTO));

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

    verify(databaseCourtRepository).deleteAll();
    // this was already tested by using when(courtRepository.saveAll(courtsDTO)), but we leave it
    // here as an example how to use captors
    verify(databaseCourtRepository).saveAll(courtDTOlistCaptor.capture());
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

  @Test
  void testImportCitationStyleLookupTable() {

    UUID TEST_UUID = UUID.randomUUID();
    List<CitationStyleDTO> citationStyleDTOS =
        List.of(
            CitationStyleDTO.builder()
                .uuid(TEST_UUID)
                .jurisId(1L)
                .changeIndicator('N')
                .version("1.0")
                .documentType("R")
                .citationDocumentType("R")
                .jurisShortcut("Änderung")
                .label("Änderung")
                .newEntry(true)
                .build());

    when(databaseCitationStyleRepository.deleteAll()).thenReturn(Mono.empty());
    when(databaseCitationStyleRepository.saveAll(citationStyleDTOS))
        .thenReturn(Flux.fromIterable(citationStyleDTOS));

    String citationStyleXml =
        """
            <?xml version="1.0"?>
            <juris-table>
              <juris-zitart id="1" aendkz="N" version="1.0">
                <dok_dokumentart>R</dok_dokumentart>
                <zit_dokumentart>R</zit_dokumentart>
                <abk>Änderung</abk>
                <bezeichnung>Änderung</bezeichnung>
              </juris-zitart>
            </juris-table>
            """;
    ByteBuffer byteBuffer = ByteBuffer.wrap(citationStyleXml.getBytes());

    try (MockedStatic<UUID> mockedUUIDStatic = mockStatic(UUID.class)) {
      mockedUUIDStatic.when(UUID::randomUUID).thenReturn(TEST_UUID);
      StepVerifier.create(service.importCitationStyleLookupTable(byteBuffer))
          .consumeNextWith(
              citationStyleDTO ->
                  assertEquals("Successfully imported the citation lookup table", citationStyleDTO))
          .verifyComplete();
    }

    verify(databaseCitationStyleRepository).deleteAll();
    verify(databaseCitationStyleRepository).saveAll(citationStyleDTOS);
  }

  @Test
  void testImportFieldOfLawLookupTable() {
    JPANormDTO childNorm1 =
        JPANormDTO.builder()
            .jpaFieldOfLawDTO(null)
            .abbreviation("normabk 2.1")
            .singleNormDescription("§ 2.1")
            .build();
    JPANormDTO childNorm2 =
        JPANormDTO.builder().jpaFieldOfLawDTO(null).abbreviation("normabk 2.2").build();
    Set<JPANormDTO> childNorms = Set.of(childNorm1, childNorm2);

    JPAKeywordDTO childKeyword1 =
        JPAKeywordDTO.builder().jpaFieldOfLawDTO(null).value("schlagwort 2.1").build();
    JPAKeywordDTO childKeyword2 =
        JPAKeywordDTO.builder().jpaFieldOfLawDTO(null).value("schlagwort 2.3").build();
    Set<JPAKeywordDTO> childKeywords = Set.of(childKeyword1, childKeyword2);

    JPAFieldOfLawDTO parent =
        JPAFieldOfLawDTO.builder().id(1L).parentFieldOfLaw(null).identifier("TS-01").build();
    JPAFieldOfLawDTO child =
        JPAFieldOfLawDTO.builder()
            .id(2L)
            .parentFieldOfLaw(parent)
            .changeDateMail("2022-12-22")
            .changeDateClient("2022-12-24")
            .changeIndicator('N')
            .version("1.0")
            .identifier("TS-01-01")
            .text("stext 2")
            .navigationTerm("navbez 2")
            .keywords(childKeywords)
            .norms(childNorms)
            .build();
    List<JPAFieldOfLawDTO> jpaFieldOfLawDTOS = List.of(parent, child);

    String fieldOfLawXml =
        """
            <?xml version="1.0"?>
            <juris-table>

                <juris-sachg id="2" aenddatum_mail="2022-12-22" aendkz="J" version="1.0">
                    <sachgebiet>TS-01-01</sachgebiet>
                    <stext>stext 2</stext>
                    <navbez>navbez 2</navbez>
                    <norm>
                        <normabk>normabk 2.1</normabk>
                        <enbez>§ 2.1</enbez>
                    </norm>
                    <norm>
                        <normabk>normabk 2.2</normabk>
                    </norm>
                    <schlagwort>schlagwort 2.1</schlagwort>
                    <schlagwort>schlagwort 2.2</schlagwort>
                </juris-sachg>

                <juris-sachg id="1" aendkz="N">
                    <sachgebiet>TS-01</sachgebiet>
                </juris-sachg>

            </juris-table>
            """;
    ByteBuffer byteBuffer = ByteBuffer.wrap(fieldOfLawXml.getBytes());

    StepVerifier.create(service.importFieldOfLawLookupTable(byteBuffer))
        .consumeNextWith(
            resultString ->
                assertEquals("Successfully imported the fieldOfLaw lookup table", resultString))
        .verifyComplete();

    verify(jpaFieldOfLawRepository, atMostOnce()).deleteAll();
    verify(jpaFieldOfLawRepository, atMostOnce()).saveAll(jpaFieldOfLawDTOS);
    verify(fieldOfLawRepository, never()).deleteAll();
    verify(fieldOfLawRepository, never()).saveAll(anyCollection());
  }
}
