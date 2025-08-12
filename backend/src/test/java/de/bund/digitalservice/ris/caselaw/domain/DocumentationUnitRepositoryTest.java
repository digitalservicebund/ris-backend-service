package de.bund.digitalservice.ris.caselaw.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCourtRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationOfficeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitProcessStepRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseFieldOfLawRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseKeywordRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseProcedureRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseProcessStepRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseReferenceRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseRelatedDocumentationRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PostgresDocumentationUnitRepositoryImpl;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import(PostgresDocumentationUnitRepositoryImpl.class)
class DocumentationUnitRepositoryTest {
  @Autowired private DocumentationUnitRepository repository;
  @MockitoBean private DatabaseDocumentationUnitRepository documentationUnitRepository;
  @MockitoBean private DatabaseCourtRepository databaseCourtRepository;
  @MockitoBean private DatabaseDocumentationOfficeRepository documentationOfficeRepository;
  @MockitoBean private DatabaseKeywordRepository keywordRepository;
  @MockitoBean private DatabaseFieldOfLawRepository fieldOfLawRepository;
  @MockitoBean private DatabaseProcedureRepository procedureRepository;
  @MockitoBean private DatabaseRelatedDocumentationRepository relatedDocumentationRepository;
  @MockitoBean private DatabaseProcessStepRepository processStepRepository;

  @MockitoBean
  private DatabaseDocumentationUnitProcessStepRepository
      databaseDocumentationUnitProcessStepRepository;

  @MockitoBean private EntityManager entityManager;
  @MockitoBean private DatabaseReferenceRepository referenceRepository;
  @MockitoBean private DocumentationUnitHistoryLogService historyLogService;

  @Test
  void testFindByUuid() throws DocumentationUnitNotExistsException {
    UUID userId = UUID.randomUUID();
    UUID documentationOfficeId = UUID.randomUUID();
    User user =
        User.builder()
            .id(userId)
            .documentationOffice(DocumentationOffice.builder().id(documentationOfficeId).build())
            .build();
    UUID otherDocumentationOfficeId = UUID.randomUUID();
    UUID documentationUnitId = UUID.randomUUID();
    ProcessStepDTO processStep1 =
        ProcessStepDTO.builder().id(UUID.randomUUID()).name("process step 1").build();
    ProcessStepDTO processStepOtherDocumentationOffice =
        ProcessStepDTO.builder()
            .id(UUID.randomUUID())
            .name("process step other documentation office")
            .build();
    ProcessStepDTO processStep2 =
        ProcessStepDTO.builder().id(UUID.randomUUID()).name("process step 2").build();
    List<DocumentationUnitProcessStepDTO> processStepDTOs =
        List.of(
            DocumentationUnitProcessStepDTO.builder()
                .processStep(processStep1)
                .userId(userId)
                .build(),
            DocumentationUnitProcessStepDTO.builder()
                .processStep(processStepOtherDocumentationOffice)
                .userId(otherDocumentationOfficeId)
                .build(),
            DocumentationUnitProcessStepDTO.builder()
                .processStep(processStep2)
                .userId(userId)
                .build());
    DecisionDTO decisionDTO =
        DecisionDTO.builder().id(documentationUnitId).processSteps(processStepDTOs).build();
    when(documentationUnitRepository.findById(documentationUnitId))
        .thenReturn(Optional.of(decisionDTO));
    when(documentationOfficeRepository.findById(documentationOfficeId))
        .thenReturn(
            Optional.of(
                DocumentationOfficeDTO.builder()
                    .id(documentationOfficeId)
                    .processSteps(List.of(processStep1, processStep2))
                    .build()));
    when(documentationOfficeRepository.findById(otherDocumentationOfficeId))
        .thenReturn(
            Optional.of(
                DocumentationOfficeDTO.builder()
                    .id(otherDocumentationOfficeId)
                    .processSteps(List.of(processStepOtherDocumentationOffice))
                    .build()));

    DocumentationUnit result = repository.findByUuid(documentationUnitId, user);

    assertThat(result.processSteps())
        .extracting("processStep.name")
        .containsExactly("process step 1", "process step 2");
  }

  @Test
  void testFindByUuid_withoutUser_shouldRemoveAllProcessSteps()
      throws DocumentationUnitNotExistsException {
    UUID userId = UUID.randomUUID();
    UUID documentationOfficeId = UUID.randomUUID();
    UUID otherDocumentationOfficeId = UUID.randomUUID();
    UUID documentationUnitId = UUID.randomUUID();
    ProcessStepDTO processStep1 =
        ProcessStepDTO.builder().id(UUID.randomUUID()).name("process step 1").build();
    ProcessStepDTO processStepOtherDocumentationOffice =
        ProcessStepDTO.builder()
            .id(UUID.randomUUID())
            .name("process step other documentation office")
            .build();
    ProcessStepDTO processStep2 =
        ProcessStepDTO.builder().id(UUID.randomUUID()).name("process step 2").build();
    List<DocumentationUnitProcessStepDTO> processStepDTOs =
        List.of(
            DocumentationUnitProcessStepDTO.builder()
                .processStep(processStep1)
                .userId(userId)
                .build(),
            DocumentationUnitProcessStepDTO.builder()
                .processStep(processStepOtherDocumentationOffice)
                .userId(otherDocumentationOfficeId)
                .build(),
            DocumentationUnitProcessStepDTO.builder()
                .processStep(processStep2)
                .userId(userId)
                .build());
    DecisionDTO decisionDTO =
        DecisionDTO.builder().id(documentationUnitId).processSteps(processStepDTOs).build();
    when(documentationUnitRepository.findById(documentationUnitId))
        .thenReturn(Optional.of(decisionDTO));
    when(documentationOfficeRepository.findById(documentationOfficeId))
        .thenReturn(
            Optional.of(
                DocumentationOfficeDTO.builder()
                    .id(documentationOfficeId)
                    .processSteps(List.of(processStep1, processStep2))
                    .build()));
    when(documentationOfficeRepository.findById(otherDocumentationOfficeId))
        .thenReturn(
            Optional.of(
                DocumentationOfficeDTO.builder()
                    .id(otherDocumentationOfficeId)
                    .processSteps(List.of(processStepOtherDocumentationOffice))
                    .build()));

    DocumentationUnit result = repository.findByUuid(documentationUnitId, null);

    assertThat(result.processSteps()).isEmpty();
  }

  @Test
  void testFindByUuid_withUserWithoutDocumentationOffice_shouldRemoveAllProcessSteps()
      throws DocumentationUnitNotExistsException {
    UUID userId = UUID.randomUUID();
    UUID documentationOfficeId = UUID.randomUUID();
    User user = User.builder().id(userId).documentationOffice(null).build();
    UUID otherDocumentationOfficeId = UUID.randomUUID();
    UUID documentationUnitId = UUID.randomUUID();
    ProcessStepDTO processStep1 =
        ProcessStepDTO.builder().id(UUID.randomUUID()).name("process step 1").build();
    ProcessStepDTO processStepOtherDocumentationOffice =
        ProcessStepDTO.builder()
            .id(UUID.randomUUID())
            .name("process step other documentation office")
            .build();
    ProcessStepDTO processStep2 =
        ProcessStepDTO.builder().id(UUID.randomUUID()).name("process step 2").build();
    List<DocumentationUnitProcessStepDTO> processStepDTOs =
        List.of(
            DocumentationUnitProcessStepDTO.builder()
                .processStep(processStep1)
                .userId(userId)
                .build(),
            DocumentationUnitProcessStepDTO.builder()
                .processStep(processStepOtherDocumentationOffice)
                .userId(otherDocumentationOfficeId)
                .build(),
            DocumentationUnitProcessStepDTO.builder()
                .processStep(processStep2)
                .userId(userId)
                .build());
    DecisionDTO decisionDTO =
        DecisionDTO.builder().id(documentationUnitId).processSteps(processStepDTOs).build();
    when(documentationUnitRepository.findById(documentationUnitId))
        .thenReturn(Optional.of(decisionDTO));
    when(documentationOfficeRepository.findById(documentationOfficeId))
        .thenReturn(
            Optional.of(
                DocumentationOfficeDTO.builder()
                    .id(documentationOfficeId)
                    .processSteps(List.of(processStep1, processStep2))
                    .build()));
    when(documentationOfficeRepository.findById(otherDocumentationOfficeId))
        .thenReturn(
            Optional.of(
                DocumentationOfficeDTO.builder()
                    .id(otherDocumentationOfficeId)
                    .processSteps(List.of(processStepOtherDocumentationOffice))
                    .build()));

    DocumentationUnit result = repository.findByUuid(documentationUnitId, user);

    assertThat(result.processSteps()).isEmpty();
  }

  @Test
  void testFindByUuid_withUserWithNotExistingDocumentationOffice_shouldRemoveAllProcessSteps()
      throws DocumentationUnitNotExistsException {
    UUID userId = UUID.randomUUID();
    UUID documentationOfficeId = UUID.randomUUID();
    User user =
        User.builder()
            .id(userId)
            .documentationOffice(DocumentationOffice.builder().id(UUID.randomUUID()).build())
            .build();
    UUID otherDocumentationOfficeId = UUID.randomUUID();
    UUID documentationUnitId = UUID.randomUUID();
    ProcessStepDTO processStep1 =
        ProcessStepDTO.builder().id(UUID.randomUUID()).name("process step 1").build();
    ProcessStepDTO processStepOtherDocumentationOffice =
        ProcessStepDTO.builder()
            .id(UUID.randomUUID())
            .name("process step other documentation office")
            .build();
    ProcessStepDTO processStep2 =
        ProcessStepDTO.builder().id(UUID.randomUUID()).name("process step 2").build();
    List<DocumentationUnitProcessStepDTO> processStepDTOs =
        List.of(
            DocumentationUnitProcessStepDTO.builder()
                .processStep(processStep1)
                .userId(userId)
                .build(),
            DocumentationUnitProcessStepDTO.builder()
                .processStep(processStepOtherDocumentationOffice)
                .userId(otherDocumentationOfficeId)
                .build(),
            DocumentationUnitProcessStepDTO.builder()
                .processStep(processStep2)
                .userId(userId)
                .build());
    DecisionDTO decisionDTO =
        DecisionDTO.builder().id(documentationUnitId).processSteps(processStepDTOs).build();
    when(documentationUnitRepository.findById(documentationUnitId))
        .thenReturn(Optional.of(decisionDTO));
    when(documentationOfficeRepository.findById(documentationOfficeId))
        .thenReturn(
            Optional.of(
                DocumentationOfficeDTO.builder()
                    .id(documentationOfficeId)
                    .processSteps(List.of(processStep1, processStep2))
                    .build()));
    when(documentationOfficeRepository.findById(otherDocumentationOfficeId))
        .thenReturn(
            Optional.of(
                DocumentationOfficeDTO.builder()
                    .id(otherDocumentationOfficeId)
                    .processSteps(List.of(processStepOtherDocumentationOffice))
                    .build()));

    DocumentationUnit result = repository.findByUuid(documentationUnitId, user);

    assertThat(result.processSteps()).isEmpty();
  }

  @Test
  void testFindByUuid_withDocumentationOfficeWithoutProcessSteps_shouldRemoveAllProcessSteps()
      throws DocumentationUnitNotExistsException {
    UUID userId = UUID.randomUUID();
    UUID documentationOfficeId = UUID.randomUUID();
    User user =
        User.builder()
            .id(userId)
            .documentationOffice(DocumentationOffice.builder().id(documentationOfficeId).build())
            .build();
    UUID otherDocumentationOfficeId = UUID.randomUUID();
    UUID documentationUnitId = UUID.randomUUID();
    ProcessStepDTO processStep1 =
        ProcessStepDTO.builder().id(UUID.randomUUID()).name("process step 1").build();
    ProcessStepDTO processStepOtherDocumentationOffice =
        ProcessStepDTO.builder()
            .id(UUID.randomUUID())
            .name("process step other documentation office")
            .build();
    ProcessStepDTO processStep2 =
        ProcessStepDTO.builder().id(UUID.randomUUID()).name("process step 2").build();
    List<DocumentationUnitProcessStepDTO> processStepDTOs =
        List.of(
            DocumentationUnitProcessStepDTO.builder()
                .processStep(processStep1)
                .userId(userId)
                .build(),
            DocumentationUnitProcessStepDTO.builder()
                .processStep(processStepOtherDocumentationOffice)
                .userId(otherDocumentationOfficeId)
                .build(),
            DocumentationUnitProcessStepDTO.builder()
                .processStep(processStep2)
                .userId(userId)
                .build());
    DecisionDTO decisionDTO =
        DecisionDTO.builder().id(documentationUnitId).processSteps(processStepDTOs).build();
    when(documentationUnitRepository.findById(documentationUnitId))
        .thenReturn(Optional.of(decisionDTO));
    when(documentationOfficeRepository.findById(documentationOfficeId))
        .thenReturn(
            Optional.of(
                DocumentationOfficeDTO.builder()
                    .id(documentationOfficeId)
                    .processSteps(null)
                    .build()));
    when(documentationOfficeRepository.findById(otherDocumentationOfficeId))
        .thenReturn(
            Optional.of(
                DocumentationOfficeDTO.builder()
                    .id(otherDocumentationOfficeId)
                    .processSteps(List.of(processStepOtherDocumentationOffice))
                    .build()));

    DocumentationUnit result = repository.findByUuid(documentationUnitId, user);

    assertThat(result.processSteps()).isEmpty();
  }
}
