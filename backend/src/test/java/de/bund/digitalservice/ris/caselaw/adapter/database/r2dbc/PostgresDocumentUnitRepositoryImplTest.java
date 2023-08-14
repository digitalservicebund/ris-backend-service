package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import static org.mockito.Mockito.verify;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JPADocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseCitationStyleRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseFieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseNormAbbreviationRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateRepository;
import de.bund.digitalservice.ris.caselaw.domain.DocumentUnitSearchInput;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationOffice;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
class PostgresDocumentUnitRepositoryImplTest {

  PostgresDocumentUnitRepositoryImpl postgresDocumentUnitRepository;
  @MockBean private DatabaseDocumentUnitRepository repository;
  @MockBean private DatabaseDocumentUnitMetadataRepository metadataRepository;
  @MockBean private FileNumberRepository fileNumberRepository;
  @MockBean private DeviatingEcliRepository deviatingEcliRepository;
  @MockBean private DatabaseDocumentationUnitLinkRepository documentationUnitLinkRepository;
  @MockBean private DatabaseDeviatingDecisionDateRepository deviatingDecisionDateRepository;
  @MockBean private DatabaseIncorrectCourtRepository incorrectCourtRepository;
  @MockBean private DatabaseCourtRepository databaseCourtRepository;
  @MockBean private StateRepository stateRepository;
  @MockBean private DatabaseDocumentTypeRepository databaseDocumentTypeRepository;
  @MockBean private DatabaseFieldOfLawRepository fieldOfLawRepository;
  @MockBean private DatabaseDocumentUnitFieldsOfLawRepository documentUnitFieldsOfLawRepository;
  @MockBean private DatabaseKeywordRepository keywordRepository;
  @MockBean private DatabaseDocumentUnitNormRepository documentUnitNormRepository;
  @MockBean private JPADocumentationOfficeRepository documentationOfficeRepository;
  @MockBean private DatabaseDocumentUnitStatusRepository databaseDocumentUnitStatusRepository;
  @MockBean private DatabaseNormAbbreviationRepository normAbbreviationRepository;
  @MockBean private DatabaseCitationStyleRepository citationStyleRepository;

  @BeforeEach
  public void setup() {
    this.postgresDocumentUnitRepository =
        new PostgresDocumentUnitRepositoryImpl(
            repository,
            metadataRepository,
            fileNumberRepository,
            deviatingEcliRepository,
            deviatingDecisionDateRepository,
            incorrectCourtRepository,
            databaseCourtRepository,
            stateRepository,
            databaseDocumentTypeRepository,
            fieldOfLawRepository,
            documentUnitFieldsOfLawRepository,
            keywordRepository,
            documentUnitNormRepository,
            databaseDocumentUnitStatusRepository,
            normAbbreviationRepository,
            documentationUnitLinkRepository,
            citationStyleRepository,
            documentationOfficeRepository);
  }

  @Test
  void testSearchByDocumentUnitListEntry() {
    Sort sort = Sort.unsorted();
    var documentationOfficeId = UUID.randomUUID();

    DocumentUnitSearchInput documentUnitListEntry = DocumentUnitSearchInput.builder().build();

    Mockito.when(
            metadataRepository.searchByDocumentUnitSearchInput(
                documentationOfficeId, 10, 0L, null, null, null, null, null, false))
        .thenReturn(Flux.empty());

    Mockito.when(documentationOfficeRepository.findByLabel("Test"))
        .thenReturn(
            JPADocumentationOfficeDTO.builder().id(documentationOfficeId).label("Test").build());

    StepVerifier.create(
            postgresDocumentUnitRepository.searchByDocumentUnitSearchInput(
                PageRequest.of(0, 10, sort),
                DocumentationOffice.builder().label("Test").build(),
                documentUnitListEntry))
        .verifyComplete();

    verify(metadataRepository)
        .searchByDocumentUnitSearchInput(
            documentationOfficeId, 10, 0L, null, null, null, null, null, false);
  }
}
