package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.DatabaseDocumentationUnitStatusService;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.StatusDTO;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
@Import({DatabaseDocumentationUnitStatusService.class})
class DocumentationUnitStatusServiceTest {
  private static final UUID TEST_UUID = UUID.fromString("11111111-2222-3333-4444-555555555555");

  private static final String DOCUMENT_NUMBER = "TEST00012024";

  @SpyBean private DatabaseDocumentationUnitStatusService statusService;

  @MockBean private DatabaseDocumentationUnitRepository databaseDocumentationUnitRepository;

  @Test
  void testUpdate_withDocumentNumberAndDocumentationUnitNotFound_shouldNotSaveAStatus() {
    Status status = Status.builder().build();

    when(databaseDocumentationUnitRepository.findByDocumentNumber(DOCUMENT_NUMBER))
        .thenReturn(Optional.empty());

    Assert.assertThrows(
        DocumentationUnitNotExistsException.class,
        () -> statusService.update(DOCUMENT_NUMBER, status));

    verify(databaseDocumentationUnitRepository, times(1)).findByDocumentNumber(DOCUMENT_NUMBER);
  }

  @Test
  void
      testUpdate_withDocumentNumberAndDocumentationUnitFoundWithExistingStatus_shouldUpdateTheExistingStatus()
          throws DocumentationUnitNotExistsException {

    var statusList =
        List.of(
            StatusDTO.builder()
                .publicationStatus(PublicationStatus.DUPLICATED)
                .withError(true)
                .createdAt(Instant.now().minus(2, ChronoUnit.DAYS))
                .build(),
            StatusDTO.builder()
                .publicationStatus(PublicationStatus.PUBLISHED)
                .withError(true)
                .createdAt(Instant.now())
                .build(),
            StatusDTO.builder()
                .publicationStatus(PublicationStatus.UNPUBLISHED)
                .withError(true)
                .createdAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build());

    DocumentationUnitDTO documentationUnitDTO =
        DocumentationUnitDTO.builder()
            .id(TEST_UUID)
            .documentNumber(DOCUMENT_NUMBER)
            .status(new ArrayList<>(statusList))
            .build();

    ArgumentCaptor<DocumentationUnitDTO> captor =
        ArgumentCaptor.forClass(DocumentationUnitDTO.class);

    when(databaseDocumentationUnitRepository.findByDocumentNumber(DOCUMENT_NUMBER))
        .thenReturn(Optional.of(documentationUnitDTO));
    var timestamp = Instant.now();

    Status newStatus =
        Status.builder()
            .createdAt(timestamp)
            .publicationStatus(PublicationStatus.PUBLISHED)
            .withError(true)
            .build();

    statusService.update(DOCUMENT_NUMBER, newStatus);

    verify(databaseDocumentationUnitRepository, times(1)).save(captor.capture());
    assertThat(captor.getValue().getStatus())
        .usingRecursiveComparison()
        .ignoringFields("id", "createdAt")
        .isEqualTo(
            Stream.concat(
                    statusList.stream(),
                    Stream.of(
                        StatusDTO.builder()
                            .publicationStatus(PublicationStatus.PUBLISHED)
                            .withError(true)
                            .build()))
                .collect(Collectors.toList()));
  }

  @Test
  void getLatestStatus() throws DocumentationUnitNotExistsException {
    DocumentationUnitDTO documentationUnitDTO =
        DocumentationUnitDTO.builder()
            .id(TEST_UUID)
            .documentNumber(DOCUMENT_NUMBER)
            .status(
                new ArrayList<>(
                    List.of(
                        StatusDTO.builder()
                            .createdAt(Instant.now())
                            .publicationStatus(PublicationStatus.PUBLISHED)
                            .build())))
            .build();

    when(databaseDocumentationUnitRepository.findByDocumentNumber(DOCUMENT_NUMBER))
        .thenReturn(Optional.of(documentationUnitDTO));

    var latestStatus = statusService.getLatestStatus(DOCUMENT_NUMBER);
    Assertions.assertEquals(PublicationStatus.PUBLISHED, latestStatus);

    verify(databaseDocumentationUnitRepository, times(1)).findByDocumentNumber(DOCUMENT_NUMBER);
  }
}
