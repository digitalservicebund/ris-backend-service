package de.bund.digitalservice.ris.caselaw.adapter.publication.sli;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.ActiveCitationSliEntity;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.CitationTypeDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseCitationTypeRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseDocumentationUnitRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DatabaseSliRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.DecisionDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.PassiveCitationSliEntity;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RevokedSli;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.RevokedSliRepository;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.SliActiveCaselawReferenceDTO;
import de.bund.digitalservice.ris.caselaw.adapter.database.jpa.SliDTO;
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
@Import({SliCitationSyncService.class})
class SliCitationSyncServiceTest {

  @Autowired SliCitationSyncService sliCitationSyncService;

  @MockitoBean DatabaseDocumentationUnitRepository caselawRepository;
  @MockitoBean DatabaseSliRepository sliRepository;
  @MockitoBean RevokedSliRepository revokedSliRepository;
  @MockitoBean DatabaseCitationTypeRepository citationTypeRepository;
  @MockitoBean PortalPublicationService portalPublicationService;
  @MockitoBean TransactionTemplate transactionTemplate = new TransactionTemplate();

  @BeforeEach
  void beforeEach() {
    when(transactionTemplate.execute(any()))
        .thenAnswer(
            invocation ->
                invocation
                    .<TransactionCallback<List<UUID>>>getArgument(0)
                    .doInTransaction(mock(TransactionStatus.class)));
  }

  @Nested
  class handleNewlyPublishedAfter {

    @Test
    void shouldUpdateMetadataWhenPassiveCitationExists()
        throws DocumentationUnitNotExistsException {
      UUID sliId = UUID.fromString("7475e016-082d-49e9-9b22-a275c9f65934");
      UUID caselawId = UUID.fromString("2fb43fd9-6414-4407-b705-1c474f0a1c7f");
      Instant now = Instant.now();

      var sli =
          SliDTO.builder()
              .id(sliId)
              .documentNumber("KSNR150060010")
              .author("Beitel, Willibald")
              .bookTitle("Rechtsprechung, Erlasse und Gesetzesänderungen (12)")
              .yearOfPublication("2005")
              .publishedAt(now)
              .build();
      var sliActiveCaselawReference =
          SliActiveCaselawReferenceDTO.builder()
              .targetDocumentationUnitId(caselawId)
              .source(sli)
              .build();
      sli.setActiveCaselawReferences(List.of(sliActiveCaselawReference));

      DecisionDTO decision =
          DecisionDTO.builder().id(caselawId).documentNumber("WBRE410005137").build();

      PassiveCitationSliEntity passiveCitation =
          PassiveCitationSliEntity.builder()
              .sourceId(sliId)
              .sourceAuthor("Gernhuber")
              .sourceBookTitle("Rechtsprechung, Erlasse und Gesetzesänderungen (12)")
              .sourceYearOfPublication("2005")
              .target(decision)
              .build();

      decision.setPassiveSliCitations(new ArrayList<>(List.of(passiveCitation)));

      when(sliRepository.findAllByPublishedAtAfter(any())).thenReturn(List.of(sli));
      when(caselawRepository.findById(caselawId)).thenReturn(Optional.of(decision));

      // Execute
      sliCitationSyncService.handleNewlyPublishedAfter(Instant.now());

      // Verify
      assertThat(decision.getPassiveSliCitations()).hasSize(1).containsExactly(passiveCitation);
      assertThat(passiveCitation.getSourceId()).isEqualTo(sliId);
      assertThat(passiveCitation.getSourceDocumentNumber()).isEqualTo("KSNR150060010");
      assertThat(passiveCitation.getSourceAuthor()).isEqualTo("Beitel, Willibald");
      assertThat(passiveCitation.getSourceBookTitle())
          .isEqualTo("Rechtsprechung, Erlasse und Gesetzesänderungen (12)");
      assertThat(passiveCitation.getSourceYearOfPublication()).isEqualTo("2005");

      verify(caselawRepository).save(decision);
      verify(portalPublicationService).publishDocumentationUnitWithChangelog(caselawId, null);
    }

    @Test
    void shouldCreateMissingPassiveCitation() throws DocumentationUnitNotExistsException {
      UUID sliId = UUID.fromString("7475e016-082d-49e9-9b22-a275c9f65934");
      UUID caselawId = UUID.fromString("2fb43fd9-6414-4407-b705-1c474f0a1c7f");
      Instant now = Instant.now();

      var sli =
          SliDTO.builder()
              .id(sliId)
              .documentNumber("KSNR150060010")
              .author("Beitel, Willibald")
              .bookTitle("Rechtsprechung, Erlasse und Gesetzesänderungen (12)")
              .yearOfPublication("2005")
              .publishedAt(now)
              .build();
      var sliActiveCaselawReference =
          SliActiveCaselawReferenceDTO.builder()
              .targetDocumentationUnitId(caselawId)
              .source(sli)
              .build();
      sli.setActiveCaselawReferences(List.of(sliActiveCaselawReference));

      DecisionDTO decision =
          DecisionDTO.builder()
              .id(caselawId)
              .documentNumber("WBRE410005137")
              .passiveSliCitations(new ArrayList<>())
              .build();

      var citationType = CitationTypeDTO.builder().abbreviation("Vgl").build();

      when(sliRepository.findAllByPublishedAtAfter(any())).thenReturn(List.of(sli));
      when(caselawRepository.findById(caselawId)).thenReturn(Optional.of(decision));
      when(citationTypeRepository.findByAbbreviation("Vgl")).thenReturn(Optional.of(citationType));

      // Execute
      sliCitationSyncService.handleNewlyPublishedAfter(Instant.now());

      // Verify
      assertThat(decision.getPassiveSliCitations()).hasSize(1);
      var passiveCitation = decision.getPassiveSliCitations().getFirst();
      assertThat(passiveCitation.getSourceId()).isEqualTo(sliId);
      assertThat(passiveCitation.getSourceDocumentNumber()).isEqualTo("KSNR150060010");
      assertThat(passiveCitation.getSourceAuthor()).isEqualTo("Beitel, Willibald");
      assertThat(passiveCitation.getSourceBookTitle())
          .isEqualTo("Rechtsprechung, Erlasse und Gesetzesänderungen (12)");
      assertThat(passiveCitation.getSourceYearOfPublication()).isEqualTo("2005");
      assertThat(passiveCitation.getRank()).isEqualTo(1);

      verify(caselawRepository).save(decision);
      verify(portalPublicationService).publishDocumentationUnitWithChangelog(caselawId, null);
    }

    @Test
    void shouldDoNothingIfNoNewSlisFound() throws DocumentationUnitNotExistsException {
      when(sliRepository.findAllByPublishedAtAfter(any())).thenReturn(List.of());

      sliCitationSyncService.handleNewlyPublishedAfter(Instant.now());

      verify(caselawRepository, never()).save(any());
      verify(portalPublicationService, never()).publishDocumentationUnitWithChangelog(any(), any());
    }
  }

  @Nested
  class handleRevokedAfter {

    @Test
    void shouldRemovePassiveCitationsWhenSliIsRevoked() throws DocumentationUnitNotExistsException {
      UUID revokedUuid = UUID.randomUUID();
      Instant now = Instant.now();

      // Setup Delta for Revoked SLI
      RevokedSli revokedEntry = RevokedSli.builder().docUnitId(revokedUuid).revokedAt(now).build();
      when(revokedSliRepository.findAllByRevokedAtAfter(any())).thenReturn(List.of(revokedEntry));

      // Setup Decision that has this SLI as a passive citation
      DecisionDTO decision =
          DecisionDTO.builder()
              .id(UUID.fromString("4eb14cae-7103-4c39-98f6-eff2e79de573"))
              .documentNumber("WBRE410005137")
              .build();

      PassiveCitationSliEntity passiveToStay =
          PassiveCitationSliEntity.builder().target(decision).sourceId(UUID.randomUUID()).build();
      PassiveCitationSliEntity passiveToRemove =
          PassiveCitationSliEntity.builder().target(decision).sourceId(revokedUuid).build();

      decision.setPassiveSliCitations(new ArrayList<>(List.of(passiveToStay, passiveToRemove)));

      when(caselawRepository.findAllByPassiveSliSourceIdAndPendingRevocation(revokedUuid))
          .thenReturn(List.of(decision));

      // Execute
      sliCitationSyncService.handleRevokedAfter(Instant.now());

      // Verify
      assertThat(decision.getPassiveSliCitations()).hasSize(1);
      assertThat(decision.getPassiveSliCitations()).containsExactly(passiveToStay);

      verify(caselawRepository).save(decision);
      verify(portalPublicationService)
          .publishDocumentationUnitWithChangelog(
              UUID.fromString("4eb14cae-7103-4c39-98f6-eff2e79de573"), null);
    }

    @Test
    void shouldRepublishDecisionWhenActiveSliTargetIsRevoked()
        throws DocumentationUnitNotExistsException {
      UUID revokedUuid = UUID.randomUUID();

      RevokedSli revokedEntry =
          RevokedSli.builder().docUnitId(revokedUuid).revokedAt(Instant.now()).build();
      when(revokedSliRepository.findAllByRevokedAtAfter(any())).thenReturn(List.of(revokedEntry));

      // Decision targets an SLI that was revoked (Active Citation)
      DecisionDTO decision =
          DecisionDTO.builder()
              .id(UUID.fromString("6c2447a7-e155-4c6b-9244-37f0d40d8435"))
              .documentNumber("WBRE410005137")
              .build();

      decision.setActiveSliCitations(
          List.of(
              ActiveCitationSliEntity.builder().source(decision).targetId(revokedUuid).build()));

      when(caselawRepository.findAllByPassiveSliSourceIdAndPendingRevocation(any()))
          .thenReturn(List.of());
      when(caselawRepository.findAllByActiveSliTargetIdAndPendingRevocation(revokedUuid))
          .thenReturn(List.of(decision));

      // Execute
      sliCitationSyncService.handleRevokedAfter(Instant.now());

      // Verify: Document is marked for republish, but nothing is removed/saved inside the doc
      verify(caselawRepository, never()).save(any());
      verify(portalPublicationService)
          .publishDocumentationUnitWithChangelog(
              UUID.fromString("6c2447a7-e155-4c6b-9244-37f0d40d8435"), null);
    }
  }
}
