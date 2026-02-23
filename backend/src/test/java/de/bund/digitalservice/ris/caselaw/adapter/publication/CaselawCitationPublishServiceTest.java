package de.bund.digitalservice.ris.caselaw.adapter.publication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationCaselawDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CourtDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DocumentTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.FileNumberDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationCaselawDTO;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({CaselawCitationPublishService.class})
public class CaselawCitationPublishServiceTest {

  @Autowired CaselawCitationPublishService caselawCitationPublishService;

  @MockitoBean DatabaseDocumentationUnitRepository documentationUnitRepository;

  @Nested
  class updatePassiveCitationSourceWithInformationFromSource {
    @Test
    void shouldReturnSameReferenceIfNoDocumentNumberIsGiven() {
      var passiveCitation =
          PassiveCitationCaselawDTO.builder().sourceFileNumber("XXX 2313").rank(1).build();

      var result =
          caselawCitationPublishService.updatePassiveCitationSourceWithInformationFromSource(
              passiveCitation);

      assertThat(result).contains(passiveCitation);
      assertThat(result.get().getSourceFileNumber()).isEqualTo("XXX 2313");
      assertThat(result.get().getSourceDate()).isNull();
      assertThat(result.get().getSourceCourt()).isNull();
      assertThat(result.get().getSourceDocumentNumber()).isNull();
      assertThat(result.get().getSourceDocumentType()).isNull();
      assertThat(result.get().getCitationType()).isNull();
    }

    @Test
    void shouldReturnEmptyIfDocumentNumberIsGivenButNoDocumentIsFound() {
      var passiveCitation =
          PassiveCitationCaselawDTO.builder()
              .sourceDocumentNumber("XXRE000714526")
              .sourceFileNumber("XXX 2313")
              .rank(1)
              .build();

      when(documentationUnitRepository.findByDocumentNumber("XXRE000714526"))
          .thenReturn(Optional.empty());

      var result =
          caselawCitationPublishService.updatePassiveCitationSourceWithInformationFromSource(
              passiveCitation);

      assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnUpdatedPassiveCitationWhenDocumentIsFound() {
      var passiveCitation =
          PassiveCitationCaselawDTO.builder().sourceDocumentNumber("XXRE000714526").rank(1).build();
      var court = CourtDTO.builder().build();
      var documentType = DocumentTypeDTO.builder().build();

      when(documentationUnitRepository.findByDocumentNumber("XXRE000714526"))
          .thenReturn(
              Optional.of(
                  DecisionDTO.builder()
                      .documentNumber("XXRE000714526")
                      .date(LocalDate.parse("2026-01-01"))
                      .court(court)
                      .fileNumbers(
                          List.of(
                              FileNumberDTO.builder().value("XXX 0001").build(),
                              FileNumberDTO.builder().value("XXX 0002").build()))
                      .documentType(documentType)
                      .build()));

      var result =
          caselawCitationPublishService.updatePassiveCitationSourceWithInformationFromSource(
              passiveCitation);

      assertThat(result).contains(passiveCitation);
      assertThat(result.get().getSourceDocumentNumber()).isEqualTo("XXRE000714526");
      assertThat(result.get().getSourceDate()).isEqualTo(LocalDate.parse("2026-01-01"));
      assertThat(result.get().getSourceCourt()).isEqualTo(court);
      assertThat(result.get().getSourceFileNumber()).isEqualTo("XXX 0001");
      assertThat(result.get().getSourceDocumentType()).isEqualTo(documentType);
      assertThat(result.get().getCitationType()).isNull();
    }
  }

  @Nested
  class updateActiveCitationTargetWithInformationFromTarget {
    @Test
    void shouldReturnSameReferenceIfNoDocumentNumberIsGiven() {
      var activeCitation =
          ActiveCitationCaselawDTO.builder().targetFileNumber("XXX 2313").rank(1).build();

      var result =
          caselawCitationPublishService.updateActiveCitationTargetWithInformationFromTarget(
              activeCitation);

      assertThat(result).isEqualTo(activeCitation);
      assertThat(result.getTargetFileNumber()).isEqualTo("XXX 2313");
      assertThat(result.getTargetDate()).isNull();
      assertThat(result.getTargetCourt()).isNull();
      assertThat(result.getTargetDocumentNumber()).isNull();
      assertThat(result.getTargetDocumentType()).isNull();
      assertThat(result.getCitationType()).isNull();
    }

    @Test
    void
        shouldReturnSameReferenceWithoutDocumentNumberIfDocumentNumberIsGivenButNoDocumentIsFound() {
      var activeCitation =
          ActiveCitationCaselawDTO.builder()
              .targetDocumentNumber("XXRE000714526")
              .targetFileNumber("XXX 2313")
              .rank(1)
              .build();

      when(documentationUnitRepository.findByDocumentNumber("XXRE000714526"))
          .thenReturn(Optional.empty());

      var result =
          caselawCitationPublishService.updateActiveCitationTargetWithInformationFromTarget(
              activeCitation);

      assertThat(result).isEqualTo(activeCitation);
      assertThat(result.getTargetFileNumber()).isEqualTo("XXX 2313");
      assertThat(result.getTargetDate()).isNull();
      assertThat(result.getTargetCourt()).isNull();
      assertThat(result.getTargetDocumentNumber()).isNull();
      assertThat(result.getTargetDocumentType()).isNull();
      assertThat(result.getCitationType()).isNull();
    }

    @Test
    void shouldReturnUpdatedActiveCitationWhenDocumentIsFound() {
      var activeCitation =
          ActiveCitationCaselawDTO.builder().targetDocumentNumber("XXRE000714526").rank(1).build();
      var court = CourtDTO.builder().build();
      var documentType = DocumentTypeDTO.builder().build();

      when(documentationUnitRepository.findByDocumentNumber("XXRE000714526"))
          .thenReturn(
              Optional.of(
                  DecisionDTO.builder()
                      .documentNumber("XXRE000714526")
                      .date(LocalDate.parse("2026-01-01"))
                      .court(court)
                      .fileNumbers(
                          List.of(
                              FileNumberDTO.builder().value("XXX 0001").build(),
                              FileNumberDTO.builder().value("XXX 0002").build()))
                      .documentType(documentType)
                      .build()));

      var result =
          caselawCitationPublishService.updateActiveCitationTargetWithInformationFromTarget(
              activeCitation);

      assertThat(result).isEqualTo(activeCitation);
      assertThat(result.getTargetDocumentNumber()).isEqualTo("XXRE000714526");
      assertThat(result.getTargetDate()).isEqualTo(LocalDate.parse("2026-01-01"));
      assertThat(result.getTargetCourt()).isEqualTo(court);
      assertThat(result.getTargetFileNumber()).isEqualTo("XXX 0001");
      assertThat(result.getTargetDocumentType()).isEqualTo(documentType);
      assertThat(result.getCitationType()).isNull();
    }
  }
}
