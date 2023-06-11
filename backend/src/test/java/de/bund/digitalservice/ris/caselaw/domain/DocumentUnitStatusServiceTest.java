package de.bund.digitalservice.ris.caselaw.domain;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DatabaseDocumentUnitStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.DocumentUnitStatusDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.r2dbc.PostgresDocumentUnitRepositoryImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@Import({DatabaseDocumentUnitStatusService.class})
class DocumentUnitStatusServiceTest {

  @SpyBean private DatabaseDocumentUnitStatusService service;

  @MockBean private DatabaseDocumentUnitStatusRepository repository;

  @MockBean private PostgresDocumentUnitRepositoryImpl documentUnitRepository;

  @Test
  void testSetInitialStatus() {
    DocumentUnit documentUnit = DocumentUnit.builder().build();
    DocumentUnitStatusDTO documentUnitStatusDTO = DocumentUnitStatusDTO.builder().build();

    when(repository.save(any(DocumentUnitStatusDTO.class)))
        .thenReturn(Mono.just(documentUnitStatusDTO));
    when(documentUnitRepository.findByUuid(documentUnit.uuid()))
        .thenReturn(Mono.just(documentUnit));

    StepVerifier.create(service.setInitialStatus(documentUnit)).expectNextCount(1).verifyComplete();

    verify(service).setInitialStatus(documentUnit);
    verify(repository).save(any(DocumentUnitStatusDTO.class));
    verify(documentUnitRepository).findByUuid(documentUnit.uuid());
  }
}
