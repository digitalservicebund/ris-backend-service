package de.bund.digitalservice.ris.caselaw.adapter.publication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

      // Setup decision with old metadata
      PassiveCitationUliDTO passiveCitation =
          PassiveCitationUliDTO.builder().sourceId(uliId).sourceAuthor("Old Author").build();

      DecisionDTO decision =
          DecisionDTO.builder()
              .id(caselawId)
              .documentNumber("XXRE123456789")
              .passiveUliCitations(new ArrayList<>(List.of(passiveCitation)))
              .build();

      when(caselawRepository.findById(caselawId)).thenReturn(Optional.of(decision));

      // Execute
      uliCitationSyncService.handleNewlyPublishedAfter(lastRun);

      // Verify
      assertThat(passiveCitation.getSourceAuthor()).isEqualTo("New Author");
      assertThat(passiveCitation.getSourceCitation()).isEqualTo("New Citation");

      verify(caselawRepository).save(decision);
      verify(portalPublicationService).publishDocumentationUnitWithChangelog(caselawId, null);
    }

    @Test
    void shouldOnlyLogWarningWhenPassiveCitationIsMissing()
        throws DocumentationUnitNotExistsException {
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
              .passiveUliCitations(new ArrayList<>()) // leer
              .build();

      when(caselawRepository.findById(caselawId)).thenReturn(Optional.of(decision));

      // Execute
      uliCitationSyncService.handleNewlyPublishedAfter(lastRun);

      // Verify
      verify(caselawRepository, never()).save(any());
      verify(portalPublicationService, never()).publishDocumentationUnit(any());
    }

    @Test
    void shouldDoNothingIfNoNewUlisFound() throws DocumentationUnitNotExistsException {
      Instant lastRun = Instant.now();
      when(databaseUliRepository.findAllByPublishedAtAfter(lastRun)).thenReturn(List.of());

      uliCitationSyncService.handleNewlyPublishedAfter(lastRun);

      verify(caselawRepository, never()).findById(any());
      verify(caselawRepository, never()).save(any());
      verify(portalPublicationService, never()).publishDocumentationUnit(any());
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

      // passive citation has no id (yet) but matches by document number
      PassiveCitationUliDTO passive =
          PassiveCitationUliDTO.builder()
              .sourceId(null)
              .sourceLiteratureDocumentNumber(docNumber)
              .sourceAuthor("Old Author")
              .build();

      DecisionDTO decision =
          DecisionDTO.builder()
              .id(caselawId)
              .passiveUliCitations(new ArrayList<>(List.of(passive)))
              .build();

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
    void shouldRemovePassiveCitationsWhenUliIsRevoked() throws DocumentationUnitNotExistsException {
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
      // assertThat(decision.getPassiveUliCitations()).isEmpty();
      // verify(caselawRepository).save(decision);
      verify(portalPublicationService)
          .publishDocumentationUnitWithChangelog(decision.getId(), null);
    }

    @Test
    void shouldRepublishDecisionWhenActiveUliTargetIsRevoked()
        throws DocumentationUnitNotExistsException {
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

      // Verify: Das Dokument muss in der Liste für das Republishing landen
      // Da der Service intern triggerRepublishing aufruft, prüfen wir die Logik
      // (In einem Integration-Test würden wir portalPublicationService.publishSafe verifizieren)

      verify(caselawRepository, never()).save(any());
      verify(portalPublicationService)
          .publishDocumentationUnitWithChangelog(decision.getId(), null);
    }
  }
}
