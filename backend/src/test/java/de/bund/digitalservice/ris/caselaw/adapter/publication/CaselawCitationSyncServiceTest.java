package de.bund.digitalservice.ris.caselaw.adapter.publication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationCaselawDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CitationTypeDTO;
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
@Import({CaselawCitationSyncService.class})
public class CaselawCitationSyncServiceTest {

  @Autowired CaselawCitationSyncService caselawCitationSyncService;

  @MockitoBean DatabaseDocumentationUnitRepository documentationUnitRepository;

  @Nested
  class syncCitations {
    @Test
    void shouldDoNothingIfPassiveCitationsForAllActiveCitationsExist() {
      var citationType = CitationTypeDTO.builder().build();
      var court = CourtDTO.builder().build();
      var documentType = DocumentTypeDTO.builder().build();
      var publishedDocUnit =
          DecisionDTO.builder()
              .documentNumber("XXRE000000001")
              .court(court)
              .fileNumbers(List.of(FileNumberDTO.builder().value("XXX 0001").build()))
              .date(LocalDate.of(2020, 1, 1))
              .documentType(documentType)
              .activeCaselawCitations(
                  List.of(
                      ActiveCitationCaselawDTO.builder()
                          .citationType(citationType)
                          .rank(1)
                          .targetDocumentNumber("XXRE000000002")
                          .build()))
              .build();

      var docUnitXXRE000000002 =
          DecisionDTO.builder()
              .documentNumber("XXRE000000002")
              .passiveCaselawCitations(
                  List.of(
                      PassiveCitationCaselawDTO.builder()
                          .citationType(citationType)
                          .sourceDocumentNumber("XXRE000000001")
                          .sourceCourt(court)
                          .sourceFileNumber("XXX 0001")
                          .sourceDate(LocalDate.of(2020, 1, 1))
                          .sourceDocumentType(documentType)
                          .rank(1)
                          .build()))
              .build();

      when(documentationUnitRepository.findByDocumentNumber("XXRE000000002"))
          .thenReturn(Optional.of(docUnitXXRE000000002));

      var result = caselawCitationSyncService.syncCitations(publishedDocUnit);

      assertThat(result).isEmpty();
      verify(documentationUnitRepository, never()).save(any());
    }

    @Test
    void shouldCreatePassiveCitationsForActiveCitations() {
      var citationType = CitationTypeDTO.builder().build();
      var court = CourtDTO.builder().build();
      var documentType = DocumentTypeDTO.builder().build();
      var publishedDocUnit =
          DecisionDTO.builder()
              .documentNumber("XXRE000000001")
              .court(court)
              .fileNumbers(List.of(FileNumberDTO.builder().value("XXX 0001").build()))
              .date(LocalDate.of(2020, 1, 1))
              .documentType(documentType)
              .activeCaselawCitations(
                  List.of(
                      ActiveCitationCaselawDTO.builder()
                          .citationType(citationType)
                          .rank(1)
                          .targetDocumentNumber("XXRE000000002")
                          .build()))
              .build();

      var docUnitXXRE000000002 = DecisionDTO.builder().documentNumber("XXRE000000002").build();

      when(documentationUnitRepository.findByDocumentNumber("XXRE000000002"))
          .thenReturn(Optional.of(docUnitXXRE000000002));

      var result = caselawCitationSyncService.syncCitations(publishedDocUnit);

      assertThat(result).containsExactly("XXRE000000002");
      verify(documentationUnitRepository).save(docUnitXXRE000000002);
      assertThat(docUnitXXRE000000002.getPassiveCaselawCitations()).hasSize(1);
      var passiveCitation = docUnitXXRE000000002.getPassiveCaselawCitations().getFirst();
      assertThat(passiveCitation.getCitationType()).isEqualTo(citationType);
      assertThat(passiveCitation.getSourceDocumentNumber()).isEqualTo("XXRE000000001");
      assertThat(passiveCitation.getSourceDate()).isEqualTo(LocalDate.of(2020, 1, 1));
      assertThat(passiveCitation.getSourceCourt()).isEqualTo(court);
      assertThat(passiveCitation.getSourceFileNumber()).isEqualTo("XXX 0001");
      assertThat(passiveCitation.getSourceDocumentType()).isEqualTo(documentType);
    }

    @Test
    void shouldUpdatePassiveCitationsForMatchingActiveCitation() {
      var citationType = CitationTypeDTO.builder().build();
      var court = CourtDTO.builder().build();
      var documentType = DocumentTypeDTO.builder().build();
      var publishedDocUnit =
          DecisionDTO.builder()
              .documentNumber("XXRE000000001")
              .court(court)
              .fileNumbers(List.of(FileNumberDTO.builder().value("XXX 0001").build()))
              .date(LocalDate.of(2020, 1, 1))
              .documentType(documentType)
              .activeCaselawCitations(
                  List.of(
                      ActiveCitationCaselawDTO.builder()
                          .citationType(citationType)
                          .rank(1)
                          .targetDocumentNumber("XXRE000000002")
                          .build()))
              .build();

      var docUnitXXRE000000002 =
          DecisionDTO.builder()
              .documentNumber("XXRE000000002")
              .passiveCaselawCitations(
                  List.of(
                      PassiveCitationCaselawDTO.builder()
                          .sourceDocumentNumber("XXRE000000001")
                          .citationType(citationType)
                          .rank(1)
                          .build()))
              .build();

      when(documentationUnitRepository.findByDocumentNumber("XXRE000000002"))
          .thenReturn(Optional.of(docUnitXXRE000000002));

      var result = caselawCitationSyncService.syncCitations(publishedDocUnit);

      assertThat(result).containsExactly("XXRE000000002");
      verify(documentationUnitRepository).save(docUnitXXRE000000002);
      assertThat(docUnitXXRE000000002.getPassiveCaselawCitations()).hasSize(1);
      var passiveCitation = docUnitXXRE000000002.getPassiveCaselawCitations().getFirst();
      assertThat(passiveCitation.getCitationType()).isEqualTo(citationType);
      assertThat(passiveCitation.getSourceDocumentNumber()).isEqualTo("XXRE000000001");
      assertThat(passiveCitation.getSourceDate()).isEqualTo(LocalDate.of(2020, 1, 1));
      assertThat(passiveCitation.getSourceCourt()).isEqualTo(court);
      assertThat(passiveCitation.getSourceFileNumber()).isEqualTo("XXX 0001");
      assertThat(passiveCitation.getSourceDocumentType()).isEqualTo(documentType);
    }
  }
}
