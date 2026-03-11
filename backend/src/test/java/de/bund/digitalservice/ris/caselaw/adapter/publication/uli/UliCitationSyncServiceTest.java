package de.bund.digitalservice.ris.caselaw.adapter.publication.uli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
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
import de.bund.digitalservice.ris.caselaw.adapter.publication.PortalPublicationService;
import de.bund.digitalservice.ris.caselaw.domain.exception.DocumentationUnitNotExistsException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@ExtendWith(SpringExtension.class)
@Import({UliCitationSyncService.class})
class UliCitationSyncServiceTest {

  @Autowired UliCitationSyncService uliCitationSyncService;

  @MockitoBean DatabaseDocumentationUnitRepository caselawRepository;
  @MockitoBean DatabaseUliRepository databaseUliRepository;
  @MockitoBean RevokedUliRepository revokedUliRepository;
  @MockitoBean JobSyncStatusRepository jobSyncStatusRepository;
  @MockitoBean ActiveCitationUliCaselawRepository activeCitationUliCaselawRepository;
  @MockitoBean PortalPublicationService portalPublicationService;
  @MockitoBean TransactionTemplate transactionTemplate;

  @BeforeEach
  void beforeEach() {
    when(transactionTemplate.execute(any()))
        .thenAnswer(
            invocation ->
                invocation
                    .<TransactionCallback<?>>getArgument(0)
                    .doInTransaction(mock(TransactionStatus.class)));
  }

  @Nested
  class handleNewlyPublishedAfter {

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
              .sourceId(null)
              .sourceAuthor("Old Author")
              .citation("Old Citation")
              .target(decision)
              .build();

      decision.getPassiveUliCitations().add(passiveCitation);

      when(caselawRepository.findById(caselawId)).thenReturn(Optional.of(decision));

      uliCitationSyncService.handleNewlyPublishedAfter(lastRun);
      // Todo: uncomment this, when we want to update the metadata
      //      assertThat(passiveCitation.getSourceAuthor()).isEqualTo("New Author");
      //      assertThat(passiveCitation.getSourceCitation()).isEqualTo("New Citation");
      assertThat(passiveCitation.getSourceId()).isEqualTo(uliId);

      verify(caselawRepository).save(decision);
      verify(portalPublicationService).publishDocumentationUnitWithChangelog(caselawId, null);

      assertThat(memoryAppender.getMessage(Level.INFO, 0))
          .contains("Updating metadata of matching passive citation");

      memoryAppender.detachLoggingTestAppender();
    }

    @Test
    void shouldCreateNewPassiveCitationWhenMissing() throws DocumentationUnitNotExistsException {
      var memoryAppender = new TestMemoryAppender(UliCitationSyncService.class);
      UUID uliId = UUID.randomUUID();
      UUID caselawId = UUID.randomUUID();
      Instant lastRun = Instant.now().minusSeconds(3600);

      UliDTO uliDTO =
          UliDTO.builder()
              .id(uliId)
              .documentNumber("ULI-DOC-1")
              .author("New Author")
              .activeCaselawReferences(
                  List.of(
                      UliActiveCaselawReferenceDTO.builder()
                          .targetDocumentationUnitId(caselawId)
                          .build()))
              .build();

      when(databaseUliRepository.findAllByPublishedAtAfter(lastRun)).thenReturn(List.of(uliDTO));

      DecisionDTO decision =
          DecisionDTO.builder()
              .id(caselawId)
              .documentNumber("XXRE123456789")
              .passiveUliCitations(new ArrayList<>())
              .build();

      when(caselawRepository.findById(caselawId)).thenReturn(Optional.of(decision));

      uliCitationSyncService.handleNewlyPublishedAfter(lastRun);

      // Todo: uncomment this, when we want to update the metadata

      //      assertThat(decision.getPassiveUliCitations()).hasSize(1);
      //      PassiveCitationUliDTO created = decision.getPassiveUliCitations().get(0);
      //      assertThat(created.getSourceAuthor()).isEqualTo("New Author");
      //      assertThat(created.getSourceLiteratureDocumentNumber()).isEqualTo("ULI-DOC-1");
      //
      //      verify(caselawRepository).save(decision);
      verify(portalPublicationService).publishDocumentationUnitWithChangelog(caselawId, null);

      assertThat(memoryAppender.getMessage(Level.INFO, 0))
          .contains("Creating missing passive citation");

      memoryAppender.detachLoggingTestAppender();
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

      DecisionDTO decision =
          DecisionDTO.builder().id(caselawId).passiveUliCitations(new ArrayList<>()).build();
      decision
          .getPassiveUliCitations()
          .add(
              PassiveCitationUliDTO.builder()
                  .sourceId(uliId)
                  .sourceAuthor("Same Author")
                  .target(decision)
                  .build());

      when(caselawRepository.findById(caselawId)).thenReturn(Optional.of(decision));

      uliCitationSyncService.handleNewlyPublishedAfter(Instant.now());

      verify(caselawRepository, never()).save(any());
      verify(portalPublicationService, never()).publishDocumentationUnitWithChangelog(any(), any());
    }

    @Test
    void shouldLogSuccessWhenRepublishSucceeds() throws DocumentationUnitNotExistsException {
      var memoryAppender = new TestMemoryAppender(UliCitationSyncService.class);
      UUID uliId = UUID.randomUUID();
      UUID caselawId = UUID.randomUUID();

      UliDTO uliDTO =
          UliDTO.builder()
              .id(uliId)
              .activeCaselawReferences(
                  List.of(
                      UliActiveCaselawReferenceDTO.builder()
                          .targetDocumentationUnitId(caselawId)
                          .build()))
              .build();

      when(databaseUliRepository.findAllByPublishedAtAfter(any())).thenReturn(List.of(uliDTO));
      when(caselawRepository.findById(caselawId))
          .thenReturn(
              Optional.of(
                  DecisionDTO.builder()
                      .id(caselawId)
                      .passiveUliCitations(new ArrayList<>())
                      .build()));

      uliCitationSyncService.handleNewlyPublishedAfter(Instant.now());

      verify(portalPublicationService).publishDocumentationUnitWithChangelog(caselawId, null);

      assertThat(memoryAppender.getMessage(Level.INFO, 1))
          .isEqualTo("Successfully republished after ULI newly published sync");

      memoryAppender.detachLoggingTestAppender();
    }

    @Test
    void shouldLogErrorWhenRepublishFails() throws DocumentationUnitNotExistsException {
      var memoryAppender = new TestMemoryAppender(UliCitationSyncService.class);
      UUID caselawId = UUID.randomUUID();

      UliDTO uliDTO =
          UliDTO.builder()
              .id(UUID.randomUUID())
              .activeCaselawReferences(
                  List.of(
                      UliActiveCaselawReferenceDTO.builder()
                          .targetDocumentationUnitId(caselawId)
                          .build()))
              .build();

      when(databaseUliRepository.findAllByPublishedAtAfter(any())).thenReturn(List.of(uliDTO));
      when(caselawRepository.findById(caselawId))
          .thenReturn(
              Optional.of(
                  DecisionDTO.builder()
                      .id(caselawId)
                      .passiveUliCitations(new ArrayList<>())
                      .build()));

      when(portalPublicationService.publishDocumentationUnitWithChangelog(caselawId, null))
          .thenThrow(new RuntimeException("S3 Connection Failed"));

      uliCitationSyncService.handleNewlyPublishedAfter(Instant.now());

      assertThat(memoryAppender.getMessage(Level.ERROR, 0))
          .isEqualTo("Failed to republish during ULI newly published sync");

      assertThat(memoryAppender.getKeyValuePairs(Level.ERROR, 0))
          .anyMatch(kv -> kv.key.equals("exception"));

      memoryAppender.detachLoggingTestAppender();
    }
  }

  @Nested
  class handleRevokedAfter {

    @Test
    void shouldRemovePassiveCitationAndRepublishWhenUliIsRevoked()
        throws DocumentationUnitNotExistsException {
      var memoryAppender = new TestMemoryAppender(UliCitationSyncService.class);
      UUID revokedUliId = UUID.randomUUID();
      UUID caselawId = UUID.randomUUID();
      Instant lastRun = Instant.now().minusSeconds(60);

      RevokedUli revokedEntry = RevokedUli.builder().docUnitId(revokedUliId).build();
      when(revokedUliRepository.findAllByRevokedAtAfter(lastRun)).thenReturn(List.of(revokedEntry));

      DecisionDTO decision =
          DecisionDTO.builder()
              .id(caselawId)
              .documentNumber("XXRE123456789")
              .passiveUliCitations(new ArrayList<>())
              .build();
      decision
          .getPassiveUliCitations()
          .add(PassiveCitationUliDTO.builder().sourceId(revokedUliId).build());

      when(caselawRepository.findAllByPassiveUliSourceIdAndPendingRevocation(revokedUliId))
          .thenReturn(List.of(decision));
      when(caselawRepository.findAllByActiveUliTargetIdAndPendingRevocation(any()))
          .thenReturn(List.of());

      uliCitationSyncService.handleRevokedAfter(lastRun);
      // Todo: uncomment this, when we want to update the metadata
      //      assertThat(decision.getPassiveUliCitations()).isEmpty();
      //      verify(caselawRepository).save(decision);
      verify(portalPublicationService).publishDocumentationUnitWithChangelog(caselawId, null);

      assertThat(memoryAppender.getMessage(Level.INFO, 1))
          .contains("Passive Citation to revoked ULI detected");
      assertThat(memoryAppender.getMessage(Level.INFO, 2))
          .contains("Successfully republished after revoked ULI sync");

      memoryAppender.detachLoggingTestAppender();
    }

    @Test
    void shouldRepublishAffectedActiveCitationsWhenUliIsRevoked()
        throws DocumentationUnitNotExistsException {
      UUID revokedUliId = UUID.randomUUID();
      UUID caselawId = UUID.randomUUID();
      Instant lastRun = Instant.now();

      when(revokedUliRepository.findAllByRevokedAtAfter(lastRun))
          .thenReturn(List.of(RevokedUli.builder().docUnitId(revokedUliId).build()));

      DecisionDTO decision =
          DecisionDTO.builder().id(caselawId).documentNumber("XXRE123456789").build();

      when(caselawRepository.findAllByPassiveUliSourceIdAndPendingRevocation(any()))
          .thenReturn(List.of());
      when(caselawRepository.findAllByActiveUliTargetIdAndPendingRevocation(revokedUliId))
          .thenReturn(List.of(decision));

      uliCitationSyncService.handleRevokedAfter(lastRun);

      // Active citations are only republished, not explicitly saved/modified in this step
      verify(caselawRepository, never()).save(any());
      verify(portalPublicationService).publishDocumentationUnitWithChangelog(caselawId, null);
    }

    @Test
    void shouldDoNothingWhenNoRevokedEntriesFound() throws DocumentationUnitNotExistsException {
      Instant lastRun = Instant.now();
      when(revokedUliRepository.findAllByRevokedAtAfter(lastRun)).thenReturn(List.of());

      uliCitationSyncService.handleRevokedAfter(lastRun);

      verify(portalPublicationService, never()).publishDocumentationUnitWithChangelog(any(), any());
    }
  }
}
