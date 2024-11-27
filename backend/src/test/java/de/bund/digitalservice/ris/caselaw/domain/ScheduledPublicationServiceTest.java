package de.bund.digitalservice.ris.caselaw.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScheduledPublicationServiceTest {

  private ScheduledPublicationService service;
  private DocumentationUnitRepository repository;
  private HandoverService handoverService;
  private final LocalDateTime pastDate = LocalDateTime.now().minusDays(1);

  @BeforeEach
  void beforeEach() {
    this.repository = mock(DocumentationUnitRepository.class);
    this.handoverService = mock(HandoverService.class);
    this.service = new ScheduledPublicationService(this.repository, this.handoverService);
  }

  @Test
  void shouldIdleForEmptyDueDocUnits() throws DocumentationUnitNotExistsException {
    when(this.repository.getScheduledDocumentationUnitsDueNow())
        .thenReturn(Collections.emptyList());

    this.service.handoverScheduledDocUnits();

    verify(handoverService, never()).handoverDocumentationUnitAsMail(any(), any());
    verify(repository, never()).save(any());
  }

  @Test
  void shouldHandoverMultipleDueDocUnits() throws DocumentationUnitNotExistsException {
    var publishedDocUnit = this.createDocUnit(pastDate, pastDate);
    var unpublishedDocUnit = this.createDocUnit(null, pastDate);
    when(this.repository.getScheduledDocumentationUnitsDueNow())
        .thenReturn(List.of(publishedDocUnit, unpublishedDocUnit));

    this.service.handoverScheduledDocUnits();

    verifyPublicationAndProcessing(publishedDocUnit);
    verifyPublicationAndProcessing(unpublishedDocUnit);
  }

  @Test
  void shouldStillSavePublicationDatesWhenHandoverFails()
      throws DocumentationUnitNotExistsException {
    var publishedDocUnit = this.createDocUnit(pastDate, pastDate);
    var unpublishedDocUnit = this.createDocUnit(null, pastDate);
    when(this.repository.getScheduledDocumentationUnitsDueNow())
        .thenReturn(List.of(publishedDocUnit, unpublishedDocUnit));
    when(this.handoverService.handoverDocumentationUnitAsMail(any(), any()))
        .thenThrow(DocumentationUnitNotExistsException.class);

    this.service.handoverScheduledDocUnits();

    verifyPublicationAndProcessing(publishedDocUnit);
    verifyPublicationAndProcessing(unpublishedDocUnit);
  }

  @Test
  void shouldContinueWhenDocUnitSaveFails() throws DocumentationUnitNotExistsException {
    var publishedDocUnit = this.createDocUnit(pastDate, pastDate);
    var unpublishedDocUnit = this.createDocUnit(null, pastDate);
    when(this.repository.getScheduledDocumentationUnitsDueNow())
        .thenReturn(List.of(publishedDocUnit, unpublishedDocUnit));
    doThrow(RuntimeException.class).when(this.repository).save(any());

    this.service.handoverScheduledDocUnits();

    verifyPublicationAndProcessing(publishedDocUnit);
    verifyPublicationAndProcessing(unpublishedDocUnit);
  }

  private DocumentationUnit createDocUnit(
      LocalDateTime lastPublicationDateTime, LocalDateTime scheduledPublicationDateTime) {
    return DocumentationUnit.builder()
        .uuid(UUID.randomUUID())
        .managementData(
            ManagementData.builder()
                .borderNumbers(Collections.emptyList())
                .lastPublicationDateTime(lastPublicationDateTime)
                .scheduledPublicationDateTime(scheduledPublicationDateTime)
                .build())
        .build();
  }

  private void verifyPublicationAndProcessing(DocumentationUnit docUnit)
      throws DocumentationUnitNotExistsException {
    verify(handoverService, times(1))
        .handoverDocumentationUnitAsMail(docUnit.uuid(), "mail@example.local");
    verify(repository, times(1))
        .save(
            argThat(
                updatedDocUnit ->
                    werePublicationDatesSet(updatedDocUnit)
                        && areAllOtherFieldsEqual(docUnit, updatedDocUnit)));
  }

  private boolean werePublicationDatesSet(DocumentationUnit actualDocUnit) {
    boolean wasPublishedWithin5Seconds =
        Duration.between(
                    LocalDateTime.now(), actualDocUnit.managementData().lastPublicationDateTime())
                .abs()
                .getSeconds()
            < 5;
    boolean schedulingWasUnset =
        actualDocUnit.managementData().scheduledPublicationDateTime() == null;
    return schedulingWasUnset && wasPublishedWithin5Seconds;
  }

  private boolean areAllOtherFieldsEqual(
      DocumentationUnit expectedDocUnit, DocumentationUnit actualDocUnit) {
    return expectedDocUnit.toBuilder()
        .managementData(
            expectedDocUnit.managementData().toBuilder()
                .lastPublicationDateTime(null)
                .scheduledPublicationDateTime(null)
                .build())
        .build()
        .equals(
            actualDocUnit.toBuilder()
                .managementData(
                    expectedDocUnit.managementData().toBuilder()
                        .lastPublicationDateTime(null)
                        .scheduledPublicationDateTime(null)
                        .build())
                .build());
  }
}
