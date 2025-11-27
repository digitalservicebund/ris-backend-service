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
import de.bund.digitalservice.ris.caselaw.domain.PublicationJobStatus;
import de.bund.digitalservice.ris.caselaw.domain.PublicationJobType;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PortalPublicationJobServiceTest {

  private PortalPublicationJobService service;
  private PortalPublicationJobRepository publicationJobRepository;
  private PortalPublicationService portalPublicationService;

  @BeforeEach
  void beforeEach() {
    this.publicationJobRepository = mock(PortalPublicationJobRepository.class);
    this.portalPublicationService = mock(PortalPublicationService.class);
    this.service =
        new PortalPublicationJobService(
            this.publicationJobRepository, this.portalPublicationService);
  }

  @Test
  void shouldDoNothingOnEmptyJobs() throws DocumentationUnitNotExistsException {
    when(this.publicationJobRepository.findNextPendingJobsBatch()).thenReturn(List.of());

    this.service.executePendingJobs();

    verify(portalPublicationService, never()).publishDocumentationUnit(anyString());
    verify(portalPublicationService, never()).withdrawDocumentationUnit(anyString());
    verify(publicationJobRepository, never()).saveAll(any());
  }

  @Test
  void shouldPublishASingleDocUnit() throws DocumentationUnitNotExistsException {
    var jobs = List.of(createPublicationJob("123", PublicationJobType.PUBLISH));
    when(this.publicationJobRepository.findNextPendingJobsBatch()).thenReturn(jobs);
    when(this.portalPublicationService.publishDocumentationUnit("123"))
        .thenReturn(new PortalPublicationResult(List.of("123.xml"), List.of()));

    this.service.executePendingJobs();

    verify(portalPublicationService, times(1)).publishDocumentationUnit("123");
    verify(portalPublicationService, never()).withdrawDocumentationUnit(anyString());
    verify(portalPublicationService, times(1)).uploadChangelog(List.of("123.xml"), List.of());
    verify(publicationJobRepository, times(1)).saveAll(jobs);
    assertThat(jobs.getFirst().getPublicationJobStatus()).isEqualTo(PublicationJobStatus.SUCCESS);
  }

  @Test
  void shouldDeleteASingleDocUnit() throws DocumentationUnitNotExistsException {
    var jobs = List.of(createPublicationJob("456", PublicationJobType.DELETE));
    when(this.publicationJobRepository.findNextPendingJobsBatch()).thenReturn(jobs);
    when(this.portalPublicationService.withdrawDocumentationUnit("456"))
        .thenReturn(new PortalPublicationResult(List.of(), List.of("456.xml")));

    this.service.executePendingJobs();

    verify(portalPublicationService, never()).publishDocumentationUnit(anyString());
    verify(portalPublicationService, times(1)).withdrawDocumentationUnit("456");
    verify(portalPublicationService, times(1)).uploadChangelog(List.of(), List.of("456.xml"));
    verify(publicationJobRepository, times(1)).saveAll(jobs);
    assertThat(jobs.getFirst().getPublicationJobStatus()).isEqualTo(PublicationJobStatus.SUCCESS);
  }

  @Test
  void shouldHandleErrorWhenPublishingASingleDocUnit() throws DocumentationUnitNotExistsException {
    var jobs = List.of(createPublicationJob("789", PublicationJobType.PUBLISH));
    when(this.publicationJobRepository.findNextPendingJobsBatch()).thenReturn(jobs);
    doThrow(RuntimeException.class).when(portalPublicationService).publishDocumentationUnit("789");

    this.service.executePendingJobs();

    verify(portalPublicationService, times(1)).publishDocumentationUnit("789");
    verify(portalPublicationService, never()).withdrawDocumentationUnit(anyString());
    verify(portalPublicationService, never()).uploadChangelog(any(), any());
    verify(publicationJobRepository, times(1)).saveAll(jobs);
    assertThat(jobs.getFirst().getPublicationJobStatus()).isEqualTo(PublicationJobStatus.ERROR);
  }

  @Test
  void shouldHandleErrorWhenDeletingASingleDocUnit() throws DocumentationUnitNotExistsException {
    var jobs = List.of(createPublicationJob("312", PublicationJobType.DELETE));
    when(this.publicationJobRepository.findNextPendingJobsBatch()).thenReturn(jobs);
    doThrow(RuntimeException.class).when(portalPublicationService).withdrawDocumentationUnit("312");

    this.service.executePendingJobs();

    verify(portalPublicationService, never()).publishDocumentationUnit(anyString());
    verify(portalPublicationService, times(1)).withdrawDocumentationUnit("312");
    verify(portalPublicationService, never()).uploadChangelog(any(), any());
    verify(publicationJobRepository, times(1)).saveAll(jobs);
    assertThat(jobs.getFirst().getPublicationJobStatus()).isEqualTo(PublicationJobStatus.ERROR);
  }

  @Test
  void shouldCatchErrorWhenUploadingChangelogFails() throws DocumentationUnitNotExistsException {
    var jobs = List.of(createPublicationJob("312", PublicationJobType.DELETE));
    when(this.publicationJobRepository.findNextPendingJobsBatch()).thenReturn(jobs);
    doThrow(RuntimeException.class).when(portalPublicationService).uploadChangelog(any(), any());

    this.service.executePendingJobs();

    verify(portalPublicationService, never()).publishDocumentationUnit(anyString());
    verify(portalPublicationService, times(1)).withdrawDocumentationUnit("312");
    // currently disabled
    //    verify(publicPortalPublicationService, times(1)).uploadChangelog(List.of(),
    // List.of("312.xml"));
    verify(publicationJobRepository, times(1)).saveAll(jobs);
    assertThat(jobs.getFirst().getPublicationJobStatus()).isEqualTo(PublicationJobStatus.SUCCESS);
  }

  @Test
  void shouldContinueExecutionOnErrors() throws DocumentationUnitNotExistsException {
    var jobs =
        List.of(
            createPublicationJob("1", PublicationJobType.PUBLISH),
            createPublicationJob("2", PublicationJobType.DELETE),
            createPublicationJob("3", PublicationJobType.PUBLISH),
            createPublicationJob("4", PublicationJobType.DELETE),
            createPublicationJob("5", PublicationJobType.PUBLISH));
    when(this.publicationJobRepository.findNextPendingJobsBatch()).thenReturn(jobs);
    doThrow(RuntimeException.class).when(portalPublicationService).publishDocumentationUnit("1");
    doThrow(RuntimeException.class).when(portalPublicationService).withdrawDocumentationUnit("2");

    this.service.executePendingJobs();

    verify(portalPublicationService, times(1)).publishDocumentationUnit("1");
    verify(portalPublicationService, times(1)).publishDocumentationUnit("3");
    verify(portalPublicationService, times(1)).publishDocumentationUnit("5");
    verify(portalPublicationService, times(1)).withdrawDocumentationUnit("2");
    verify(portalPublicationService, times(1)).withdrawDocumentationUnit("4");
    // currently disabled
    //    verify(publicPortalPublicationService, times(1))
    //        .uploadChangelog(List.of("3.xml", "5.xml"), List.of("4.xml"));
    verify(publicationJobRepository, times(1)).saveAll(jobs);
    assertThat(jobs.getFirst().getPublicationJobStatus()).isEqualTo(PublicationJobStatus.ERROR);
    assertThat(jobs.get(1).getPublicationJobStatus()).isEqualTo(PublicationJobStatus.ERROR);
    assertThat(jobs.get(2).getPublicationJobStatus()).isEqualTo(PublicationJobStatus.SUCCESS);
    assertThat(jobs.get(3).getPublicationJobStatus()).isEqualTo(PublicationJobStatus.SUCCESS);
    assertThat(jobs.get(4).getPublicationJobStatus()).isEqualTo(PublicationJobStatus.SUCCESS);
  }

  private PortalPublicationJobDTO createPublicationJob(String docNumber, PublicationJobType type) {
    return PortalPublicationJobDTO.builder()
        .documentNumber(docNumber)
        .publicationJobType(type)
        .publicationJobStatus(PublicationJobStatus.PENDING)
        .build();
  }
}
