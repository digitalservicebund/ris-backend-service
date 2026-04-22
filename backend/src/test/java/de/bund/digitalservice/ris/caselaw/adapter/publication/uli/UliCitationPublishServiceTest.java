package de.bund.digitalservice.ris.caselaw.adapter.publication.uli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import de.bund.digitalservice.ris.caselaw.TestMemoryAppender;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationUliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseUliRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationUliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.UliDTO;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Disabled;
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

  @MockitoBean DatabaseUliRepository databaseUliRepository;

  @Nested
  class updatePassiveUliCitationWithInformationFromSource {

    @Test
    void shouldUnlinkAndLogInfoIfUliIsNotFound() {
      var memoryAppender = new TestMemoryAppender(UliCitationPublishService.class);
      UUID sourceId = UUID.randomUUID();
      var passiveCitation =
          PassiveCitationUliDTO.builder()
              .sourceId(sourceId)
              .sourceLiteratureDocumentNumber("ULI-123")
              .rank(1)
              .build();

      when(databaseUliRepository.findById(sourceId)).thenReturn(Optional.empty());
      when(databaseUliRepository.findByDocumentNumber("ULI-123")).thenReturn(Optional.empty());

      var result =
          uliCitationPublishService.updatePassiveUliCitationWithInformationFromSource(
              passiveCitation);

      assertThat(result).isPresent();
      assertThat(result.get().getSourceId()).isNull();
      assertThat(result.get().getSourceLiteratureDocumentNumber()).isNull();

      assertThat(memoryAppender.count(Level.WARN)).isEqualTo(1L);
      assertThat(memoryAppender.getMessage(Level.WARN, 0))
          .contains("Unlinking passive citation: source ULI document not found in database.");

      memoryAppender.detachLoggingTestAppender();
    }

    @Test
    @Disabled(
        "Currently enrichment is disabled in UliCitationPublishService; only metadata divergence is logged. Re-enable once the service updates the DTO fields again.")
    void shouldFindUliByDocumentNumberIfIdMatchesNothing() {
      UUID unknownId = UUID.randomUUID();
      UUID foundId = UUID.randomUUID();
      var passiveCitation =
          PassiveCitationUliDTO.builder()
              .sourceId(unknownId)
              .sourceLiteratureDocumentNumber("ULI-123")
              .target(DecisionDTO.builder().documentNumber("CASELAW-123").build())
              .build();

      UliDTO uli =
          UliDTO.builder().id(foundId).documentNumber("ULI-123").author("Fallback Author").build();

      when(databaseUliRepository.findById(unknownId)).thenReturn(Optional.empty());
      when(databaseUliRepository.findByDocumentNumber("ULI-123")).thenReturn(Optional.of(uli));

      var result =
          uliCitationPublishService.updatePassiveUliCitationWithInformationFromSource(
              passiveCitation);

      assertThat(result).isPresent();
      assertThat(result.get().getSourceId()).isEqualTo(foundId);
      assertThat(result.get().getSourceAuthor()).isEqualTo("Fallback Author");
    }

    @Test
    @Disabled(
        "Currently enrichment is disabled in UliCitationPublishService; only metadata divergence is logged. Re-enable once the service updates the DTO fields again.")
    void shouldEnrichPassiveCitationWhenUliIsFound() {
      var memoryAppender = new TestMemoryAppender(UliCitationPublishService.class);
      UUID uliId = UUID.randomUUID();
      var passiveCitation = PassiveCitationUliDTO.builder().sourceId(uliId).build();

      UliDTO uli =
          UliDTO.builder()
              .id(uliId)
              .documentNumber("ULI-123")
              .author("Dr. Test")
              .citation("NJW 2026, 1")
              .documentTypeRawValue("Aufsatz")
              .legalPeriodicalRawValue("NJW")
              .build();

      when(databaseUliRepository.findById(uliId)).thenReturn(Optional.of(uli));

      var result =
          uliCitationPublishService.updatePassiveUliCitationWithInformationFromSource(
              passiveCitation);

      assertThat(result).isPresent();
      assertThat(result.get().getSourceAuthor()).isEqualTo("Dr. Test");
      assertThat(result.get().getCitation()).isEqualTo("NJW 2026, 1");
      assertThat(result.get().getSourceLiteratureDocumentNumber()).isEqualTo("ULI-123");

      assertThat(memoryAppender.count(Level.INFO)).isEqualTo(1L);
      assertThat(memoryAppender.getMessage(Level.INFO, 0))
          .contains("Enriched passive citation with metadata from ULI source document.");

      memoryAppender.detachLoggingTestAppender();
    }
  }

  @Nested
  class updateActiveUliCitationWithInformationFromTarget {

    @Test
    void shouldUnlinkAndLogInfoIfUliIsNotFound() {
      var memoryAppender = new TestMemoryAppender(UliCitationPublishService.class);
      var activeCitation =
          ActiveCitationUliDTO.builder()
              .source(DecisionDTO.builder().documentNumber("CAS-123").build())
              .targetLiteratureDocumentNumber("MISSING-404")
              .legalPeriodicalRawValue("AB")
              .citation("Citation from caselaw document")
              .build();

      when(databaseUliRepository.findByDocumentNumber("MISSING-404")).thenReturn(Optional.empty());

      var result =
          uliCitationPublishService.updateActiveUliCitationWithInformationFromTarget(
              activeCitation);

      assertThat(result.getTargetLiteratureDocumentNumber()).isNull();
      assertThat(memoryAppender.count(Level.INFO)).isEqualTo(1L);
      assertThat(memoryAppender.getMessage(Level.INFO, 0))
          .contains("Unlinking active citation: target ULI document not found in database.");
      assertThat(memoryAppender.getKeyValuePairs(Level.INFO, 0))
          .anyMatch(kv -> kv.key.equals("sourceDocumentNumber") && kv.value.equals("CAS-123"))
          .anyMatch(
              kv ->
                  kv.key.equals("missingTargetUliDocumentNumber")
                      && kv.value.equals("MISSING-404"));

      memoryAppender.detachLoggingTestAppender();
    }

    @Test
    void shouldLogDivergentStatesOfActiveCitationWhenUliIsFound() {
      var memoryAppender = new TestMemoryAppender(UliCitationPublishService.class);
      UUID caselawId = UUID.randomUUID();

      ActiveCitationUliDTO activeCitation =
          ActiveCitationUliDTO.builder()
              .source(DecisionDTO.builder().id(caselawId).documentNumber("CAS-123").build())
              .targetAuthor("Author from caselaw document")
              .targetLiteratureDocumentNumber("ULI-123")
              .citation("Citation from caselaw document")
              .legalPeriodicalRawValue("AB")
              .build();

      UliDTO uli =
          UliDTO.builder()
              .documentNumber("ULI-123")
              .author("Prof. Dr. ULI")
              .citation("JZ 2026, 500")
              .legalPeriodicalRawValue("JZ")
              .build();

      when(databaseUliRepository.findByDocumentNumber("ULI-123")).thenReturn(Optional.of(uli));

      uliCitationPublishService.updateActiveUliCitationWithInformationFromTarget(activeCitation);

      assertThat(memoryAppender.count(Level.INFO)).isEqualTo(1L);
      assertThat(memoryAppender.getMessage(Level.INFO, 0))
          .contains(
              "Metadata divergence detected between caselaw active citation and target uli document.");
      assertThat(memoryAppender.getKeyValuePairs(Level.INFO, 0))
          .anyMatch(kv -> kv.key.equals("sourceDocumentNumber") && kv.value.equals("CAS-123"))
          .anyMatch(kv -> kv.key.equals("target.documentNumber") && kv.value.equals("ULI-123"))
          .anyMatch(kv -> kv.key.equals("target.author") && kv.value.equals("Prof. Dr. ULI"));

      memoryAppender.detachLoggingTestAppender();
    }
  }
}
