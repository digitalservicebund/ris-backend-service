package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentCategoryRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentCategoryDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentTypeRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CitationStyleDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseCitationStyleRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.FieldOfLawKeywordRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.NormRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.PostgresCitationStyleRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.citation.CitationStyle;
import de.bund.digitalservice.ris.caselaw.domain.lookuptable.documenttype.DocumentType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
  PostgresCitationStyleRepositoryImpl.class
})
class LookupTableServiceTest {

  @SpyBean private LookupTableService service;

  @MockBean private DatabaseDocumentTypeRepository databaseDocumentTypeRepository;
  @MockBean private DatabaseDocumentCategoryRepository databaseDocumentCategoryRepository;
  @MockBean private DatabaseCitationStyleRepository databaseCitationStyleRepository;
  @MockBean private FieldOfLawRepository fieldOfLawRepository;
  @MockBean private NormRepository normRepository;
  @MockBean private FieldOfLawKeywordRepository fieldOfLawKeywordRepository;

  @Test
  void testGetDocumentTypes() {
    var category = DocumentCategoryDTO.builder().id(UUID.randomUUID()).label("R").build();
    when(databaseDocumentCategoryRepository.findFirstByLabel("R")).thenReturn(category);

    DocumentTypeDTO documentTypeDTO = DocumentTypeDTO.builder().build();
    documentTypeDTO.setAbbreviation("ABC");
    documentTypeDTO.setLabel("LabelABC");
    when(databaseDocumentTypeRepository.findAllByCategoryOrderByAbbreviationAscLabelAsc(category))
        .thenReturn(List.of(documentTypeDTO));

    StepVerifier.create(service.getCaselawDocumentTypes(Optional.empty()))
        .consumeNextWith(
            documentType -> {
              assertThat(documentType).isInstanceOf(DocumentType.class);
              assertThat(documentType.jurisShortcut()).isEqualTo("ABC");
              assertThat(documentType.label()).isEqualTo("LabelABC");
            })
        .verifyComplete();

    verify(databaseDocumentTypeRepository)
        .findAllByCategoryOrderByAbbreviationAscLabelAsc(category);
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
                .documentType('R')
                .citationDocumentType('R')
                .jurisShortcut("Änderung")
                .label("Änderung")
                .newEntry(true)
                .build());

    when(databaseCitationStyleRepository
            .findAllByDocumentTypeAndCitationDocumentTypeOrderByLabelAsc('R', 'R'))
        .thenReturn(Flux.fromIterable(citationStyleDTOS));

    StepVerifier.create(service.getCitationStyles(Optional.empty()))
        .consumeNextWith(
            citationStyle -> {
              assertThat(citationStyle).isInstanceOf(CitationStyle.class);
              assertThat(citationStyle.documentType()).isEqualTo('R');
              assertThat(citationStyle.citationDocumentType()).isEqualTo('R');
              assertThat(citationStyle.jurisShortcut()).isEqualTo("Änderung");
              assertThat(citationStyle.label()).isEqualTo("Änderung");
            })
        .verifyComplete();

    verify(databaseCitationStyleRepository)
        .findAllByDocumentTypeAndCitationDocumentTypeOrderByLabelAsc('R', 'R');
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
                .documentType('R')
                .citationDocumentType('R')
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
              assertThat(citationStyle.documentType()).isEqualTo('R');
              assertThat(citationStyle.citationDocumentType()).isEqualTo('R');
              assertThat(citationStyle.jurisShortcut()).isEqualTo("Änderung");
              assertThat(citationStyle.label()).isEqualTo("Änderung");
            })
        .verifyComplete();

    verify(databaseCitationStyleRepository).findBySearchStr("Änderung");
  }
}
