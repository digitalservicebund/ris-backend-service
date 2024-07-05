package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import java.util.Optional;
import java.util.UUID;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({DatabaseDocumentUnitStatusService.class})
class DocumentUnitStatusServiceTest {
  private static final UUID TEST_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");

  @SpyBean private DatabaseDocumentUnitStatusService statusService;

  @MockBean private DatabaseStatusRepository repository;

  @MockBean private DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository;

  @MockBean private DocumentUnitRepository documentUnitRepo;

  @Test
  void testUpdate_withDocumentNumberAndDocumentationUnitNotFound_shouldNotSaveAStatus() {
    String documentNumber = "document number";
    Status status = Status.builder().build();
    DocumentationUnitDTO documentUnitDto = DocumentationUnitDTO.builder().build();

    when(databaseDocumentationUnitRepository.findByDocumentNumber(documentNumber))
        .thenReturn(Optional.of(documentUnitDto));

    Assert.assertThrows(
        DocumentationUnitNotExistsException.class,
        () -> statusService.update(documentNumber, status));

    verify(databaseDocumentationUnitRepository, times(1)).findByDocumentNumber(documentNumber);
    verify(repository, never())
        .findFirstByDocumentationUnitDTOOrderByCreatedAtDesc(any(DocumentationUnitDTO.class));
    verify(repository, never()).save(any(StatusDTO.class));
  }

  @Test
  void
      testUpdate_withDocumentNumberAndDocumentationUnitFoundWithoutExistingStatus_shouldNotSaveAStatus() {
    String documentNumber = "document number";
    Status status = Status.builder().build();
    DocumentUnit documentationUnit = DocumentUnit.builder().uuid(TEST_UUID).build();
    DocumentationUnitDTO documentationUnitDTO =
        DocumentationUnitDTO.builder()
            .id(TEST_UUID)
            .documentNumber(documentationUnit.documentNumber())
            .build();
    when(databaseDocumentationUnitRepository.findByDocumentNumber(documentNumber))
        .thenReturn(Optional.of(documentationUnitDTO));
    when(repository.findFirstByDocumentationUnitDTO_IdOrderByCreatedAtDesc(
            documentationUnitDTO.getId()))
        .thenReturn(null);

    Assert.assertThrows(
        NullPointerException.class, () -> statusService.update(documentNumber, status));

    verify(databaseDocumentationUnitRepository, times(1)).findByDocumentNumber(documentNumber);
    verify(repository, never())
        .findFirstByDocumentationUnitDTOOrderByCreatedAtDesc(any(DocumentationUnitDTO.class));
    verify(repository, never()).save(any(StatusDTO.class));
  }

  @Test
  void
      testUpdate_withDocumentNumberAndDocumentationUnitFoundWithExistingStatus_shouldUpdateTheExistingStatus()
          throws DocumentationUnitNotExistsException {
    String documentNumber = "document number";
    Status status =
        Status.builder().publicationStatus(PublicationStatus.PUBLISHED).withError(true).build();
    DocumentUnit.builder().uuid(TEST_UUID).build();
    DocumentationUnitDTO documentationUnitDTO =
        DocumentationUnitDTO.builder().id(TEST_UUID).documentNumber(documentNumber).build();
    StatusDTO statusDTO =
        StatusDTO.builder()
            .documentationUnitDTO(documentationUnitDTO)
            .withError(false)
            .publicationStatus(PublicationStatus.UNPUBLISHED)
            .build();

    StatusDTO updatedStatus =
        StatusDTO.builder()
            .documentationUnitDTO(documentationUnitDTO)
            .publicationStatus(PublicationStatus.PUBLISHED)
            .withError(true)
            .build();
    ArgumentCaptor<StatusDTO> captor = ArgumentCaptor.forClass(StatusDTO.class);

    when(databaseDocumentationUnitRepository.findByDocumentNumber(documentNumber))
        .thenReturn(Optional.of(documentationUnitDTO));
    when(repository.findFirstByDocumentationUnitDTO_IdOrderByCreatedAtDesc(
            documentationUnitDTO.getId()))
        .thenReturn(Optional.of(statusDTO));
    when(repository.save(any(StatusDTO.class))).thenReturn(updatedStatus);

    statusService.update(documentNumber, status);

    verify(repository, times(1))
        .findFirstByDocumentationUnitDTO_IdOrderByCreatedAtDesc(documentationUnitDTO.getId());
    verify(repository, times(1)).save(captor.capture());
    assertThat(captor.getValue())
        .usingRecursiveComparison()
        .ignoringFields("createdAt")
        .isEqualTo(updatedStatus);
  }

  @Test
  void testUpdate_withUUID() throws DocumentationUnitNotExistsException {
    DocumentationUnitDTO documentationUnitDTO =
        DocumentationUnitDTO.builder().id(TEST_UUID).build();
    Status status =
        Status.builder().publicationStatus(PublicationStatus.PUBLISHED).withError(true).build();
    StatusDTO statusDTO = StatusDTO.builder().documentationUnitDTO(documentationUnitDTO).build();
    StatusDTO updatedStatus =
        StatusDTO.builder()
            .documentationUnitDTO(documentationUnitDTO)
            .publicationStatus(PublicationStatus.PUBLISHED)
            .withError(true)
            .build();
    ArgumentCaptor<StatusDTO> captor = ArgumentCaptor.forClass(StatusDTO.class);
    when(databaseDocumentationUnitRepository.getReferenceById(TEST_UUID))
        .thenReturn(documentationUnitDTO);
    when(repository.findFirstByDocumentationUnitDTO_IdOrderByCreatedAtDesc(
            documentationUnitDTO.getId()))
        .thenReturn(Optional.of(statusDTO));
    when(repository.save(any(StatusDTO.class))).thenReturn(updatedStatus);

    statusService.update(TEST_UUID, status);

    verify(repository, times(1))
        .findFirstByDocumentationUnitDTO_IdOrderByCreatedAtDesc(documentationUnitDTO.getId());
    verify(repository, times(1)).save(captor.capture());
    assertThat(captor.getValue())
        .usingRecursiveComparison()
        .ignoringFields("createdAt")
        .isEqualTo(updatedStatus);
  }

  @Test
  void getLatestStatus() {
    DocumentationUnitDTO documentationUnitDTO =
        DocumentationUnitDTO.builder().id(TEST_UUID).build();
    StatusDTO statusDTO =
        StatusDTO.builder().publicationStatus(PublicationStatus.PUBLISHED).build();
    when(repository.findFirstByDocumentationUnitDTOOrderByCreatedAtDesc(documentationUnitDTO))
        .thenReturn(statusDTO);
    when(databaseDocumentationUnitRepository.getReferenceById(TEST_UUID))
        .thenReturn(documentationUnitDTO);

    var latestStatus = statusService.getLatestStatus(TEST_UUID);
    Assertions.assertEquals(PublicationStatus.PUBLISHED, latestStatus);

    verify(repository, times(1))
        .findFirstByDocumentationUnitDTOOrderByCreatedAtDesc(documentationUnitDTO);
    verify(databaseDocumentationUnitRepository, times(1)).getReferenceById(TEST_UUID);
  }
}
