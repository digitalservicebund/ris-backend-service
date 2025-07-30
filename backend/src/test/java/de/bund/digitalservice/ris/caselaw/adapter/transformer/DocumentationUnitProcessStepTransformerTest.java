package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.domain.DocumentationUnitProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.ProcessStep;
import de.bund.digitalservice.ris.caselaw.domain.User;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DocumentationUnitProcessStepTransformerTest {

  UUID documentationUnitProcessStepId = UUID.randomUUID();
  User user = User.builder().id(UUID.randomUUID()).build();
  LocalDateTime createdAt = LocalDateTime.now();

  @Test
  void shouldTransformToDomain() {

    ProcessStep processStep = ProcessStep.builder().uuid(UUID.randomUUID()).build();
    DocumentationUnitProcessStepDTO documentationUnitProcessStepDTO =
        DocumentationUnitProcessStepDTO.builder()
            .id(documentationUnitProcessStepId)
            .userId(user.id())
            .createdAt(createdAt)
            .build();
    DocumentationUnitProcessStep documentationUnitProcessStep =
        DocumentationUnitProcessStep.builder()
            .id(documentationUnitProcessStepId)
            .user(user)
            .createdAt(createdAt)
            .processStep(processStep)
            .build();

    assertThat(
            DocumentationUnitProcessStepTransformer.toDomain(
                documentationUnitProcessStepDTO, processStep))
        .isEqualTo(documentationUnitProcessStep);
  }

  @Test
  void shouldNotTransformToDomain_ifProcessStepNull() {
    ProcessStep processStep = null;
    DocumentationUnitProcessStepDTO documentationUnitProcessStepDTO =
        DocumentationUnitProcessStepDTO.builder()
            .id(documentationUnitProcessStepId)
            .userId(user.id())
            .createdAt(createdAt)
            .build();

    assertThat(
            DocumentationUnitProcessStepTransformer.toDomain(
                documentationUnitProcessStepDTO, processStep))
        .isNull();
  }

  @Test
  void shouldNotTransformToDomain_ifDocumentationUnitProcessStepDTONull() {
    ProcessStep processStep = ProcessStep.builder().uuid(UUID.randomUUID()).build();
    DocumentationUnitProcessStepDTO documentationUnitProcessStepDTO = null;

    assertThat(
            DocumentationUnitProcessStepTransformer.toDomain(
                documentationUnitProcessStepDTO, processStep))
        .isNull();
  }
}
