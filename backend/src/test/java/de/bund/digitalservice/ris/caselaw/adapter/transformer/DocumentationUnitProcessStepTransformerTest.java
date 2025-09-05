package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationUnitProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcessStepDTO;
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
    ProcessStepDTO processStepDTO = ProcessStepDTO.builder().id(UUID.randomUUID()).build();
    DocumentationUnitProcessStepDTO documentationUnitProcessStepDTO =
        DocumentationUnitProcessStepDTO.builder()
            .id(documentationUnitProcessStepId)
            .user(UserTransformer.transformToDTO(user))
            .createdAt(createdAt)
            .processStep(processStepDTO)
            .build();
    DocumentationUnitProcessStep documentationUnitProcessStep =
        DocumentationUnitProcessStep.builder()
            .id(documentationUnitProcessStepId)
            .user(user)
            .createdAt(createdAt)
            .processStep(ProcessStep.builder().uuid(processStepDTO.getId()).build())
            .build();

    assertThat(DocumentationUnitProcessStepTransformer.toDomain(documentationUnitProcessStepDTO))
        .isEqualTo(documentationUnitProcessStep);
  }

  @Test
  void shouldNotTransformToDomain_ifProcessStepNull() {
    assertThat(DocumentationUnitProcessStepTransformer.toDomain(null)).isNull();
  }

  @Test
  void shouldNotTransformToDomain_ifDocumentationUnitProcessStepDTONull() {
    DocumentationUnitProcessStepDTO documentationUnitProcessStepDTO = null;

    assertThat(DocumentationUnitProcessStepTransformer.toDomain(documentationUnitProcessStepDTO))
        .isNull();
  }
}
