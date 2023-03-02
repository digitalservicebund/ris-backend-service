package de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc;

import static org.mockito.Mockito.verify;

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
class PostgresDocumentUnitListEntryRepositoryImplTest {

  PostgresDocumentUnitListEntryRepositoryImpl postgresDocumentUnitListEntryRepository;

  @MockBean private DatabaseDocumentUnitListEntryRepository listEntryRepository;

  @MockBean private FileNumberRepository fileNumberRepository;

  @BeforeEach
  public void setup() {
    this.postgresDocumentUnitListEntryRepository =
        new PostgresDocumentUnitListEntryRepositoryImpl(listEntryRepository, fileNumberRepository);
  }

  @Test
  void testFindAll() {
    Sort sort = Sort.unsorted();
    Mockito.when(listEntryRepository.findAllByDataSourceLike(sort, DataSource.NEURIS.name()))
        .thenReturn(Flux.empty());

    StepVerifier.create(postgresDocumentUnitListEntryRepository.findAll(sort)).verifyComplete();

    verify(listEntryRepository).findAllByDataSourceLike(sort, DataSource.NEURIS.name());
  }
}
