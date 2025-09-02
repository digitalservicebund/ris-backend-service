package de.bund.digitalservice.ris.caselaw.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScheduledPublicationServiceTest {

  private ScheduledPublicationService service;
  private DocumentationUnitRepository docUnitRepository;
  private HandoverService handoverService;
  private HttpMailSender httpMailSender;
  private final LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
  private int docUnitCounter = 0;

  @BeforeEach
  void beforeEach() {
    this.docUnitRepository = mock(DocumentationUnitRepository.class);
    this.handoverService = mock(HandoverService.class);
    this.httpMailSender = mock(HttpMailSender.class);
    this.service =
        new ScheduledPublicationService(
            this.docUnitRepository, this.handoverService, this.httpMailSender);
  }

  @AfterEach
  void afterEach() {
    reset(this.docUnitRepository);
    reset(this.handoverService);
    reset(this.httpMailSender);
    this.service =
        new ScheduledPublicationService(
            this.docUnitRepository, this.handoverService, this.httpMailSender);
  }

  @Test
  void shouldIdleForEmptyDueDocUnits() throws DocumentationUnitNotExistsException {
    when(this.docUnitRepository.getScheduledDocumentationUnitsDueNow())
        .thenReturn(Collections.emptyList());

    this.service.handoverScheduledDocUnits();

    verify(handoverService, never()).handoverDocumentationUnitAsMail(any(), any(), any());
    verify(docUnitRepository, never()).save(any());
    verify(httpMailSender, never()).sendMail(any(), any(), any(), any(), any(), any());
  }

  @Test
  void shouldHandoverMultipleDueDocUnits() throws DocumentationUnitNotExistsException {
    var publishedDocUnit = this.createDocUnit(pastDate, pastDate);
    var unpublishedDocUnit = this.createDocUnit(null, pastDate);
    when(this.docUnitRepository.getScheduledDocumentationUnitsDueNow())
        .thenReturn(List.of(publishedDocUnit, unpublishedDocUnit));
    mockHandoverWithSuccessStatus(true);

    this.service.handoverScheduledDocUnits();

    verifyPublicationAndDocUnitUpdate(publishedDocUnit);
    verifyPublicationAndDocUnitUpdate(unpublishedDocUnit);
    verify(httpMailSender, never()).sendMail(any(), any(), any(), any(), any(), any());
  }

  @Test
  void shouldStillSaveHandoverDatesWhenHandoverIsUnsuccessful()
      throws DocumentationUnitNotExistsException {
    var publishedDocUnit = this.createDocUnit(pastDate, pastDate);
    var unpublishedDocUnit = this.createDocUnit(null, pastDate);
    when(this.docUnitRepository.getScheduledDocumentationUnitsDueNow())
        .thenReturn(List.of(publishedDocUnit, unpublishedDocUnit));
    mockHandoverWithSuccessStatus(false);
    // When sending the error notification fails, further processing should not be disrupted.
    mockFailedErrorNotificationOnce();

    this.service.handoverScheduledDocUnits();

    verifyPublicationAndDocUnitUpdate(publishedDocUnit);
    verifyPublicationAndDocUnitUpdate(unpublishedDocUnit);
    verifyEmailErrorNotification(publishedDocUnit);
    verifyEmailErrorNotification(unpublishedDocUnit);
  }

  @Test
  void shouldStillSaveHandoverDatesWhenHandoverFails() throws DocumentationUnitNotExistsException {
    var publishedDocUnit = this.createDocUnit(pastDate, pastDate);
    var unpublishedDocUnit = this.createDocUnit(null, pastDate);
    when(this.docUnitRepository.getScheduledDocumentationUnitsDueNow())
        .thenReturn(List.of(publishedDocUnit, unpublishedDocUnit));
    when(this.handoverService.handoverDocumentationUnitAsMail(any(), any(), any()))
        .thenThrow(DocumentationUnitNotExistsException.class);

    this.service.handoverScheduledDocUnits();

    verifyPublicationAndDocUnitUpdate(publishedDocUnit);
    verifyPublicationAndDocUnitUpdate(unpublishedDocUnit);
    verifyEmailErrorNotification(publishedDocUnit);
    verifyEmailErrorNotification(unpublishedDocUnit);
  }

  @Test
  void shouldContinueWhenDocUnitSaveFails() throws DocumentationUnitNotExistsException {
    var publishedDocUnit = this.createDocUnit(pastDate, pastDate);
    var unpublishedDocUnit = this.createDocUnit(null, pastDate);
    mockHandoverWithSuccessStatus(true);
    when(this.docUnitRepository.getScheduledDocumentationUnitsDueNow())
        .thenReturn(List.of(publishedDocUnit, unpublishedDocUnit));
    doThrow(RuntimeException.class).when(this.docUnitRepository).save(any());

    this.service.handoverScheduledDocUnits();

    verifyPublicationAndDocUnitUpdate(publishedDocUnit);
    verifyPublicationAndDocUnitUpdate(unpublishedDocUnit);
    verify(httpMailSender, never()).sendMail(any(), any(), any(), any(), any(), any());
  }

  private void mockHandoverWithSuccessStatus(boolean successStatus)
      throws DocumentationUnitNotExistsException {
    var result = new HandoverMail(null, null, "", "", null, successStatus, null, null, null);
    when(this.handoverService.handoverDocumentationUnitAsMail(any(), any(), any()))
        .thenReturn(result);
  }

  private Decision createDocUnit(
      LocalDateTime lastHandoverDateTime, LocalDateTime scheduledPublicationDateTime) {
    docUnitCounter++;
    String docNumber = String.format("KORE12345%05d", docUnitCounter);
    return Decision.builder()
        .uuid(UUID.randomUUID())
        .documentNumber(docNumber)
        .managementData(
            ManagementData.builder()
                .borderNumbers(Collections.emptyList())
                .lastHandoverDateTime(lastHandoverDateTime)
                .scheduledPublicationDateTime(scheduledPublicationDateTime)
                .scheduledByEmail(docUnitCounter + "@example.local")
                .duplicateRelations(List.of())
                .build())
        .build();
  }

  private void verifyPublicationAndDocUnitUpdate(Decision docUnit)
      throws DocumentationUnitNotExistsException {
    verify(handoverService, times(1))
        .handoverDocumentationUnitAsMail(
            docUnit.uuid(), docUnit.managementData().scheduledByEmail(), null);
    verify(docUnitRepository, times(1))
        .save(
            argThat(
                updatedDocUnit ->
                    werePublicationDatesSet((Decision) updatedDocUnit)
                        && areAllOtherFieldsEqual(docUnit, (Decision) updatedDocUnit)));
  }

  private boolean werePublicationDatesSet(Decision actualDocUnit) {
    boolean wasPublishedWithin5Seconds =
        Duration.between(LocalDateTime.now(), actualDocUnit.managementData().lastHandoverDateTime())
                .abs()
                .getSeconds()
            < 5;
    boolean schedulingWasUnset =
        actualDocUnit.managementData().scheduledPublicationDateTime() == null;
    return schedulingWasUnset && wasPublishedWithin5Seconds;
  }

  private boolean areAllOtherFieldsEqual(Decision expectedDocUnit, Decision actualDocUnit) {
    return expectedDocUnit.toBuilder()
        .managementData(
            expectedDocUnit.managementData().toBuilder()
                .lastHandoverDateTime(null)
                .scheduledPublicationDateTime(null)
                .build())
        .build()
        .equals(
            actualDocUnit.toBuilder()
                .managementData(
                    expectedDocUnit.managementData().toBuilder()
                        .lastHandoverDateTime(null)
                        .scheduledPublicationDateTime(null)
                        .build())
                .build());
  }

  private void verifyEmailErrorNotification(Decision docUnit) {
    verify(httpMailSender, times(1))
        .sendMail(
            any(),
            eq(docUnit.managementData().scheduledByEmail()),
            eq("Terminierte Abgabe fehlgeschlagen: " + docUnit.documentNumber()),
            contains("Terminierte Abgabe von"),
            eq(Collections.emptyList()),
            eq(docUnit.documentNumber()));
  }

  private void mockFailedErrorNotificationOnce() {
    // First call fails
    doThrow(new RuntimeException())
        // second call succeeds
        .doNothing()
        .when(httpMailSender)
        .sendMail(any(), any(), any(), any(), any(), any());
  }
}
