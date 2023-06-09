package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import static org.mockito.Mockito.verify;

import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseDocumentTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseFieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.DatabaseNormAbbreviationRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.lookuptable.StateRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.proceedingdecision.DatabaseProceedingDecisionLinkRepository;
import de.bund.digitalservice.ris.caselaw.domain.DataSource;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
class PostgresDocumentUnitRepositoryImplTest {

  PostgresDocumentUnitRepositoryImpl postgresDocumentUnitRepository;
  @MockBean private DatabaseDocumentUnitRepository repository;
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
  @MockBean private DatabaseDocumentUnitStatusRepository databaseDocumentUnitStatusRepository;
  @MockBean private DatabaseNormAbbreviationRepository normAbbreviationRepository;

  @BeforeEach
  public void setup() {
    this.postgresDocumentUnitRepository =
        new PostgresDocumentUnitRepositoryImpl(
            repository,
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
            documentationOfficeRepository,
            databaseDocumentUnitStatusRepository,
            normAbbreviationRepository);
  }

  @Test
  void testFindAll() {
    Sort sort = Sort.unsorted();
    var documentationOfficeId = UUID.randomUUID();
    Mockito.when(
            metadataRepository.findAllByDataSourceAndDocumentationOfficeId(
                DataSource.NEURIS.name(), documentationOfficeId, 10, 0L))
        .thenReturn(Flux.empty());

    Mockito.when(documentationOfficeRepository.findByLabel("Test"))
        .thenReturn(
            Mono.just(
                DocumentationOfficeDTO.builder().id(documentationOfficeId).label("Test").build()));

    StepVerifier.create(
            postgresDocumentUnitRepository.findAll(
                PageRequest.of(0, 10, sort), DocumentationOffice.builder().label("Test").build()))
        .verifyComplete();

    verify(metadataRepository)
        .findAllByDataSourceAndDocumentationOfficeId(
            DataSource.NEURIS.name(), documentationOfficeId, 10, 0L);
  }
}
