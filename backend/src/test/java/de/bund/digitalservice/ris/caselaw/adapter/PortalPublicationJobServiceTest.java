package de.bund.digitalservice.ris.caselaw.adapter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PortalPublicationJobDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PortalPublicationJobRepository;
import de.bund.digitalservice.ris.caselaw.domain.PortalPublicationTaskStatus;
import de.bund.digitalservice.ris.caselaw.domain.PortalPublicationTaskType;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PortalPublicationJobServiceTest {

  private PortalPublicationJobService service;
  private PortalPublicationJobRepository publicationJobRepository;
  private PrototypePortalPublicationService prototypePortalPublicationService;

  @BeforeEach
  void beforeEach() {
    this.publicationJobRepository = mock(PortalPublicationJobRepository.class);
    this.prototypePortalPublicationService = mock(PrototypePortalPublicationService.class);
    this.service =
        new PortalPublicationJobService(
            this.publicationJobRepository, this.prototypePortalPublicationService);
  }

  @Test
  void shouldDoNothingOnEmptyJobs() throws DocumentationUnitNotExistsException {
    when(this.publicationJobRepository.findNextPendingJobsBatch()).thenReturn(List.of());

    this.service.executePendingJobs();

    verify(prototypePortalPublicationService, never()).publishDocumentationUnit(anyString());
    verify(prototypePortalPublicationService, never()).deleteDocumentationUnit(anyString());
    verify(publicationJobRepository, never()).saveAll(any());
  }

  @Test
  void shouldPublishASingleDocUnit() throws DocumentationUnitNotExistsException {
    var jobs = List.of(createPublicationJob("123", PortalPublicationTaskType.PUBLISH));
    when(this.publicationJobRepository.findNextPendingJobsBatch()).thenReturn(jobs);

    this.service.executePendingJobs();

    verify(prototypePortalPublicationService, times(1)).publishDocumentationUnit("123");
    verify(prototypePortalPublicationService, never()).deleteDocumentationUnit(anyString());
    // currently disabled
    //    verify(publicPortalPublicationService, times(1)).uploadChangelog(List.of("123.xml"),
    // List.of());
    verify(publicationJobRepository, times(1)).saveAll(jobs);
    assertThat(jobs.getFirst().getPublicationStatus())
        .isEqualTo(PortalPublicationTaskStatus.SUCCESS);
  }

  @Test
  void shouldDeleteASingleDocUnit() throws DocumentationUnitNotExistsException {
    var jobs = List.of(createPublicationJob("456", PortalPublicationTaskType.DELETE));
    when(this.publicationJobRepository.findNextPendingJobsBatch()).thenReturn(jobs);

    this.service.executePendingJobs();

    verify(prototypePortalPublicationService, never()).publishDocumentationUnit(anyString());
    verify(prototypePortalPublicationService, times(1)).deleteDocumentationUnit("456");
    // currently disabled
    //    verify(publicPortalPublicationService, times(1)).uploadChangelog(List.of(),
    // List.of("456.xml"));
    verify(publicationJobRepository, times(1)).saveAll(jobs);
    assertThat(jobs.getFirst().getPublicationStatus())
        .isEqualTo(PortalPublicationTaskStatus.SUCCESS);
  }

  @Test
  void shouldHandleErrorWhenPublishingASingleDocUnit() throws DocumentationUnitNotExistsException {
    var jobs = List.of(createPublicationJob("789", PortalPublicationTaskType.PUBLISH));
    when(this.publicationJobRepository.findNextPendingJobsBatch()).thenReturn(jobs);
    doThrow(RuntimeException.class)
        .when(prototypePortalPublicationService)
        .publishDocumentationUnit("789");

    this.service.executePendingJobs();

    verify(prototypePortalPublicationService, times(1)).publishDocumentationUnit("789");
    verify(prototypePortalPublicationService, never()).deleteDocumentationUnit(anyString());
    verify(prototypePortalPublicationService, never()).uploadChangelog(any(), any());
    verify(publicationJobRepository, times(1)).saveAll(jobs);
    assertThat(jobs.getFirst().getPublicationStatus()).isEqualTo(PortalPublicationTaskStatus.ERROR);
  }

  @Test
  void shouldHandleErrorWhenDeletingASingleDocUnit() throws DocumentationUnitNotExistsException {
    var jobs = List.of(createPublicationJob("312", PortalPublicationTaskType.DELETE));
    when(this.publicationJobRepository.findNextPendingJobsBatch()).thenReturn(jobs);
    doThrow(RuntimeException.class)
        .when(prototypePortalPublicationService)
        .deleteDocumentationUnit("312");

    this.service.executePendingJobs();

    verify(prototypePortalPublicationService, never()).publishDocumentationUnit(anyString());
    verify(prototypePortalPublicationService, times(1)).deleteDocumentationUnit("312");
    verify(prototypePortalPublicationService, never()).uploadChangelog(any(), any());
    verify(publicationJobRepository, times(1)).saveAll(jobs);
    assertThat(jobs.getFirst().getPublicationStatus()).isEqualTo(PortalPublicationTaskStatus.ERROR);
  }

  @Test
  void shouldCatchErrorWhenUploadingChangelogFails() throws DocumentationUnitNotExistsException {
    var jobs = List.of(createPublicationJob("312", PortalPublicationTaskType.DELETE));
    when(this.publicationJobRepository.findNextPendingJobsBatch()).thenReturn(jobs);
    doThrow(RuntimeException.class)
        .when(prototypePortalPublicationService)
        .uploadChangelog(any(), any());

    this.service.executePendingJobs();

    verify(prototypePortalPublicationService, never()).publishDocumentationUnit(anyString());
    verify(prototypePortalPublicationService, times(1)).deleteDocumentationUnit("312");
    // currently disabled
    //    verify(publicPortalPublicationService, times(1)).uploadChangelog(List.of(),
    // List.of("312.xml"));
    verify(publicationJobRepository, times(1)).saveAll(jobs);
    assertThat(jobs.getFirst().getPublicationStatus())
        .isEqualTo(PortalPublicationTaskStatus.SUCCESS);
  }

  @Test
  void shouldContinueExecutionOnErrors() throws DocumentationUnitNotExistsException {
    var jobs =
        List.of(
            createPublicationJob("1", PortalPublicationTaskType.PUBLISH),
            createPublicationJob("2", PortalPublicationTaskType.DELETE),
            createPublicationJob("3", PortalPublicationTaskType.PUBLISH),
            createPublicationJob("4", PortalPublicationTaskType.DELETE),
            createPublicationJob("5", PortalPublicationTaskType.PUBLISH));
    when(this.publicationJobRepository.findNextPendingJobsBatch()).thenReturn(jobs);
    doThrow(RuntimeException.class)
        .when(prototypePortalPublicationService)
        .publishDocumentationUnit("1");
    doThrow(RuntimeException.class)
        .when(prototypePortalPublicationService)
        .deleteDocumentationUnit("2");

    this.service.executePendingJobs();

    verify(prototypePortalPublicationService, times(1)).publishDocumentationUnit("1");
    verify(prototypePortalPublicationService, times(1)).publishDocumentationUnit("3");
    verify(prototypePortalPublicationService, times(1)).publishDocumentationUnit("5");
    verify(prototypePortalPublicationService, times(1)).deleteDocumentationUnit("2");
    verify(prototypePortalPublicationService, times(1)).deleteDocumentationUnit("4");
    // currently disabled
    //    verify(publicPortalPublicationService, times(1))
    //        .uploadChangelog(List.of("3.xml", "5.xml"), List.of("4.xml"));
    verify(publicationJobRepository, times(1)).saveAll(jobs);
    assertThat(jobs.getFirst().getPublicationStatus()).isEqualTo(PortalPublicationTaskStatus.ERROR);
    assertThat(jobs.get(1).getPublicationStatus()).isEqualTo(PortalPublicationTaskStatus.ERROR);
    assertThat(jobs.get(2).getPublicationStatus()).isEqualTo(PortalPublicationTaskStatus.SUCCESS);
    assertThat(jobs.get(3).getPublicationStatus()).isEqualTo(PortalPublicationTaskStatus.SUCCESS);
    assertThat(jobs.get(4).getPublicationStatus()).isEqualTo(PortalPublicationTaskStatus.SUCCESS);
  }

  private PortalPublicationJobDTO createPublicationJob(
      String docNumber, PortalPublicationTaskType type) {
    return PortalPublicationJobDTO.builder()
        .documentNumber(docNumber)
        .publicationType(type)
        .publicationStatus(PortalPublicationTaskStatus.PENDING)
        .build();
  }
}
