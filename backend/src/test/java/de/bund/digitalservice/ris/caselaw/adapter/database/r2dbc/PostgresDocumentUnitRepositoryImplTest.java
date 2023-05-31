package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import static org.mockito.Mockito.verify;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseFieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.proceedingdecision.DatabaseProceedingDecisionLinkRepository;
import de.bund.digitalservice.ris.caselaw.domain.DataSource;
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
  @MockBean private DatabaseDocumentUnitReadRepository repository;
  @MockBean private DatabaseDocumentUnitWriteRepository writeRepository;
  @MockBean private DatabaseDocumentUnitMetadataRepository metadataRepository;
  @MockBean private FileNumberRepository fileNumberRepository;
  @MockBean private DeviatingEcliRepository deviatingEcliRepository;
  @MockBean private DatabaseProceedingDecisionLinkRepository proceedingDecisionLinkRepository;
  @MockBean private DatabaseDeviatingDecisionDateRepository deviatingDecisionDateRepository;
  @MockBean private DatabaseIncorrectCourtRepository incorrectCourtRepository;
  @MockBean private DatabaseCourtRepository databaseCourtRepository;
  @MockBean private StateRepository stateRepository;
  @MockBean private DatabaseDocumentTypeRepository databaseDocumentTypeRepository;
  @MockBean private DatabaseFieldOfLawRepository fieldOfLawRepository;
  @MockBean private DatabaseDocumentUnitFieldsOfLawRepository documentUnitFieldsOfLawRepository;
  @MockBean private DatabaseKeywordRepository keywordRepository;
  @MockBean private DatabaseDocumentUnitNormRepository documentUnitNormRepository;
  @MockBean private DatabaseDocumentationOfficeRepository documentationOfficeRepository;

  @BeforeEach
  public void setup() {
    this.postgresDocumentUnitRepository =
        new PostgresDocumentUnitRepositoryImpl(
            repository,
            writeRepository,
            metadataRepository,
            fileNumberRepository,
            deviatingEcliRepository,
            proceedingDecisionLinkRepository,
            deviatingDecisionDateRepository,
            incorrectCourtRepository,
            databaseCourtRepository,
            stateRepository,
            databaseDocumentTypeRepository,
            fieldOfLawRepository,
            documentUnitFieldsOfLawRepository,
            keywordRepository,
            documentUnitNormRepository,
            documentationOfficeRepository);
  }

  @Test
  void testFindAll() {
    Sort sort = Sort.unsorted();
    Mockito.when(
            metadataRepository.findAllByDataSource(
                DataSource.NEURIS.name(), PageRequest.of(0, 10, sort)))
        .thenReturn(Flux.empty());

    StepVerifier.create(postgresDocumentUnitRepository.findAll(PageRequest.of(0, 10, sort)))
        .verifyComplete();

    verify(metadataRepository)
        .findAllByDataSource(DataSource.NEURIS.name(), PageRequest.of(0, 10, sort));
  }
}
