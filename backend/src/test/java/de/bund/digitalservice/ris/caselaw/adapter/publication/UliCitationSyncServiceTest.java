package de.bund.digitalservice.ris.caselaw.adapter.publication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import de.bund.digitalservice.ris.caselaw.TestMemoryAppender;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationUliCaselawRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseUliRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JobSyncStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationUliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RevokedUli;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RevokedUliRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.UliActiveCaselawReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.UliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.publication.uli.UliCitationSyncService;
import de.bund.digitalservice.ris.caselaw.domain.LoggingKeys;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@Import({UliCitationSyncService.class})
public class UliCitationSyncServiceTest {

  @Autowired UliCitationSyncService uliCitationSyncService;

  @MockitoBean DatabaseDocumentationUnitRepository caselawRepository;
  @MockitoBean DatabaseUliRepository databaseUliRepository;
  @MockitoBean RevokedUliRepository revokedUliRepository;
  @MockitoBean JobSyncStatusRepository jobSyncStatusRepository;
  @MockitoBean ActiveCitationUliCaselawRepository activeCitationUliCaselawRepository;
  @MockitoBean PortalPublicationService portalPublicationService;

  @Nested
  class handleUliPassiveSync {

    @Test
    void shouldUpdateMetadataWhenPassiveCitationExists()
        throws DocumentationUnitNotExistsException {
      var memoryAppender = new TestMemoryAppender(UliCitationSyncService.class);
      UUID uliId = UUID.randomUUID();
      UUID caselawId = UUID.randomUUID();
      Instant lastRun = Instant.now().minusSeconds(3600);

      UliActiveCaselawReferenceDTO link =
          UliActiveCaselawReferenceDTO.builder().targetDocumentationUnitId(caselawId).build();

      UliDTO uliDTO =
          UliDTO.builder()
              .id(uliId)
              .author("New Author")
              .citation("New Citation")
              .activeCaselawReferences(List.of(link))
              .build();

      when(databaseUliRepository.findAllByPublishedAtAfter(lastRun)).thenReturn(List.of(uliDTO));
      DecisionDTO decision =
          DecisionDTO.builder()
              .id(caselawId)
              .documentNumber("XXRE123456789")
              .passiveUliCitations(new ArrayList<>())
              .build();

      PassiveCitationUliDTO passiveCitation =
          PassiveCitationUliDTO.builder()
              .sourceId(uliId)
              .sourceAuthor("Old Author")
              .target(decision)
              .build();

      decision.getPassiveUliCitations().add(passiveCitation);

      when(caselawRepository.findById(caselawId)).thenReturn(Optional.of(decision));

      // Execute
      uliCitationSyncService.handleNewlyPublishedAfter(lastRun);

      // Verify
      assertThat(passiveCitation.getSourceAuthor()).isEqualTo("New Author");
      assertThat(passiveCitation.getSourceCitation()).isEqualTo("New Citation");

      assertThat(memoryAppender.count(Level.INFO)).isEqualTo(1L);
      assertThat(memoryAppender.getKeyValuePairs(Level.INFO, 0))
          .anyMatch(
              kv -> kv.key.equals(LoggingKeys.SOURCE_DOCUMENT_NUMBER) && kv.value.equals(uliId))
          .anyMatch(kv -> kv.key.equals("caselawDocNumber") && kv.value.equals("XXRE123456789"));

      assertThat(memoryAppender.getMessage(Level.INFO, 0))
          .contains("Updating metadata of matching passive citation");

      verify(caselawRepository).save(decision);
      verify(portalPublicationService).publishDocumentationUnitWithChangelog(caselawId, null);
      memoryAppender.detachLoggingTestAppender();
    }

    @Test
    void shouldLogWarningWhenPassiveCitationIsMissing() throws DocumentationUnitNotExistsException {
      var memoryAppender = new TestMemoryAppender(UliCitationSyncService.class);
      UUID uliId = UUID.randomUUID();
      UUID caselawId = UUID.randomUUID();
      Instant lastRun = Instant.now();

      UliActiveCaselawReferenceDTO link =
          UliActiveCaselawReferenceDTO.builder().targetDocumentationUnitId(caselawId).build();

      UliDTO uliDTO = UliDTO.builder().id(uliId).activeCaselawReferences(List.of(link)).build();
      when(databaseUliRepository.findAllByPublishedAtAfter(lastRun)).thenReturn(List.of(uliDTO));

      DecisionDTO decision =
          DecisionDTO.builder()
              .id(caselawId)
              .documentNumber("XXRE123456789")
              .passiveUliCitations(new ArrayList<>()) // empty
              .build();

      when(caselawRepository.findById(caselawId)).thenReturn(Optional.of(decision));

      // Execute
      uliCitationSyncService.handleNewlyPublishedAfter(lastRun);

      // Verify
      verify(caselawRepository, never()).save(any());
      verify(portalPublicationService, never()).publishDocumentationUnit(any());
      assertThat(memoryAppender.count(Level.WARN)).isEqualTo(1L);
      assertThat(memoryAppender.getKeyValuePairs(Level.WARN, 0))
          .anyMatch(
              kv -> kv.key.equals(LoggingKeys.SOURCE_DOCUMENT_NUMBER) && kv.value.equals(uliId))
          .anyMatch(kv -> kv.key.equals(LoggingKeys.DOCUMENT_ID) && kv.value.equals(caselawId));
      assertThat(memoryAppender.getMessage(Level.WARN, 0))
          .contains(
              "Inconsistency: Active citation in ULI points to caselaw, but passive counterpart is missing in caselaw document.");

      memoryAppender.detachLoggingTestAppender();
    }

    @Test
    void shouldDoNothingIfNoNewUlisFound() throws DocumentationUnitNotExistsException {
      var memoryAppender = new TestMemoryAppender(UliCitationSyncService.class);
      Instant lastRun = Instant.now();
      when(databaseUliRepository.findAllByPublishedAtAfter(lastRun)).thenReturn(List.of());

      uliCitationSyncService.handleNewlyPublishedAfter(lastRun);

      verify(caselawRepository, never()).findById(any());
      verify(caselawRepository, never()).save(any());
      verify(portalPublicationService, never()).publishDocumentationUnit(any());

      assertThat(memoryAppender.count(Level.INFO)).isEqualTo(1L);
      assertThat(memoryAppender.getMessage(Level.INFO, 0))
          .contains("No documents found for republishing. Skipping sync step.");

      memoryAppender.detachLoggingTestAppender();
    }

    @Test
    void shouldUpdateMetadataWhenMatchingByDocumentNumberInsteadOfId() {
      UUID uliId = UUID.randomUUID();
      UUID caselawId = UUID.randomUUID();
      String docNumber = "ULI-2026-100";

      UliDTO uliDTO =
          UliDTO.builder()
              .id(uliId)
              .documentNumber(docNumber)
              .author("New Author")
              .activeCaselawReferences(
                  List.of(
                      UliActiveCaselawReferenceDTO.builder()
                          .targetDocumentationUnitId(caselawId)
                          .build()))
              .build();

      when(databaseUliRepository.findAllByPublishedAtAfter(any())).thenReturn(List.of(uliDTO));

      DecisionDTO decision =
          DecisionDTO.builder().id(caselawId).passiveUliCitations(new ArrayList<>()).build();
      // passive citation has no id (yet) but matches by document number
      PassiveCitationUliDTO passive =
          PassiveCitationUliDTO.builder()
              .sourceId(null)
              .sourceLiteratureDocumentNumber(docNumber)
              .sourceAuthor("Old Author")
              .target(decision)
              .build();

      decision.getPassiveUliCitations().add(passive);

      when(caselawRepository.findById(caselawId)).thenReturn(Optional.of(decision));

      uliCitationSyncService.handleNewlyPublishedAfter(Instant.now());

      assertThat(passive.getSourceAuthor()).isEqualTo("New Author");
      assertThat(passive.getSourceId()).isEqualTo(uliId);
      verify(caselawRepository).save(decision);
    }

    @Test
    void shouldNotUpdateOrPublishIfMetadataIsAlreadyIdentical()
        throws DocumentationUnitNotExistsException {
      UUID uliId = UUID.randomUUID();
      UUID caselawId = UUID.randomUUID();

      UliDTO uliDTO =
          UliDTO.builder()
              .id(uliId)
              .author("Same Author")
              .activeCaselawReferences(
                  List.of(
                      UliActiveCaselawReferenceDTO.builder()
                          .targetDocumentationUnitId(caselawId)
                          .build()))
              .build();

      when(databaseUliRepository.findAllByPublishedAtAfter(any())).thenReturn(List.of(uliDTO));

      PassiveCitationUliDTO passive =
          PassiveCitationUliDTO.builder().sourceId(uliId).sourceAuthor("Same Author").build();

      DecisionDTO decision =
          DecisionDTO.builder()
              .id(caselawId)
              .passiveUliCitations(new ArrayList<>(List.of(passive)))
              .build();

      when(caselawRepository.findById(caselawId)).thenReturn(Optional.of(decision));

      uliCitationSyncService.handleNewlyPublishedAfter(Instant.now());

      verify(caselawRepository, never()).save(any());
      verify(portalPublicationService, never()).publishDocumentationUnitWithChangelog(any(), any());
    }
  }

  @Nested
  class handleRevokedAfter {
    @Test
    void shouldDoNothingWhenNoUliRevokedAndLog() throws DocumentationUnitNotExistsException {
      var memoryAppender = new TestMemoryAppender(UliCitationSyncService.class);
      Instant lastRun = Instant.now();

      when(revokedUliRepository.findAllByRevokedAtAfter(lastRun)).thenReturn(List.of());

      uliCitationSyncService.handleRevokedAfter(lastRun);

      verify(caselawRepository, never()).findAllByPassiveUliSourceIdAndPendingRevocation(any());
      verify(caselawRepository, never()).findAllByActiveUliTargetIdAndPendingRevocation(any());
      verify(portalPublicationService, never()).publishDocumentationUnitWithChangelog(any(), any());

      assertThat(memoryAppender.count(Level.INFO)).isEqualTo(1L);
      assertThat(memoryAppender.getMessage(Level.INFO, 0)).contains("No new revoked entries");
      assertThat(memoryAppender.getKeyValuePairs(Level.INFO, 0))
          .anyMatch(kv -> kv.key.equals("lastRun") && kv.value.equals(lastRun));

      memoryAppender.detachLoggingTestAppender();
    }

    @Test
    void syncCitationsForRevokedUli_shouldLogErrorAndSkipWhenDocUnitIdIsNull()
        throws DocumentationUnitNotExistsException {
      var memoryAppender = new TestMemoryAppender(UliCitationSyncService.class);
      Instant lastRun = Instant.now();

      RevokedUli invalidEntry = RevokedUli.builder().docUnitId(null).build();
      when(revokedUliRepository.findAllByRevokedAtAfter(lastRun)).thenReturn(List.of(invalidEntry));

      uliCitationSyncService.handleRevokedAfter(lastRun);

      verify(caselawRepository, never()).findAllByPassiveUliSourceIdAndPendingRevocation(any());
      verify(caselawRepository, never()).findAllByActiveUliTargetIdAndPendingRevocation(any());
      verify(portalPublicationService, never()).publishDocumentationUnitWithChangelog(any(), any());

      assertThat(memoryAppender.count(Level.ERROR)).isEqualTo(1L);
      assertThat(memoryAppender.getMessage(Level.ERROR, 0))
          .contains("RevokedUli entry found without docUnitId. Skipping sync for this entry.");

      assertThat(memoryAppender.getKeyValuePairs(Level.ERROR, 0))
          .anyMatch(kv -> kv.key.equals("revokedUliEntry") && kv.value.equals(invalidEntry));

      memoryAppender.detachLoggingTestAppender();
    }

    @Test
    void shouldLogAndRepublishAffectedPassiveCitationsWhenUliIsRevoked()
        throws DocumentationUnitNotExistsException {
      var memoryAppender = new TestMemoryAppender(UliCitationSyncService.class);
      UUID revokedUliId = UUID.randomUUID();
      Instant lastRun = Instant.now().minusSeconds(60);

      RevokedUli revokedEntry = RevokedUli.builder().docUnitId(revokedUliId).build();
      when(revokedUliRepository.findAllByRevokedAtAfter(lastRun)).thenReturn(List.of(revokedEntry));

      PassiveCitationUliDTO passiveToRemove =
          PassiveCitationUliDTO.builder().sourceId(revokedUliId).build();

      DecisionDTO decision =
          DecisionDTO.builder()
              .documentNumber("XXRE123456789")
              .passiveUliCitations(new ArrayList<>(List.of(passiveToRemove)))
              .build();

      when(caselawRepository.findAllByPassiveUliSourceIdAndPendingRevocation(revokedUliId))
          .thenReturn(List.of(decision));
      when(caselawRepository.findAllByActiveUliTargetIdAndPendingRevocation(any()))
          .thenReturn(List.of());

      // Execute
      uliCitationSyncService.handleRevokedAfter(lastRun);

      // Verify
      // For now we just log and add for republish to unlink the targetId
      assertThat(memoryAppender.count(Level.INFO)).isGreaterThanOrEqualTo(2L);

      assertThat(memoryAppender.getKeyValuePairs(Level.INFO, 1))
          .anyMatch(kv -> kv.key.equals(LoggingKeys.REVOKED_ULI) && kv.value.equals(revokedUliId))
          .anyMatch(
              kv -> kv.key.equals("affectedDocUnitNumber") && kv.value.equals("XXRE123456789"));

      assertThat(memoryAppender.getMessage(Level.INFO, 1))
          .contains("Passive Citation to revoked ULI detected");

      verify(caselawRepository, never()).save(any());
      verify(portalPublicationService)
          .publishDocumentationUnitWithChangelog(decision.getId(), null);

      memoryAppender.detachLoggingTestAppender();
    }

    @Test
    void shouldLogAndRepublishAffectedActiveCitationsWhenUliIsRevoked()
        throws DocumentationUnitNotExistsException {
      var memoryAppender = new TestMemoryAppender(UliCitationSyncService.class);

      UUID revokedUliId = UUID.randomUUID();
      Instant lastRun = Instant.now();

      RevokedUli revokedEntry = RevokedUli.builder().docUnitId(revokedUliId).build();
      when(revokedUliRepository.findAllByRevokedAtAfter(lastRun)).thenReturn(List.of(revokedEntry));

      DecisionDTO decision = DecisionDTO.builder().documentNumber("XXRE123456789").build();

      when(caselawRepository.findAllByPassiveUliSourceIdAndPendingRevocation(revokedUliId))
          .thenReturn(List.of());

      when(caselawRepository.findAllByActiveUliTargetIdAndPendingRevocation(revokedUliId))
          .thenReturn(List.of(decision));

      uliCitationSyncService.handleRevokedAfter(lastRun);

      assertThat(memoryAppender.count(Level.INFO)).isGreaterThanOrEqualTo(2L);

      assertThat(memoryAppender.getKeyValuePairs(Level.INFO, 1))
          .anyMatch(kv -> kv.key.equals(LoggingKeys.REVOKED_ULI) && kv.value.equals(revokedUliId))
          .anyMatch(
              kv -> kv.key.equals("affectedDocUnitNumber") && kv.value.equals("XXRE123456789"));

      assertThat(memoryAppender.getMessage(Level.INFO, 1))
          .contains("Active Citation to revoked ULI detected");

      verify(caselawRepository, never()).save(any());
      verify(portalPublicationService)
          .publishDocumentationUnitWithChangelog(decision.getId(), null);
      memoryAppender.detachLoggingTestAppender();
    }
  }
}
