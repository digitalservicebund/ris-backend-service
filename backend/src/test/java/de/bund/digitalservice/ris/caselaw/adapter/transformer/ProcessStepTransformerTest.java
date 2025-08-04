package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.assertj.core.api.Assertions.assertThat;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ProcessStepDTO;
import de.bund.digitalservice.ris.caselaw.domain.ProcessStep;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProcessStepTransformerTest {

  @Test
  void shouldTransformToDomain() {
    UUID processStepId = UUID.randomUUID();
    ProcessStepDTO processStepDTO =
        ProcessStepDTO.builder()
            .id(processStepId)
            .abbreviation("abbreviation")
            .name("name")
            .build();
    ProcessStep processStep =
        ProcessStep.builder().uuid(processStepId).abbreviation("abbreviation").name("name").build();

    assertThat(ProcessStepTransformer.toDomain(processStepDTO)).isEqualTo(processStep);
  }

  @Test
  void shouldNotTransformToDomain() {
    ProcessStepDTO processStepDTO = null;

    assertThat(ProcessStepTransformer.toDomain(processStepDTO)).isNull();
  }
}
