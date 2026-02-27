package de.bund.digitalservice.ris.caselaw.adapter.publication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationUliCaselaw;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationUliCaselawRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JobSyncStatus;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.JobSyncStatusRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationUliDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PublishedUli;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PublishedUliRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RevokedUli;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RevokedUliRepository;
import de.bund.digitalservice.ris.caselaw.adapter.publication.uli.UliCitationSyncService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
  @MockitoBean PublishedUliRepository publishedUliRepository;
  @MockitoBean RevokedUliRepository revokedUliRepository;
  @MockitoBean JobSyncStatusRepository jobSyncStatusRepository;
  @MockitoBean ActiveCitationUliCaselawRepository activeCitationUliCaselawRepository;

  @Nested
  class handleUliPassiveSync {

    @Test
    void shouldUpdateMetadataWhenPassiveCitationExists() {
      UUID uliId = UUID.randomUUID();
      UUID caselawId = UUID.randomUUID();
      Instant now = Instant.now();

      // Setup Job Status & Delta
      when(jobSyncStatusRepository.findById("ULI_PASSIVE_CITATION_SYNC"))
          .thenReturn(Optional.empty());

      PublishedUli publishedUli =
          PublishedUli.builder()
              .id(uliId)
              .author("New Author")
              .citation("New Citation")
              .publishedAt(now)
              .build();
      when(publishedUliRepository.findAllByPublishedAtAfter(any()))
          .thenReturn(List.of(publishedUli));

      // Setup Links
      ActiveCitationUliCaselaw link =
          ActiveCitationUliCaselaw.builder().sourceId(uliId).targetId(caselawId).build();
      when(activeCitationUliCaselawRepository.findAllBySourceIdIn(Set.of(uliId)))
          .thenReturn(List.of(link));

      // Setup Decision with OLD metadata
      PassiveCitationUliDTO passiveCitation =
          PassiveCitationUliDTO.builder().sourceId(uliId).sourceAuthor("Old Author").build();

      DecisionDTO decision =
          DecisionDTO.builder()
              .id(caselawId)
              .documentNumber("RE001")
              .passiveUliCitations(new ArrayList<>(List.of(passiveCitation)))
              .build();

      when(caselawRepository.findAllAffectedByUliUpdates(Set.of(uliId)))
          .thenReturn(List.of(decision));

      // Execute
      Set<String> result = uliCitationSyncService.handleUliPassiveSync();

      // Verify
      assertThat(result).containsExactly("RE001");
      assertThat(passiveCitation.getSourceAuthor()).isEqualTo("New Author");
      assertThat(passiveCitation.getSourceCitation()).isEqualTo("New Citation");

      verify(caselawRepository).save(decision);
      verify(jobSyncStatusRepository).save(any(JobSyncStatus.class));
    }

    @Test
    void shouldOnlyLogWarningWhenPassiveCitationIsMissing() {
      UUID uliId = UUID.randomUUID();
      UUID caselawId = UUID.randomUUID();

      when(jobSyncStatusRepository.findById("ULI_PASSIVE_CITATION_SYNC"))
          .thenReturn(Optional.empty());

      PublishedUli publishedUli =
          PublishedUli.builder().id(uliId).publishedAt(Instant.now()).build();
      when(publishedUliRepository.findAllByPublishedAtAfter(any()))
          .thenReturn(List.of(publishedUli));

      // Link exists in the view...
      ActiveCitationUliCaselaw link =
          ActiveCitationUliCaselaw.builder().sourceId(uliId).targetId(caselawId).build();
      when(activeCitationUliCaselawRepository.findAllBySourceIdIn(Set.of(uliId)))
          .thenReturn(List.of(link));

      // ...but PassiveCitationUliDTO is missing in the Decision
      DecisionDTO decision =
          DecisionDTO.builder()
              .id(caselawId)
              .documentNumber("RE001")
              .passiveUliCitations(new ArrayList<>()) // empty!
              .build();

      when(caselawRepository.findAllAffectedByUliUpdates(Set.of(uliId)))
          .thenReturn(List.of(decision));

      // Execute
      Set<String> result = uliCitationSyncService.handleUliPassiveSync();

      // Verify: No republish, no save, but status still updates
      assertThat(result).isEmpty();
      verify(caselawRepository, never()).save(any());
    }

    @Test
    void shouldDoNothingIfNoNewUlisFound() {
      when(jobSyncStatusRepository.findById("ULI_PASSIVE_CITATION_SYNC"))
          .thenReturn(Optional.empty());
      when(publishedUliRepository.findAllByPublishedAtAfter(any())).thenReturn(List.of());

      Set<String> result = uliCitationSyncService.handleUliPassiveSync();

      assertThat(result).isEmpty();
      verify(activeCitationUliCaselawRepository, never()).findAllBySourceIdIn(any());
      verify(caselawRepository, never()).save(any());
    }
  }

  @Nested
  class handleUliRevoked {

    @Test
    void shouldRemovePassiveCitationsWhenUliIsRevoked() {
      UUID revokedUliId = UUID.randomUUID();
      Instant now = Instant.now();

      // Setup Delta for Revoked ULIs
      when(jobSyncStatusRepository.findById("ULI_REVOKED_SYNC")).thenReturn(Optional.empty());

      RevokedUli revokedEntry = RevokedUli.builder().docUnitId(revokedUliId).revokedAt(now).build();
      when(revokedUliRepository.findAllByRevokedAtAfter(any())).thenReturn(List.of(revokedEntry));

      // Setup Decision that has this ULI as a passive citation
      PassiveCitationUliDTO passiveToStay =
          PassiveCitationUliDTO.builder().sourceId(UUID.randomUUID()).build();
      PassiveCitationUliDTO passiveToRemove =
          PassiveCitationUliDTO.builder().sourceId(revokedUliId).build();

      List<PassiveCitationUliDTO> passiveCitations =
          new ArrayList<>(List.of(passiveToStay, passiveToRemove));

      DecisionDTO decision =
          DecisionDTO.builder()
              .documentNumber("RE-REV-001")
              .passiveUliCitations(passiveCitations)
              .build();

      when(caselawRepository.findAllByPassiveUliSourceIdInAndPendingRevocation(
              Set.of(revokedUliId)))
          .thenReturn(List.of(decision));
      when(caselawRepository.findAllByActiveUliTargetIdInAndPendingRevocation(any()))
          .thenReturn(List.of());

      // Execute
      Set<String> result = uliCitationSyncService.handleUliRevoked();

      // Verify
      assertThat(result).containsExactly("RE-REV-001");
      assertThat(decision.getPassiveUliCitations()).hasSize(1);
      assertThat(decision.getPassiveUliCitations()).containsExactly(passiveToStay);

      verify(caselawRepository).save(decision);
      verify(jobSyncStatusRepository).save(any(JobSyncStatus.class));
    }

    @Test
    void shouldRepublishDecisionWhenActiveUliTargetIsRevoked() {
      UUID revokedUliId = UUID.randomUUID();

      when(jobSyncStatusRepository.findById("ULI_REVOKED_SYNC")).thenReturn(Optional.empty());
      RevokedUli revokedEntry =
          RevokedUli.builder().docUnitId(revokedUliId).revokedAt(Instant.now()).build();
      when(revokedUliRepository.findAllByRevokedAtAfter(any())).thenReturn(List.of(revokedEntry));

      // Decision targets a ULI that was revoked (Active Citation)
      DecisionDTO decision = DecisionDTO.builder().documentNumber("RE-ACT-002").build();

      when(caselawRepository.findAllByPassiveUliSourceIdInAndPendingRevocation(any()))
          .thenReturn(List.of());
      when(caselawRepository.findAllByActiveUliTargetIdInAndPendingRevocation(Set.of(revokedUliId)))
          .thenReturn(List.of(decision));

      // Execute
      Set<String> result = uliCitationSyncService.handleUliRevoked();

      // Verify: Document is marked for republish, but nothing is removed/saved inside the doc
      assertThat(result).containsExactly("RE-ACT-002");
      verify(caselawRepository, never()).save(any());
    }
  }
}
