package de.bund.digitalservice.ris.caselaw.adapter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseFieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseKeywordRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseProcedureRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseReferenceRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseRelatedDocumentationRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitSearchRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.domain.Decision;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitHistoryLogService;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.domain.FeatureToggleService;
import de.bund.digitalservice.ris.caselaw.domain.HistoryLogEventType;
import de.bund.digitalservice.ris.caselaw.domain.ManagementData;
import de.bund.digitalservice.ris.caselaw.domain.UserService;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({PostgresDocumentationUnitRepositoryImpl.class})
class HistoryLogServiceTest {
  private static final LocalDateTime OLD_SCHEDULED_DATE = LocalDateTime.of(2010, 12, 31, 11, 11);
  private static final LocalDateTime NEW_SCHEDULED_DATE = LocalDateTime.of(2011, 1, 1, 11, 11);

  @Autowired private DocumentationUnitRepository repository;

  @MockitoBean private DatabaseDocumentationUnitRepository documentationUnitRepository;

  @MockitoBean private DatabaseCourtRepository courtRepository;

  @MockitoBean private DocumentationUnitHistoryLogService historyLogService;

  @MockitoBean private DatabaseDocumentationOfficeRepository officeRepository;

  @MockitoBean private DatabaseRelatedDocumentationRepository relatedDocumentationRepository;

  @MockitoBean private DatabaseKeywordRepository keywordRepository;

  @MockitoBean private DatabaseProcedureRepository procedureRepository;

  @MockitoBean private DatabaseFieldOfLawRepository fieldOfLawRepository;

  @MockitoBean private DatabaseReferenceRepository referenceRepository;

  @MockitoBean private PostgresDocumentationUnitSearchRepositoryImpl searchRepository;

  @MockitoBean private UserService userService;

  @MockitoBean private FeatureToggleService featureToggleService;

  @MockitoBean private EntityManager entityManager;

  @Test
  void testScheduledPublicationDate_createDate_shouldLogSetDate() {
    UUID documentationUnitId = UUID.randomUUID();
    DecisionDTO dto = DecisionDTO.builder().id(documentationUnitId).build();
    Decision updatedDecision =
        Decision.builder()
            .uuid(documentationUnitId)
            .managementData(
                ManagementData.builder().scheduledPublicationDateTime(NEW_SCHEDULED_DATE).build())
            .build();
    when(documentationUnitRepository.findById(documentationUnitId)).thenReturn(Optional.of(dto));

    repository.save(updatedDecision, null, null);

    verify(historyLogService, times(2)).saveHistoryLog(any(), any(), any(), any());
    verify(historyLogService)
        .saveHistoryLog(
            documentationUnitId,
            null,
            HistoryLogEventType.UPDATE,
            "Abgabe wurde auf den 01.01.2011 11:11 gesetzt.");
    verify(historyLogService)
        .saveHistoryLog(
            documentationUnitId, null, HistoryLogEventType.UPDATE, "Dokeinheit bearbeitet");
  }

  @Test
  void testScheduledPublicationDate_withNoChangeOnDate_shouldLogNothing() {
    UUID documentationUnitId = UUID.randomUUID();
    DecisionDTO dto =
        DecisionDTO.builder()
            .id(documentationUnitId)
            .scheduledPublicationDateTime(OLD_SCHEDULED_DATE)
            .build();
    Decision updatedDecision =
        Decision.builder()
            .uuid(documentationUnitId)
            .managementData(
                ManagementData.builder().scheduledPublicationDateTime(OLD_SCHEDULED_DATE).build())
            .build();
    when(documentationUnitRepository.findById(documentationUnitId)).thenReturn(Optional.of(dto));

    repository.save(updatedDecision, null, null);

    verify(historyLogService, times(1)).saveHistoryLog(any(), any(), any(), any());
    verify(historyLogService)
        .saveHistoryLog(
            documentationUnitId, null, HistoryLogEventType.UPDATE, "Dokeinheit bearbeitet");
  }

  @Test
  void testScheduledPublicationDate_noDateSet_shouldLogNothing() {
    UUID documentationUnitId = UUID.randomUUID();
    DecisionDTO dto = DecisionDTO.builder().id(documentationUnitId).build();
    Decision updatedDecision = Decision.builder().uuid(documentationUnitId).build();
    when(documentationUnitRepository.findById(documentationUnitId)).thenReturn(Optional.of(dto));

    repository.save(updatedDecision, null, null);

    verify(historyLogService, times(1)).saveHistoryLog(any(), any(), any(), any());
    verify(historyLogService)
        .saveHistoryLog(
            documentationUnitId, null, HistoryLogEventType.UPDATE, "Dokeinheit bearbeitet");
  }

  @Test
  void testScheduledPublicationDate_changeDate_shouldLogChangeDate() {
    UUID documentationUnitId = UUID.randomUUID();
    DecisionDTO dto =
        DecisionDTO.builder()
            .id(documentationUnitId)
            .scheduledPublicationDateTime(OLD_SCHEDULED_DATE)
            .build();
    Decision updatedDecision =
        Decision.builder()
            .uuid(documentationUnitId)
            .managementData(
                ManagementData.builder().scheduledPublicationDateTime(NEW_SCHEDULED_DATE).build())
            .build();
    when(documentationUnitRepository.findById(documentationUnitId)).thenReturn(Optional.of(dto));

    repository.save(updatedDecision, null, null);

    verify(historyLogService, times(2)).saveHistoryLog(any(), any(), any(), any());
    verify(historyLogService)
        .saveHistoryLog(
            documentationUnitId,
            null,
            HistoryLogEventType.UPDATE,
            "Abgabe wurde auf den 01.01.2011 11:11 geändert.");
    verify(historyLogService)
        .saveHistoryLog(
            documentationUnitId, null, HistoryLogEventType.UPDATE, "Dokeinheit bearbeitet");
  }

  @Test
  void testScheduledPublicationDate_removeDate_shouldLogDateDeletion() {
    UUID documentationUnitId = UUID.randomUUID();
    DecisionDTO dto =
        DecisionDTO.builder()
            .id(documentationUnitId)
            .scheduledPublicationDateTime(OLD_SCHEDULED_DATE)
            .build();
    Decision updatedDecision = Decision.builder().uuid(documentationUnitId).build();
    when(documentationUnitRepository.findById(documentationUnitId)).thenReturn(Optional.of(dto));

    repository.save(updatedDecision, null, null);

    verify(historyLogService, times(2)).saveHistoryLog(any(), any(), any(), any());
    verify(historyLogService)
        .saveHistoryLog(
            documentationUnitId,
            null,
            HistoryLogEventType.UPDATE,
            "Zeitliche Abgabe wurde gelöscht.");
    verify(historyLogService)
        .saveHistoryLog(
            documentationUnitId, null, HistoryLogEventType.UPDATE, "Dokeinheit bearbeitet");
  }
}
