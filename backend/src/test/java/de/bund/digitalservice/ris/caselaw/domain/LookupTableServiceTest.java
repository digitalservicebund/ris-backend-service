package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CitationStyleDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseCitationStyleRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.FieldOfLawKeywordRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.NormRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.PostgresCitationStyleRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.PostgresCourtRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.PostgresDocumentTypeRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationStyle;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.court.Court;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@Import({
  LookupTableService.class,
  PostgresDocumentTypeRepositoryImpl.class,
  PostgresCourtRepositoryImpl.class,
  PostgresCitationStyleRepositoryImpl.class
})
class LookupTableServiceTest {

  @SpyBean private LookupTableService service;

  @MockBean private DatabaseDocumentTypeRepository databaseDocumentTypeRepository;
  @MockBean private DatabaseCourtRepository databaseCourtRepository;
  @MockBean private DatabaseCitationStyleRepository databaseCitationStyleRepository;
  @MockBean private FieldOfLawRepository fieldOfLawRepository;
  @MockBean private NormRepository normRepository;
  @MockBean private FieldOfLawKeywordRepository fieldOfLawKeywordRepository;

  @Captor private ArgumentCaptor<CitationStyleDTO> listCaptor;

  @Test
  void testGetDocumentTypes() {
    DocumentTypeDTO documentTypeDTO = DocumentTypeDTO.EMPTY;
    documentTypeDTO.setJurisShortcut("ABC");
    documentTypeDTO.setLabel("LabelABC");
    when(databaseDocumentTypeRepository.findAllByDocumentTypeOrderByJurisShortcutAscLabelAsc('R'))
        .thenReturn(Flux.just(documentTypeDTO));

    StepVerifier.create(service.getCaselawDocumentTypes(Optional.empty()))
        .consumeNextWith(
            documentType -> {
              assertThat(documentType).isInstanceOf(DocumentType.class);
              assertThat(documentType.jurisShortcut()).isEqualTo("ABC");
              assertThat(documentType.label()).isEqualTo("LabelABC");
            })
        .verifyComplete();

    verify(databaseDocumentTypeRepository)
        .findAllByDocumentTypeOrderByJurisShortcutAscLabelAsc('R');
  }

  @Test
  void testGetTwoDifferentCourts() {
    // court where the location will be intentionally dropped
    CourtDTO courtA = new CourtDTO();
    courtA.setCourttype("ABC");
    courtA.setCourtlocation("Berlin");
    courtA.setSuperiorcourt("Ja");
    courtA.setForeigncountry("Nein");

    // court where the location will be kept
    CourtDTO courtB = new CourtDTO();
    courtB.setCourttype("XYZ");
    courtB.setCourtlocation("Hamburg");
    courtB.setSuperiorcourt("Nein");
    courtB.setForeigncountry("Nein");

    when(databaseCourtRepository.findAllByOrderByCourttypeAscCourtlocationAsc())
        .thenReturn(Flux.fromIterable(List.of(courtA, courtB)));

    StepVerifier.create(service.getCourts(Optional.empty()))
        .expectNext(new Court("ABC", null, "ABC", null))
        .expectNext(new Court("XYZ", "Hamburg", "XYZ Hamburg", null))
        .verifyComplete();

    verify(databaseCourtRepository).findAllByOrderByCourttypeAscCourtlocationAsc();
  }

  @Test
  void testGetCitationStyles() {
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

    when(databaseCitationStyleRepository
            .findAllByDocumentTypeAndCitationDocumentTypeOrderByCitationDocumentTypeAsc('R', 'R'))
        .thenReturn(Flux.fromIterable(citationStyleDTOS));

    StepVerifier.create(service.getCitationStyles(Optional.empty()))
        .consumeNextWith(
            citationStyle -> {
              assertThat(citationStyle).isInstanceOf(CitationStyle.class);
              assertThat(citationStyle.documentType()).isEqualTo("R");
              assertThat(citationStyle.citationDocumentType()).isEqualTo("R");
              assertThat(citationStyle.jurisShortcut()).isEqualTo("Änderung");
              assertThat(citationStyle.label()).isEqualTo("Änderung");
            })
        .verifyComplete();

    verify(databaseCitationStyleRepository)
        .findAllByDocumentTypeAndCitationDocumentTypeOrderByCitationDocumentTypeAsc('R', 'R');
  }

  @Test
  void testGetCitationStylesWithSearchQuery() {
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

    when(databaseCitationStyleRepository.findBySearchStr("Änderung"))
        .thenReturn(Flux.fromIterable(citationStyleDTOS));

    StepVerifier.create(service.getCitationStyles(Optional.of("Änderung")))
        .consumeNextWith(
            citationStyle -> {
              assertThat(citationStyle).isInstanceOf(CitationStyle.class);
              assertThat(citationStyle.documentType()).isEqualTo("R");
              assertThat(citationStyle.citationDocumentType()).isEqualTo("R");
              assertThat(citationStyle.jurisShortcut()).isEqualTo("Änderung");
              assertThat(citationStyle.label()).isEqualTo("Änderung");
            })
        .verifyComplete();

    verify(databaseCitationStyleRepository).findBySearchStr("Änderung");
  }
}
