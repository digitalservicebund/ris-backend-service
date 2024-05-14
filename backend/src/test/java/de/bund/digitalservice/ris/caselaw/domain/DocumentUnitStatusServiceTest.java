package de.bund.digitalservice.ris.caselaw.domain;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({DatabaseDocumentUnitStatusService.class})
class DocumentUnitStatusServiceTest {

  @SpyBean private DatabaseDocumentUnitStatusService service;

  @MockBean private DatabaseStatusRepository repository;

  @MockBean private DatabaseDocumentationUnitRepository documentUnitRepository;

  @MockBean private DocumentUnitRepository documentUnitRepo;

  @Test
  void testSetInitialStatus() throws DocumentationUnitNotExistsException {
    DocumentationUnitDTO documentUnitDTO =
        DocumentationUnitDTO.builder().id(UUID.randomUUID()).build();
    DocumentUnit documentUnit = DocumentUnit.builder().uuid(documentUnitDTO.getId()).build();
    StatusDTO documentUnitStatusDTO = StatusDTO.builder().build();

    when(repository.save(any(StatusDTO.class))).thenReturn(documentUnitStatusDTO);
    when(documentUnitRepo.findByUuid(documentUnitDTO.getId()))
        .thenReturn(Optional.of(documentUnit));

    service.setInitialStatus(documentUnit);

    verify(service).setInitialStatus(documentUnit);
    verify(repository).save(any(StatusDTO.class));
    verify(documentUnitRepo).findByUuid(documentUnit.uuid());
  }
}
