package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import static org.mockito.Mockito.verify;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.CourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseFieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.proceedingdecision.DatabaseProceedingDecisionLinkRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.proceedingdecision.DatabaseProceedingDecisionRepository;
import de.bund.digitalservice.ris.caselaw.domain.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
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
  @MockBean private DatabaseProceedingDecisionRepository databaseProceedingDecisionRepository;
  @MockBean private DatabaseProceedingDecisionLinkRepository proceedingDecisionLinkRepository;
  @MockBean private DatabaseDeviatingDecisionDateRepository deviatingDecisionDateRepository;
  @MockBean private DatabaseIncorrectCourtRepository incorrectCourtRepository;
  @MockBean private CourtRepository courtRepository;
  @MockBean private StateRepository stateRepository;
  @MockBean private DocumentTypeRepository documentTypeRepository;
  @MockBean private DatabaseFieldOfLawRepository fieldOfLawRepository;
  @MockBean private DatabaseDocumentUnitFieldsOfLawRepository documentUnitFieldsOfLawRepository;
  @MockBean private DatabaseKeywordRepository keywordRepository;

  @BeforeEach
  public void setup() {
    this.postgresDocumentUnitRepository =
        new PostgresDocumentUnitRepositoryImpl(
            repository,
            metadataRepository,
            fileNumberRepository,
            deviatingEcliRepository,
            databaseProceedingDecisionRepository,
            proceedingDecisionLinkRepository,
            deviatingDecisionDateRepository,
            incorrectCourtRepository,
            courtRepository,
            stateRepository,
            documentTypeRepository,
            fieldOfLawRepository,
            documentUnitFieldsOfLawRepository,
            keywordRepository);
  }

  @Test
  void testFindAll() {
    Sort sort = Sort.unsorted();
    Mockito.when(metadataRepository.findAllByDataSourceLike(sort, DataSource.NEURIS.name()))
        .thenReturn(Flux.empty());

    StepVerifier.create(postgresDocumentUnitRepository.findAll(sort)).verifyComplete();

    verify(metadataRepository).findAllByDataSourceLike(sort, DataSource.NEURIS.name());
  }
}
