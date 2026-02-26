package de.bund.digitalservice.ris.caselaw.adapter.publication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationUliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationUliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PublishedUli;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PublishedUliRepository;
import de.bund.digitalservice.ris.caselaw.adapter.publication.uli.UliCitationPublishService;
import java.util.Optional;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({UliCitationPublishService.class})
public class UliCitationPublishServiceTest {

  @Autowired UliCitationPublishService uliCitationPublishService;

  @MockitoBean PublishedUliRepository publishedUliRepository;

  @Nested
  class updatePassiveUliCitationWithInformationFromSource {
    @Test
    void shouldReturnSameReferenceIfNoDocumentNumberIsGiven() {
      var passiveCitation =
          PassiveCitationUliDTO.builder().sourceAuthor("Test Autor").rank(1).build();

      var result =
          uliCitationPublishService.updatePassiveUliCitationWithInformationFromSource(
              passiveCitation);

      assertThat(result).contains(passiveCitation);
      assertThat(result.get().getSourceAuthor()).isEqualTo("Test Autor");
      assertThat(result.get().getSourceCitation()).isNull();
      assertThat(result.get().getSourceLiteratureDocumentNumber()).isNull();
    }

    @Test
    void shouldReturnEmptyIfDocumentNumberIsGivenButNoUliIsFound() {
      var passiveCitation =
          PassiveCitationUliDTO.builder().sourceLiteratureDocumentNumber("ULI-123").rank(1).build();

      when(publishedUliRepository.findByDocumentNumber("ULI-123")).thenReturn(Optional.empty());

      var result =
          uliCitationPublishService.updatePassiveUliCitationWithInformationFromSource(
              passiveCitation);

      assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnUpdatedPassiveCitationWhenUliIsFound() {
      var passiveCitation =
          PassiveCitationUliDTO.builder().sourceLiteratureDocumentNumber("ULI-123").rank(1).build();

      var publishedUli = mock(PublishedUli.class);
      when(publishedUli.getAuthor()).thenReturn("Dr. Test");
      when(publishedUli.getCitation()).thenReturn("NJW 2026, 1");
      when(publishedUli.getDocumentTypeRawValue()).thenReturn("Aufsatz");
      when(publishedUli.getLegalPeriodicalRawValue()).thenReturn("NJW");

      when(publishedUliRepository.findByDocumentNumber("ULI-123"))
          .thenReturn(Optional.of(publishedUli));

      var result =
          uliCitationPublishService.updatePassiveUliCitationWithInformationFromSource(
              passiveCitation);

      assertThat(result).contains(passiveCitation);
      assertThat(result.get().getSourceAuthor()).isEqualTo("Dr. Test");
      assertThat(result.get().getSourceCitation()).isEqualTo("NJW 2026, 1");
      assertThat(result.get().getSourceDocumentTypeRawValue()).isEqualTo("Aufsatz");
      assertThat(result.get().getSourceLegalPeriodicalRawValue()).isEqualTo("NJW");
    }
  }

  @Nested
  class updateActiveUliCitationWithInformationFromTarget {
    @Test
    void shouldReturnSameReferenceIfNoDocumentNumberIsGiven() {
      var activeCitation =
          ActiveCitationUliDTO.builder()
              .source(new DecisionDTO())
              .targetAuthor("Original Autor")
              .targetCitation("citation")
              .rank(1)
              .build();

      var result =
          uliCitationPublishService.updateActiveUliCitationWithInformationFromTarget(
              activeCitation);

      assertThat(result).isEqualTo(activeCitation);
      assertThat(result.getTargetAuthor()).isEqualTo("Original Autor");
      assertThat(result.getTargetCitation()).isEqualTo("citation");
      assertThat(result.getTargetLiteratureDocumentNumber()).isNull();
    }

    @Test
    void shouldReturnSameReferenceWithNullDocumentNumberIfUliIsNotFound() {
      var activeCitation =
          ActiveCitationUliDTO.builder()
              .source(new DecisionDTO())
              .targetLiteratureDocumentNumber("ULI-123")
              .targetCitation("citation")
              .rank(1)
              .build();

      when(publishedUliRepository.findByDocumentNumber("ULI-123")).thenReturn(Optional.empty());

      var result =
          uliCitationPublishService.updateActiveUliCitationWithInformationFromTarget(
              activeCitation);

      assertThat(result).isEqualTo(activeCitation);
      assertThat(result.getTargetLiteratureDocumentNumber()).isNull();
    }

    @Test
    void shouldReturnUpdatedActiveCitationWhenUliIsFound() {
      var activeCitation =
          ActiveCitationUliDTO.builder()
              .source(new DecisionDTO())
              .targetLiteratureDocumentNumber("ULI-123")
              .targetCitation("citation")
              .rank(1)
              .build();

      var publishedUli = mock(PublishedUli.class);
      when(publishedUli.getAuthor()).thenReturn("Prof. Dr. ULI");
      when(publishedUli.getCitation()).thenReturn("JZ 2026, 500");
      when(publishedUli.getLegalPeriodicalRawValue()).thenReturn("JZ");

      when(publishedUliRepository.findByDocumentNumber("ULI-123"))
          .thenReturn(Optional.of(publishedUli));

      var result =
          uliCitationPublishService.updateActiveUliCitationWithInformationFromTarget(
              activeCitation);

      assertThat(result).isEqualTo(activeCitation);
      assertThat(result.getTargetAuthor()).isEqualTo("Prof. Dr. ULI");
      assertThat(result.getTargetCitation()).isEqualTo("JZ 2026, 500");
      assertThat(result.getTargetLegalPeriodicalRawValue()).isEqualTo("JZ");
      assertThat(result.getTargetLiteratureDocumentNumber()).isEqualTo("ULI-123");
    }
  }
}
