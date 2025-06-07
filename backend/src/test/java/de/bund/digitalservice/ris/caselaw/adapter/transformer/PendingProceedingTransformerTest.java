package de.bund.digitalservice.ris.caselaw.adapter.transformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentationOfficeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PendingProceedingDTO;
import de.bund.digitalservice.ris.caselaw.domain.PendingProceeding;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class PendingProceedingTransformerTest {

  @Test
  void testTransformToDomain_withPendingProceedingDTOIsNull_shouldThrowException() {
    assertThatThrownBy(() -> PendingProceedingTransformer.transformToDomain(null))
        .isInstanceOf(DocumentationUnitTransformerException.class)
        .hasMessageContaining("Pending proceeding is null and won't transform");
  }

  @Test
  void testTransformToDomain_withPendingProceedingFields() {
    PendingProceedingDTO decisionDTO =
        generateSimpleDTOBuilder()
            .appellant("appellant")
            .admissionOfAppeal("admission of appeal")
            .legalIssue("legal issue")
            .resolutionNote("resolution note")
            .isResolved(true)
            .resolutionDate(LocalDate.now())
            .build();
    PendingProceeding pendingProceeding =
        PendingProceedingTransformer.transformToDomain(decisionDTO);

    assertThat(pendingProceeding.appellant()).isEqualTo("appellant");
    assertThat(pendingProceeding.admissionOfAppeal()).isEqualTo("admission of appeal");
    assertThat(pendingProceeding.legalIssue()).isEqualTo("legal issue");
    assertThat(pendingProceeding.resolutionNote()).isEqualTo("resolution note");
    assertThat(pendingProceeding.coreData().isResolved()).isTrue();
    assertThat(pendingProceeding.coreData().resolutionDate()).isToday();
  }

  private PendingProceedingDTO.PendingProceedingDTOBuilder<?, ?> generateSimpleDTOBuilder() {
    return PendingProceedingDTO.builder()
        .documentationOffice(DocumentationOfficeDTO.builder().abbreviation("doc office").build());
  }
}
